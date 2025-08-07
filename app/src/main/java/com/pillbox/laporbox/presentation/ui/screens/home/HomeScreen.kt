package com.pillbox.laporbox.presentation.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.presentation.ui.navigation.Screen
import com.pillbox.laporbox.presentation.ui.theme.CardBlue
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val resepList by viewModel.reseps.collectAsState()

    // --- PERBAIKAN UTAMA DI SINI ---
    // Gunakan if-else untuk memilih layout yang akan ditampilkan

    if (resepList.isEmpty()) {
        EmptyStateScreen()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), // LazyColumn bisa fillMaxSize di sini
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "All Medicines",
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.padding(top = 50.dp, bottom = 10.dp, start = 20.dp, end = 20.dp)
                    )
                }
            }
            items(items = resepList, key = { it.id }) { resep ->
                ResepItemCard(
                    resep = resep,
                    onClick = {
                        navController.navigate("${Screen.ResepDetail.route}/${resep.id}")
                    }
                )
            }
        }
    }
}

// Composable baru khusus untuk tampilan halaman kosong
@Composable
private fun EmptyStateScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Belum ada resep. Tekan tombol '+' untuk menambahkan.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun ResepItemCard(resep: ResepModel, onClick: () -> Unit) {
    val progress = if (resep.jumlahObat > 0) {
        resep.totalLaporan.toFloat() / resep.jumlahObat.toFloat()
    } else { 0f }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = CardBlue,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = resep.penyakit,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = resep.namaObat,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${resep.totalLaporan} / ${resep.jumlahObat} pil dilaporkan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}