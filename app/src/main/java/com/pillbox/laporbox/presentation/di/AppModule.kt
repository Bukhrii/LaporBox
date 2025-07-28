package com.pillbox.laporbox.presentation.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pillbox.laporbox.MainViewModel
import com.pillbox.laporbox.data.local.database.AppDatabase
import com.pillbox.laporbox.data.remote.ResendApiService
import com.pillbox.laporbox.data.repository.DataStoreRepositoryImpl
import com.pillbox.laporbox.data.repository.EmailRepositoryImpl
import com.pillbox.laporbox.data.repository.ResepRepositoryImpl
import com.pillbox.laporbox.data.repository.UserRepositoryImpl
import com.pillbox.laporbox.data.worker.SyncResepWorker
import com.pillbox.laporbox.domain.repository.DataStoreRepository
import com.pillbox.laporbox.domain.repository.EmailRepository
import com.pillbox.laporbox.domain.repository.ResepRepository
import com.pillbox.laporbox.domain.repository.UserRepository
import com.pillbox.laporbox.domain.usecase.ReadOnboardingUseCase
import com.pillbox.laporbox.domain.usecase.SaveOnboardingUseCase
import com.pillbox.laporbox.presentation.ui.screens.auth.AuthViewModel
import com.pillbox.laporbox.presentation.ui.screens.home.HomeViewModel
import com.pillbox.laporbox.presentation.ui.screens.lapor.LaporViewModel
import com.pillbox.laporbox.presentation.ui.screens.onboarding.OnboardingViewModel
import com.pillbox.laporbox.presentation.ui.screens.profile.ProfileViewModel
import com.pillbox.laporbox.presentation.ui.screens.resepform.FormResepViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// --- MODUL UNTUK JARINGAN (RETROFIT) ---
val networkModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("https://api.resend.com/") // Base URL untuk Resend API
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single {
        get<Retrofit>().create(ResendApiService::class.java)
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "laporbox-database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().resepDao() }
}

val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
}

val repositoryModule = module {
    single<ResepRepository> {
        ResepRepositoryImpl(
            auth = get(),
            firestore = get(),
            resepDao = get(),
            context = androidContext()
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            firestore = get()
        )
    }
    single<DataStoreRepository> {
        DataStoreRepositoryImpl(context = androidContext())
    }
    // --- INI BAGIAN YANG DIPERBAIKI ---
    single<EmailRepository> {
        EmailRepositoryImpl(apiService = get())
    }
    // ---------------------------------
}

val useCaseModule = module {
    factory { SaveOnboardingUseCase(dataStoreRepository = get()) }
    factory { ReadOnboardingUseCase(dataStoreRepository = get()) }
}

val viewModelModule = module {
    viewModel {
        HomeViewModel(
            resepRepository = get(),
            auth = get(),
            userRepository = get(),
            emailRepository = get(),
        )
    }
    viewModel {
        FormResepViewModel(
            repository = get()
        )
    }
    viewModel {
        AuthViewModel(
            auth = get(),
            userRepository = get()
        )
    }
    viewModel {
        OnboardingViewModel(
            saveOnboardingUseCase = get(),
            readOnboardingUseCase = get()
        )
    }
    viewModel {
        LaporViewModel(
            auth = get(),
            firestore = get(),
            emailRepository = get(),
            userRepository = get()
        )
    }
    // PASTIKAN BLOK INI ADA
    viewModel {
        MainViewModel(
            readOnboardingUseCase = get()
        )
    }
    viewModel {
        ProfileViewModel(
            auth = get(),
            userRepository = get()
        )
    }
}

// --- DAFTAR SEMUA MODULE ---
val appModules = listOf(
    networkModule,
    databaseModule,
    firebaseModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)