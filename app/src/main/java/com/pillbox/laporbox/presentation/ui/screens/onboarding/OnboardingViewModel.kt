package com.pillbox.laporbox.presentation.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pillbox.laporbox.domain.repository.UserRepository
import com.pillbox.laporbox.domain.usecase.ReadOnboardingUseCase
import com.pillbox.laporbox.domain.usecase.SaveOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val saveOnboardingUseCase: SaveOnboardingUseCase,
    private val readOnboardingUseCase: ReadOnboardingUseCase
) : ViewModel() {

    private val _onboardingCompleted = MutableStateFlow<Boolean?>(null)
    val onboardingCompleted: StateFlow<Boolean?> = _onboardingCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            readOnboardingUseCase().collect { isCompleted ->
                _onboardingCompleted.value = isCompleted
            }
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            saveOnboardingUseCase(true)
        }
    }
}