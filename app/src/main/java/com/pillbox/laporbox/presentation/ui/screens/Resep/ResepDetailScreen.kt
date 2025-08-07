package com.pillbox.laporbox.presentation.ui.screens.resep

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.pillbox.laporbox.R
import com.pillbox.laporbox.domain.models.LaporanModel
import com.pillbox.laporbox.presentation.ui.components.BackButtonIcon
import com.pillbox.laporbox.presentation.ui.components.FormHeader
import com.pillbox.laporbox.presentation.ui.navigation.Screen
import com.pillbox.laporbox.presentation.ui.theme.CardBlue
import com.pillbox.laporbox.presentation.ui.theme.TextHeading
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class ProgressStatus {
    REPORTED,
    MISSED,
    UPCOMING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResepDetailScreen(
    navController: NavController,
    resepId: String,
    viewModel: ResepDetailViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val resep = state.resep
    val context = LocalContext.current

    LaunchedEffect(state.emailStatus) {
        when(state.emailStatus) {
            EmailSendStatus.SUCCESS -> {
                Toast.makeText(context, "Email rekap berhasil dikirim!", Toast.LENGTH_SHORT).show()
                viewModel.resetEmailStatus()
            }
            EmailSendStatus.ERROR -> {
                Toast.makeText(context, "Gagal mengirim email.", Toast.LENGTH_SHORT).show()
                viewModel.resetEmailStatus()
            }
            else -> {}
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (resep == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Resep tidak ditemukan.")
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
            BackButtonIcon(onClick = { navController.popBackStack() })

            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(8.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                    .padding(horizontal = 36.dp)
                    .size(72.dp)
                    .align(Alignment.End)
                    .clickable {
                        navController.navigate("lapor_screen/${resepId}")
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Lapor Foto",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(top = 0.dp, bottom = 36.dp, start = 36.dp, end = 36.dp,)
            )
            {
                Image(
                    painter = painterResource(id = R.drawable.pill_image),
                    contentDescription = "Pill Image",
                    modifier = Modifier
                        .size(200.dp)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = resep.namaObat,
                    style = MaterialTheme.typography.displayMedium,
                    color  = TextHeading,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = "dr.${resep.namaDokter} - ${resep.emailFaskes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color  = TextHeading,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.padding(4.dp))

                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Text(
                            text = resep.frekuensiObat,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 30.dp),
                            color = Color.Black
                        )
                    }

                Spacer(modifier = Modifier.padding(8.dp))

                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBlue,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(16.dp),
                    ) {
                        val dosesPerDay = resep.frekuensiObat.firstOrNull()?.digitToIntOrNull() ?: 1

                        // 1. Hitung berapa dosis yang seharusnya sudah diminum sampai hari ini
                        val expectedDoses = calculateExpectedDoses(resep.createdAt, dosesPerDay)

                        // 2. Ambil total laporan aktual dari data resep
                        val actualReports = resep.totalLaporan.toInt()

                        // 3. Ambil semua laporan (tidak hanya hari ini)
                        val allReports = state.laporanList

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(expectedDoses) { index ->
                                // --- LOGIKA UTAMA DIPERBAIKI DI SINI ---
                                val hasReported = index < actualReports
                                val isMissed = index >= actualReports && index < expectedDoses

                                val status = when {
                                    hasReported -> ProgressStatus.REPORTED
                                    isMissed -> ProgressStatus.MISSED
                                    else -> ProgressStatus.UPCOMING
                                }

                                val laporan = if (hasReported) allReports.getOrNull(index) else null

                                ProgressIcon(
                                    status = status,
                                    laporan = laporan,
                                    onClick = {
                                        if (laporan?.timestamp != null) {
                                            val encodedUrl = URLEncoder.encode(laporan.imageUrl, StandardCharsets.UTF_8.toString())
                                            navController.navigate("${Screen.LaporanDetail.route}/$encodedUrl/${laporan.timestamp.time}")
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Sisa pil untuk dilaporkan: ${resep.jumlahObat - resep.totalLaporan}", modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.sendLaporanRekapEmail() },
                            enabled = state.emailStatus != EmailSendStatus.SENDING,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TextHeading
                            ),
                            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                        ) {
                            if (state.emailStatus == EmailSendStatus.SENDING) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = "Kirim Laporan")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kirim Laporan via Email")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressIcon(
    status: ProgressStatus,
    laporan: LaporanModel?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (status == ProgressStatus.REPORTED) Color(0xFFFC0404) else Color(0xFFBDBDBD))
            .clickable(enabled = status == ProgressStatus.REPORTED, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Gunakan when untuk menentukan tampilan berdasarkan status
        when (status) {
            ProgressStatus.REPORTED -> {
                Box{
                    SubcomposeAsyncImage(
                        model = laporan?.imageUrl,
                        contentDescription = "Gambar Laporan",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = { CircularProgressIndicator()},
                        error = { Icon(Icons.Default.Close, "Gagal memuat") }
                    )
                }
            }
            ProgressStatus.MISSED -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Terlewat",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            ProgressStatus.UPCOMING -> {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Akan Datang",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun calculateExpectedDoses(startDate: Date?, dosesPerDay: Int): Int {
    if (startDate == null) return 0

    val startCalendar = Calendar.getInstance().apply { time = startDate }
    val currentCalendar = Calendar.getInstance()

    startCalendar.set(Calendar.HOUR_OF_DAY, 0)
    startCalendar.set(Calendar.MINUTE, 0)
    startCalendar.set(Calendar.SECOND, 0)
    startCalendar.set(Calendar.MILLISECOND, 0)

    currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
    currentCalendar.set(Calendar.MINUTE, 0)
    currentCalendar.set(Calendar.SECOND, 0)
    currentCalendar.set(Calendar.MILLISECOND, 0)

    val diffInMillis = currentCalendar.timeInMillis - startCalendar.timeInMillis
    val daysPassed = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt() + 1

    return daysPassed * dosesPerDay
}