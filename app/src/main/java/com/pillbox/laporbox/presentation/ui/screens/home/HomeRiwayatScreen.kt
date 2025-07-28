package com.pillbox.laporbox.presentation.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.pillbox.laporbox.domain.models.LaporanModel
import com.pillbox.laporbox.domain.models.ResepModel

@Composable
fun HomeRiwayatScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val laporanList by viewModel.laporanList.collectAsState()
    val isLoading by viewModel.isLoadingLaporan.collectAsState()
    val emailStatus by viewModel.rekapEmailStatus.collectAsState() // Ambil state status email

    val resep by viewModel.reseps.collectAsState()
    val resepInfo = resep.firstOrNull()

    val context = LocalContext.current

    // Tampilkan Toast berdasarkan status pengiriman email
    LaunchedEffect(emailStatus) {
        when (emailStatus) {
            EmailSendStatus.SUCCESS -> {
                Toast.makeText(context, "Rekap laporan berhasil dikirim!", Toast.LENGTH_SHORT).show()
                viewModel.resetEmailStatus() // Reset status setelah notifikasi
            }
            EmailSendStatus.ERROR -> {
                Toast.makeText(context, "Gagal mengirim rekap. Pastikan email valid.", Toast.LENGTH_LONG).show()
                viewModel.resetEmailStatus() // Reset status setelah notifikasi
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Button(
            onClick = { viewModel.sendLaporanRekapEmail() }, // Panggil fungsi ViewModel
            shape = MaterialTheme.shapes.extraLarge,
            // Nonaktifkan tombol saat sedang mengirim atau jika tidak ada laporan
            enabled = emailStatus != EmailSendStatus.SENDING && laporanList.isNotEmpty()
        ) {
            if (emailStatus == EmailSendStatus.SENDING) {
                // Tampilkan loading indicator di dalam tombol
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Filled.Send, contentDescription = "Kirim", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kirim Rekap Laporan")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (laporanList.isEmpty()) {
                Text("Belum ada riwayat laporan.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(laporanList) { laporan ->
                        LaporanItem(
                            laporan = laporan,
                            resep = resepInfo
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LaporanItem(laporan: LaporanModel, resep: ResepModel?) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large, // Sedikit lebih bulat
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Gambar Thumbnail di Kiri
            AsyncImage(
                model = laporan.imageUrl,
                contentDescription = "Foto Laporan",
                modifier = Modifier
                    .size(150.dp) // Ukuran lebih kecil
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Kolom untuk Teks di Sebelah Kanan Gambar
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Teks Nama Obat & Penyakit
                Text(
                    text = resep?.namaObat ?: "Nama Obat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = resep?.penyakit ?: "Penyakit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Baris untuk Tanggal dan Waktu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = laporan.timestamp?.let { formatDate(it, "MMMM d, yyyy") } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = laporan.timestamp?.let { formatDate(it, "HH:mm") } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDate(date: Date, pattern: String): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}