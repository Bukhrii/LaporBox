package com.pillbox.laporbox.presentation.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.pillbox.laporbox.presentation.ui.components.BottomNavigation
import com.pillbox.laporbox.presentation.ui.components.HomeTopNavigation
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = koinViewModel()) {
    // State untuk mengontrol Pager (halaman mana yang aktif)
    val pagerState = rememberPagerState(initialPage = 0) { 2 } // Ada 2 halaman
    val scope = rememberCoroutineScope()

    // Ambil daftar resep dari ViewModel
    val resepList by homeViewModel.reseps.collectAsState()

    // Ambil ID dari resep pertama, jika ada.
    val resepIdForLaporan = resepList.firstOrNull()?.id ?: ""

    Scaffold(
        topBar = {
            // Kirim pagerState & scope agar TopBar bisa mengontrol Pager
            HomeTopNavigation(
                pagerState = pagerState,
                onTabSelected = { page ->
                    // Animasikan scroll ke halaman yang dipilih
                    scope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
            )
        },
        content = { paddingValues ->
            // HorizontalPager untuk konten yang bisa digeser
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(paddingValues)
            ) { page ->
                // Tampilkan halaman yang sesuai berdasarkan indeks pager
                when (page) {
                    0 -> HomeResepScreen(navController = navController, viewModel = homeViewModel)
                    1 -> HomeRiwayatScreen(navController = navController, viewModel = homeViewModel)
                }
            }
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                resepIdForLaporan = resepIdForLaporan
            )
        }
    )
}