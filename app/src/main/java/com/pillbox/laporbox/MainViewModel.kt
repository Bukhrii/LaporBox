package com.pillbox.laporbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillbox.laporbox.domain.usecase.ReadOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val readOnboardingUseCase: ReadOnboardingUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow("onboarding_screen")
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val isCompleted = readOnboardingUseCase().first()
            _startDestination.value = if (isCompleted) "home_screen" else "onboarding_screen"
            _isLoading.value = false
        }
    }
}