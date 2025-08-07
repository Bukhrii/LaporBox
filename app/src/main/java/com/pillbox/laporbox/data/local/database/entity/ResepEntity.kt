package com.pillbox.laporbox.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pillbox.laporbox.domain.models.ResepModel
import java.util.Date
import java.util.UUID

@Entity(tableName = "resep_table")
data class ResepEntity(
    @PrimaryKey val id: String = "",
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
    val createdAt: Date = Date(),
    val isSynced: Boolean = false,
    val totalLaporan: Long = 0,
    val terakhirLapor: Date? = null
)

fun ResepEntity.toResepModel(): ResepModel {
    return ResepModel(
        id = this.id,
        userId = this.userId,
        namaDokter = this.namaDokter,
        emailFaskes = this.emailFaskes,
        tanggalKontrolTerakhir = this.tanggalKontrolTerakhir,
        tanggalKontrolBerikutnya = this.tanggalKontrolBerikutnya,
        penyakit = this.penyakit,
        penyakitLainnya = this.penyakitLainnya,
        namaKeluarga = this.namaKeluarga,
        emailKeluarga = this.emailKeluarga,
        namaObat = this.namaObat,
        frekuensiObat = this.frekuensiObat,
        aturanMakan = this.aturanMakan,
        jadwalPengingat = this.jadwalPengingat,
        durasiObat = this.durasiObat,
        jumlahObat = this.jumlahObat,
        createdAt = this.createdAt,
        isSynced = this.isSynced,
        totalLaporan = this.totalLaporan,
        terakhirLapor = this.terakhirLapor
    )
}