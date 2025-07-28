package com.pillbox.laporbox

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.cloudinary.android.MediaManager
import com.pillbox.laporbox.presentation.di.appModules
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MyApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        // --- TAMBAHKAN BLOK INISIALISASI CLOUDINARY DI SINI ---
        try {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
            )
            MediaManager.init(this, config)
        } catch (e: Exception) {
            Log.e("MyApplication", "Gagal inisialisasi Cloudinary: ${e.message}")
        }
        // --------------------------------------------------------

        startKoin {
            androidContext(this@MyApplication)
            workManagerFactory()
            modules(appModules)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(get())
            .build()
}