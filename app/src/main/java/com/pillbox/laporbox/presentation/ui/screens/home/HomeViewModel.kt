package com.pillbox.laporbox.presentation.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.domain.models.UserModel
import com.pillbox.laporbox.domain.repository.ResepRepository
import com.pillbox.laporbox.domain.repository.UserRepository
import com.pillbox.laporbox.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val resepRepository: ResepRepository,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    val reseps: StateFlow<List<ResepModel>> = resepRepository.getReseps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _user = MutableStateFlow<UserModel?>(null)
    val user = _user.asStateFlow()

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                when(val result = userRepository.getUser(userId)) {
                    is Resource.Success -> _user.value = result.data
                    is Resource.Error -> Log.e("HomeViewModel", "Gagal fetch user: ${result.message}")
                    else -> {}
                }
            }
        }
    }

    fun syncResepsFromRemote() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Memulai sinkronisasi manual dari remote...")
            val result = resepRepository.fetchAndSyncReseps()
            if (result is Resource.Error) {
                Log.e("HomeViewModel", "Sinkronisasi Gagal: ${result.message}")
            } else {
                Log.d("HomeViewModel", "Sinkronisasi Selesai.")
            }
        }
    }
}