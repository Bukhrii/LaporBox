package com.pillbox.laporbox.presentation.ui.screens.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pillbox.laporbox.domain.models.UserModel
import com.pillbox.laporbox.domain.repository.UserRepository
import com.pillbox.laporbox.util.Resource
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data object RegistrationSuccess : AuthState()
    data class Error(val message: String) : AuthState()
}


class AuthViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email dan password tidak boleh kosong")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login gagal")
                }
            }
    }

    fun signup(
        username: String,
        phoneNumber: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (username.isBlank() || email.isEmpty() || password.isEmpty() || confirmPassword.isBlank()) {
            _authState.value = AuthState.Error("Tidak boleh ada yang tidak diisi")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Password dan konfirmasi password tidak cocok")
            return
        }


        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && auth.currentUser != null) {
                    val firebaseUser = auth.currentUser!!

                    val newUser = UserModel(
                        uid = firebaseUser.uid,
                        username = username,
                        email = email,
                        photoUrl = null,
                        phoneNumber = phoneNumber
                    )
                    viewModelScope.launch {
                        when (userRepository.createUser(newUser)) {
                            is Resource.Success -> {
                                _authState.value = AuthState.RegistrationSuccess
                            }
                            is Resource.Error -> {
                                _authState.value = AuthState.Error("Gagal menyimpan data pengguna.")
                            }
                            else -> {}
                        }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Registrasi gagal")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}