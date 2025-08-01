package com.pillbox.laporbox.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "laporan_table")
data class LaporanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val resepId: String,
    val imagePath: String,
    val attemptCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
