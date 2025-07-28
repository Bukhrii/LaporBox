package com.pillbox.laporbox.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pillbox.laporbox.data.local.database.dao.ResepDao
import com.pillbox.laporbox.data.local.database.entity.ResepEntity

@Database(entities = [ResepEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resepDao(): ResepDao
}