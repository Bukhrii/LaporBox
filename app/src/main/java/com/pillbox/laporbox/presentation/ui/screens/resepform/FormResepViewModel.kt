package com.pillbox.laporbox.presentation.ui.screens.resepform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.domain.repository.ResepRepository
import com.pillbox.laporbox.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FormMode { CREATE, EDIT }
enum class SaveStatus { IDLE, LOADING, SUCCESS, ERROR }

data class FormResepState(
    val formMode: FormMode = FormMode.CREATE,
    val resepId: String? = null,
    val namaDokter: String = "",
    val emailFaskes: String = "",
    val tanggalKontrolTerakhir: String = "",
    val tanggalKontrolBerikutnya: String = "",
    val penyakit: String = "",
    val penyakitLainnya: String = "",
    val namaKeluarga: String = "",
    val emailKeluarga: String = "",
    val namaObat: String = "",
    val frekuensiObat: String = "",
    val aturanMakan: String = "",
    val jadwalPengingat: List<String> = listOf("07:00"),
    val saveStatus: SaveStatus = SaveStatus.IDLE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FormResepViewModel(
    private val repository: ResepRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormResepState())
    val uiState = _uiState.asStateFlow()

    fun loadResep(resepId: String?) {
        if (resepId == null) {
            _uiState.value = FormResepState(formMode = FormMode.CREATE)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val resepModel = repository.getResepById(resepId).first()
            if (resepModel != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        formMode = FormMode.EDIT,
                        resepId = resepModel.id,
                        namaDokter = resepModel.namaDokter,
                        emailFaskes = resepModel.emailFaskes,
                        tanggalKontrolTerakhir = resepModel.tanggalKontrolTerakhir ?: "",
                        tanggalKontrolBerikutnya = resepModel.tanggalKontrolBerikutnya ?: "",
                        penyakit = resepModel.penyakit,
                        penyakitLainnya = resepModel.penyakitLainnya ?: "",
                        namaKeluarga = resepModel.namaKeluarga,
                        emailKeluarga = resepModel.emailKeluarga,
                        namaObat = resepModel.namaObat,
                        frekuensiObat = resepModel.frekuensiObat
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Resep tidak ditemukan") }
            }
        }
    }

    fun onNamaDokterChange(nama: String) { _uiState.update { it.copy(namaDokter = nama) } }
    fun onEmailFaskesChange(email: String) { _uiState.update  { it.copy(emailFaskes = email) } }
    fun onTanggalKontrolTerakhirChange(tanggal: String) { _uiState.update { it.copy(tanggalKontrolTerakhir = tanggal) } }
    fun onTanggalKontrolBerikutnyaChange(tanggal: String) { _uiState.update { it.copy(tanggalKontrolBerikutnya = tanggal) } }
    fun onPenyakitSelected(penyakit: String) { _uiState.update { it.copy(penyakit = penyakit) } }
    fun onPenyakitLainnyaChange(penyakit: String) { _uiState.update { it.copy(penyakitLainnya = penyakit) } }
    fun onNamaKeluargaChange(nama: String) { _uiState.update { it.copy(namaKeluarga = nama) } }
    fun onEmailKeluargaChange(email: String) { _uiState.update { it.copy(emailKeluarga = email) } }
    fun onNamaObatChange(nama: String) { _uiState.update { it.copy(namaObat = nama) } }
    fun onFrekuensiObatChange(frekuensi: String) {
        val defaultTimes = when (frekuensi) {
            "1x sehari" -> listOf("07:00")
            "2x sehari" -> listOf("07:00", "19:00")
            "3x sehari" -> listOf("07:00", "13:00", "19:00")
            else -> emptyList()
        }
        _uiState.update {
            it.copy(
                frekuensiObat = frekuensi,
                jadwalPengingat = defaultTimes
            )
        }
    }
    fun onAturanMakanChange(aturan: String) { _uiState.update { it.copy(aturanMakan = aturan) } }

    fun onTimeChanged(index: Int, time: String) {
        _uiState.update { currentState ->
            val updatedJadwal = currentState.jadwalPengingat.toMutableList()
            if (index < updatedJadwal.size) {
                updatedJadwal[index] = time
            }
            currentState.copy(jadwalPengingat = updatedJadwal)
        }
    }

    fun saveOrUpdateResep() {
        viewModelScope.launch {
            _uiState.update { it.copy(saveStatus = SaveStatus.LOADING) }
            val currentState = _uiState.value
            val jadwalPengingatMap = currentState.jadwalPengingat.mapIndexed { index, time ->
                "waktu_${index + 1}" to time
            }.toMap()

            val resepModel = ResepModel(
                id = currentState.resepId ?: "",
                namaDokter = currentState.namaDokter,
                emailFaskes = currentState.emailFaskes,
                tanggalKontrolTerakhir = currentState.tanggalKontrolTerakhir.takeIf { it.isNotBlank() },
                tanggalKontrolBerikutnya = currentState.tanggalKontrolBerikutnya.takeIf { it.isNotBlank() },
                penyakit = currentState.penyakit,
                penyakitLainnya = if (currentState.penyakit == "Lainnya") currentState.penyakitLainnya else null,
                namaKeluarga = currentState.namaKeluarga,
                emailKeluarga = currentState.emailKeluarga,
                namaObat = currentState.namaObat,
                frekuensiObat = currentState.frekuensiObat,
                aturanMakan = currentState.aturanMakan,
                jadwalPengingat = jadwalPengingatMap
            )

            val result = if (currentState.formMode == FormMode.CREATE) {
                repository.addResep(resepModel)
            } else {
                repository.updateResep(resepModel)
            }

            when (result) {
                is Resource.Success -> _uiState.update { it.copy(saveStatus = SaveStatus.SUCCESS) }
                is Resource.Error -> _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = result.message) }
                is Resource.Loading -> { }
            }
        }
    }
}