package com.pillbox.laporbox.domain.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ResepModel(
    val id: String = "",
    val userId: String = "",
    val namaDokter: String = "",
    val emailFaskes: String = "",
    val tanggalKontrolTerakhir: String? = null,
    val tanggalKontrolBerikutnya: String? = null,
    val penyakit: String = "",
    val penyakitLainnya: String? = null,
    val namaKeluarga: String = "",
    val emailKeluarga: String = "",
    val namaObat: String = "",
    val frekuensiObat: String = "",
    val aturanMakan: String = "",
    val jadwalPengingat: Map<String, String> = emptyMap(),
    val durasiObat: Int = 30,
    val jumlahObat: Int = 30,
    @ServerTimestamp
    val createdAt: Date? = null,
    val isSynced: Boolean = false,
    val totalLaporan: Long = 0,
    val terakhirLapor: Date? = null
)