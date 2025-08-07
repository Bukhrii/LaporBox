package com.pillbox.laporbox.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pillbox.laporbox.data.local.database.dao.LaporanDao
import com.pillbox.laporbox.data.local.database.dao.ResepDao
import com.pillbox.laporbox.data.local.database.entity.LaporanEntity
import com.pillbox.laporbox.data.local.database.entity.ResepEntity

@Database(entities = [ResepEntity::class, LaporanEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resepDao(): ResepDao
    abstract fun laporanDao(): LaporanDao
}