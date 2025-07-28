package com.pillbox.laporbox.presentation.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.components.BottomNavigation
import com.pillbox.laporbox.presentation.ui.screens.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(), // Ditambahkan untuk mendapatkan resepId
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user

    // Ambil daftar resep dari HomeViewModel untuk mendapatkan resepId
    val resepList by homeViewModel.reseps.collectAsState()
    val resepIdForLaporan = resepList.firstOrNull()?.id ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Info Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                resepIdForLaporan = resepIdForLaporan
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding dari Scaffold
                .padding(horizontal = 24.dp) // Padding konten
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Bagian Foto Profil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = user?.photoUrl,
                    contentDescription = "Foto Profil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                    },
                    error = {
                        // --- PERBAIKAN DI SINI ---
                        // Menggunakan Image, bukan Icon
                        Image(
                            painter = painterResource(id = R.drawable.user_profile), // Ganti ke drawable Anda jika berbeda
                            contentDescription = "Placeholder Foto",
                            modifier = Modifier.size(72.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { /* TODO: Logika ubah foto */ }) {
                Text(text = "Ubah Foto", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Informasi Pengguna
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (user != null) {
                InfoField(label = "Nama Pengguna", value = user.username)
                Spacer(modifier = Modifier.height(16.dp))
                InfoField(label = "Email", value = user.email)
                Spacer(modifier = Modifier.height(16.dp))
                InfoField(label = "Nomor Telepon", value = user.phoneNumber ?: "-")
            } else {
                Text(state.errorMessage ?: "Gagal memuat data pengguna.")
            }


            Spacer(modifier = Modifier.weight(1f)) // Mendorong tombol ke bawah

            // Tombol Logout
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
private fun InfoField(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}