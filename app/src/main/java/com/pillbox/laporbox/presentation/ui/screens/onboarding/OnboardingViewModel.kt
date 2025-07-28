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
    // Mengganti nama agar lebih jelas dan konsisten
    private val readOnboardingUseCase: ReadOnboardingUseCase,
    private val saveOnboardingUseCase: SaveOnboardingUseCase
) : ViewModel() {

    // 1. State private yang hanya bisa diubah di dalam ViewModel
    //    Menggunakan Boolean? (nullable) dengan nilai awal null untuk merepresentasikan state "sedang memuat".
    private val _onboardingCompleted = MutableStateFlow<Boolean?>(null)

    // 2. State public yang bersifat read-only untuk diamati oleh UI (Composable)
    val onboardingCompleted: StateFlow<Boolean?> = _onboardingCompleted.asStateFlow()

    // 3. init block akan dijalankan saat ViewModel pertama kali dibuat
    init {
        // Langsung cek status onboarding dari repository/datastore
        viewModelScope.launch {
            readOnboardingUseCase().collect { isCompleted ->
                _onboardingCompleted.value = isCompleted
            }
        }
    }

    /**
     * Fungsi ini dipanggil dari UI ketika pengguna menekan tombol "Selesai" di layar onboarding.
     */
    fun finishOnboarding() {
        viewModelScope.launch {
            // Memanggil use case yang benar untuk menyimpan status
            saveOnboardingUseCase(true)
        }
    }
}