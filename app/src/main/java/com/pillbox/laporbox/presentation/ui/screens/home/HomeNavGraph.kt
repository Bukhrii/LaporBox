package com.pillbox.laporbox.presentation.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pillbox.laporbox.presentation.ui.components.BottomNavigation
import com.pillbox.laporbox.presentation.ui.navigation.AUTH_GRAPH_ROUTE
import com.pillbox.laporbox.presentation.ui.navigation.RESEP_ROUTE
import com.pillbox.laporbox.presentation.ui.navigation.Screen
import com.pillbox.laporbox.presentation.ui.screens.auth.AuthViewModel
import com.pillbox.laporbox.presentation.ui.screens.lapor.LaporScreen
import com.pillbox.laporbox.presentation.ui.screens.profile.ProfileScreen
import com.pillbox.laporbox.presentation.ui.screens.resep.LaporanDetailScreen
import com.pillbox.laporbox.presentation.ui.screens.resep.ResepDetailScreen
import com.pillbox.laporbox.presentation.ui.screens.resepform.*
import com.pillbox.laporbox.presentation.ui.theme.Gradient
import com.pillbox.laporbox.presentation.ui.theme.TextHeading
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeNavGraph(rootNavController: NavController) {
    val homeNavController = rememberNavController()
    val homeViewModel: HomeViewModel = koinViewModel()
    val resepList by homeViewModel.reseps.collectAsState()
    val context = LocalContext.current

    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screensWithBottomNav = listOf(
        Screen.Home.route,
        Screen.Profile.route,
        "${Screen.ResepDetail.route}/{resepId}",
        "${Screen.LaporanDetail.route}/{imageUrl}/{timestamp}"
    )

    // Daftar untuk layar yang memiliki Tombol '+' (HANYA Home)
    val screensWithFab = listOf(
        Screen.Home.route
    )

    val gradientColor = Gradient.appGradientBrush

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientColor)
    ) {
        Scaffold(
            containerColor = Color.Transparent,

            bottomBar = {
                if (currentRoute in screensWithBottomNav) {
                    BottomNavigation(navController = homeNavController)
                }
            },
            floatingActionButton = {
                if (currentRoute in screensWithFab) {
                    FloatingActionButton(
                        onClick = {
                            val resepIdForLaporan = resepList.firstOrNull()?.id ?: ""
                            if (resepIdForLaporan.isBlank()) {
                                homeNavController.navigate(RESEP_ROUTE)
                            } else {
                                Toast.makeText(context, "Cukup 1 resep untuk Hipertensi", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = CircleShape,
                        containerColor = TextHeading,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Tambah Resep",
                            tint = Color.White
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = homeNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(navController = homeNavController, viewModel = homeViewModel)
                }
                composable(
                    route = "${Screen.ResepDetail.route}/{resepId}",
                    arguments = listOf(navArgument("resepId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val resepId = backStackEntry.arguments?.getString("resepId")
                    if (resepId != null) {
                        ResepDetailScreen(navController = homeNavController, resepId = resepId)
                    }
                }
                composable(
                    route = "${Screen.LaporanDetail.route}/{imageUrl}/{timestamp}",
                    arguments = listOf(
                        navArgument("imageUrl") { type = NavType.StringType },
                        navArgument("timestamp") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val imageUrl = backStackEntry.arguments?.getString("imageUrl")
                    val timestamp = backStackEntry.arguments?.getString("timestamp")
                    if (imageUrl != null && timestamp != null) {
                        LaporanDetailScreen(navController = homeNavController, imageUrl = imageUrl, timestamp = timestamp)
                    }
                }
                composable(Screen.Profile.route) {
                    val authViewModel: AuthViewModel = koinViewModel()
                    ProfileScreen(
                        navController = homeNavController,
                        onLogout = {
                            authViewModel.signOut()
                            rootNavController.navigate(AUTH_GRAPH_ROUTE) {
                                popUpTo(rootNavController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    )
                }

                formResepNavGraph(navController = homeNavController)

                composable(
                    route = "lapor_screen/{resepId}",
                    arguments = listOf(navArgument("resepId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val resepId = backStackEntry.arguments?.getString("resepId")
                    if (resepId != null) {
                        LaporScreen(navController = homeNavController, resepId = resepId)
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.formResepNavGraph(navController: NavController) {
    navigation(
        startDestination = Screen.MulaiForm.route,
        route = RESEP_ROUTE
    ) {
        composable(Screen.MulaiForm.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(RESEP_ROUTE) }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            val resepId = backStackEntry.arguments?.getString("resepId")
            LaunchedEffect(key1 = resepId) { viewModel.loadResep(resepId) }
            MulaiFormScreen(navController = navController)
        }
        composable(Screen.FormDokter.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(RESEP_ROUTE) }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            FormDokterScreen(navController, viewModel)
        }
        composable(Screen.FormPenyakit.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(RESEP_ROUTE) }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            FormPenyakitScreen(navController, viewModel)
        }
        composable(Screen.FormKeluarga.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(RESEP_ROUTE) }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            FormKeluargaScreen(navController, viewModel)
        }
        composable(Screen.FormObat.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(RESEP_ROUTE) }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            FormObatScreen(navController, viewModel)
        }
        composable(Screen.FormPengingat.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(RESEP_ROUTE) }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            FormPengingatScreen(navController, viewModel)
        }
        composable(Screen.FormDetail.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(RESEP_ROUTE) }
            val viewModel: FormResepViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            FormDetailScreen(navController = navController, viewModel = viewModel, onFinish = {
                navController.popBackStack(Screen.Home.route, inclusive = false)
            })
        }
    }
}