package com.pillbox.laporbox.presentation.ui.navigation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.pillbox.laporbox.presentation.ui.screens.auth.AuthState
import com.pillbox.laporbox.presentation.ui.screens.auth.AuthViewModel
import com.pillbox.laporbox.presentation.ui.screens.auth.LoginScreen
import com.pillbox.laporbox.presentation.ui.screens.auth.SignupScreen
import com.pillbox.laporbox.presentation.ui.screens.home.HomeScreen
import com.pillbox.laporbox.presentation.ui.screens.home.HomeViewModel
import com.pillbox.laporbox.presentation.ui.screens.lapor.LaporScreen
import com.pillbox.laporbox.presentation.ui.screens.resepform.MulaiFormScreen
import com.pillbox.laporbox.presentation.ui.screens.onboarding.OnboardingScreen
import com.pillbox.laporbox.presentation.ui.screens.onboarding.OnboardingViewModel
import com.pillbox.laporbox.presentation.ui.screens.profile.ProfileScreen
import com.pillbox.laporbox.presentation.ui.screens.resepform.FormDetailScreen
import com.pillbox.laporbox.presentation.ui.screens.resepform.FormDokterScreen
import com.pillbox.laporbox.presentation.ui.screens.resepform.FormKeluargaScreen
import com.pillbox.laporbox.presentation.ui.screens.resepform.FormKontrolScreen
import com.pillbox.laporbox.presentation.ui.screens.resepform.FormObatScreen
import com.pillbox.laporbox.presentation.ui.screens.resepform.FormPenyakitScreen
import com.pillbox.laporbox.presentation.ui.screens.resepform.FormResepViewModel
import org.koin.androidx.compose.koinViewModel


const val AUTH_GRAPH_ROUTE = "auth_graph"
const val MAIN_GRAPH_ROUTE = "main_graph"
const val RESEP_ROUTE = "resep_flow"

@Composable
fun RootNavGraph(
    // Inject ViewModel yang diperlukan di level tertinggi
    authViewModel: AuthViewModel = koinViewModel(),
    onboardingViewModel: OnboardingViewModel = koinViewModel()
) {
    val navController = rememberNavController()

    // Ambil state dari ViewModel
    val hasCompletedOnboarding by onboardingViewModel.onboardingCompleted.collectAsState(initial = null)
    val authState by authViewModel.authState.observeAsState()

    // Tampilkan layar kosong selagi menunggu state dimuat
    if (hasCompletedOnboarding == null || authState == null) {
        // Bisa diganti dengan Composable Splash Screen atau loading indicator
        return
    }

    // Tentukan rute awal secara dinamis berdasarkan state
    val startDestination = when {
        hasCompletedOnboarding == false -> Screen.Onboarding.route
        authState is AuthState.Authenticated -> MAIN_GRAPH_ROUTE
        else -> AUTH_GRAPH_ROUTE
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Rute Onboarding (jika belum selesai)
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    // Setelah onboarding selesai, navigasi ke auth graph.
                    // Jika user sudah login, startDestination akan otomatis mengarahkannya ke main graph.
                    navController.navigate(AUTH_GRAPH_ROUTE) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Graph Autentikasi (Login & Register)
        authNavGraph(navController = navController)

        // 3. Graph Utama Aplikasi (setelah login berhasil)
        mainNavGraph(navController = navController)
    }
}

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        route = AUTH_GRAPH_ROUTE,
        startDestination = Screen.LoginScreen.route
    ) {
        composable(Screen.LoginScreen.route) {
            val homeViewModel: HomeViewModel = koinViewModel() // Dapatkan instance HomeViewModel
            LoginScreen(
                homeViewModel = homeViewModel, // Pass ViewModel
                onLoginSuccess = {
                    navController.navigate(MAIN_GRAPH_ROUTE) {
                        popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignupScreen.route)
                }
            )
        }
        composable(Screen.SignupScreen.route) {
            SignupScreen(
                onSignUpSuccessToLogin = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}

fun NavGraphBuilder.mainNavGraph(navController: NavController) {
    navigation(
        route = MAIN_GRAPH_ROUTE,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            // Contoh jika profile butuh logout
            val authViewModel: AuthViewModel = koinViewModel()
            ProfileScreen(
                navController = navController,
                onLogout = {
                    authViewModel.signOut()
                    // Setelah logout, kembali ke auth graph
                    navController.navigate(AUTH_GRAPH_ROUTE) {
                        popUpTo(MAIN_GRAPH_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "lapor_screen/{resepId}", // Definisikan argumen di rute
            arguments = listOf(navArgument("resepId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Ambil argumen dari backStackEntry
            val resepId = backStackEntry.arguments?.getString("resepId")
            if (resepId != null) {
                LaporScreen(navController = navController, resepId = resepId)
            } else {
                // Handle jika resepId tidak ada, misal kembali ke halaman sebelumnya
                navController.popBackStack()
            }
        }

        // Graph untuk form resep berada di dalam main graph
        formResepNavGraph(navController = navController)
    }
}


// Fungsi ekstensi untuk NavGraphBuilder
fun NavGraphBuilder.formResepNavGraph(navController: NavController) {
    navigation(
        startDestination = Screen.MulaiForm.route,
        route = "$RESEP_ROUTE?resepId={resepId}",
        arguments = listOf(navArgument("resepId") {
            type = NavType.StringType
            nullable = true
        })
    ) {

        // Setiap layar di dalam 'navigation' ini akan berbagi ViewModel yang sama
        // karena kita mengaitkannya ke 'RESEP_ROUTE'.

        composable(Screen.MulaiForm.route) { backStackEntry ->
            // Dapatkan parent entry dari rute 'navigation' yang benar
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("$RESEP_ROUTE?resepId={resepId}")
            }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            // AMBIL resepId dari argumen
            val resepId = backStackEntry.arguments?.getString("resepId")

            // GUNAKAN LaunchedEffect untuk memanggil loadResep HANYA SEKALI
            LaunchedEffect(key1 = resepId) {
                viewModel.loadResep(resepId)
            }

            MulaiFormScreen(navController = navController, viewModel = viewModel)
        }

        composable(Screen.FormDokter.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RESEP_ROUTE)
            }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            FormDokterScreen(navController, viewModel)
        }

        composable(Screen.FormKontrol.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RESEP_ROUTE)
            }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            FormKontrolScreen(navController, viewModel)
        }

        composable(Screen.FormPenyakit.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RESEP_ROUTE)
            }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            FormPenyakitScreen(navController, viewModel)
        }

        composable(Screen.FormKeluarga.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RESEP_ROUTE)
            }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            FormKeluargaScreen(navController, viewModel)
        }

        composable(Screen.FormObat.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RESEP_ROUTE)
            }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            FormObatScreen(navController, viewModel)
        }

        composable(Screen.FormDetail.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RESEP_ROUTE)
            }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            FormDetailScreen(navController, viewModel)
        }
    }
}