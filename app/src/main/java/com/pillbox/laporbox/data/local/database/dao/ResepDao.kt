package com.pillbox.laporbox.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pillbox.laporbox.data.local.database.entity.ResepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateResep(resep: ResepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(reseps: List<ResepEntity>)

    @Delete
    suspend fun deleteResep(resep: ResepEntity)

    @Query("SELECT * FROM resep_table WHERE id = :resepId")
    fun getResepById(resepId: String): Flow<ResepEntity?>

    @Query("SELECT * FROM resep_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllReseps(userId: String): Flow<List<ResepEntity>>

    @Query("SELECT * FROM resep_table WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsyncedReseps(userId: String): List<ResepEntity>
}