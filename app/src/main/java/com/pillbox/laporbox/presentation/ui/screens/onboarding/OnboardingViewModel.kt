package com.pillbox.laporbox.presentation.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillbox.laporbox.domain.usecase.ReadOnboardingUseCase
import com.pillbox.laporbox.domain.usecase.SaveOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val readOnboardingUseCase: ReadOnboardingUseCase,
    private val saveOnboardingUseCase: SaveOnboardingUseCase
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