package com.pillbox.laporbox.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pillbox.laporbox.data.local.database.entity.LaporanEntity

@Dao
interface LaporanDao {
    @Insert
    suspend fun insert(laporan: LaporanEntity)

    @Query("SELECT * FROM laporan_table ORDER BY createdAt ASC LIMIT 1")
    suspend fun getNextPendingLaporan(): LaporanEntity?

    @Update
    suspend fun update(laporan: LaporanEntity)

    @Delete
    suspend fun delete(laporan: LaporanEntity)
}