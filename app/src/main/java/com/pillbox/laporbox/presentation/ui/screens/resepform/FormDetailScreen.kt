package com.pillbox.laporbox.presentation.ui.screens.resepform

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pillbox.laporbox.presentation.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDetailScreen(
    navController: NavController,
    viewModel: FormResepViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(state.saveStatus) {
        when (state.saveStatus) {
            SaveStatus.SUCCESS -> {
                Toast.makeText(context, "Resep berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
            SaveStatus.ERROR -> {
                Toast.makeText(context, "Gagal menambahkan resep.", Toast.LENGTH_SHORT).show()
            }
            else -> {  }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Berikut Detail Resep Obatmu!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.shapes.large
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryItem("Nama Dokter", state.namaDokter)
                SummaryItem("Email Faskes", state.emailFaskes)
                SummaryItem(
                    label = "Penyakit yang diobati",
                    value = if(state.penyakit == "Lainnya") state.penyakitLainnya else state.penyakit
                )
                SummaryItem("Nama Anggota Keluarga", state.namaKeluarga)
                SummaryItem("Email Anggota Keluarga", state.emailKeluarga)
                SummaryItem("Nama Obat", state.namaObat)
                SummaryItem("Frekuensi Obat", state.frekuensiObat)
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.saveStatus == SaveStatus.LOADING) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { viewModel.saveOrUpdateResep() },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Tambahkan Resep")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}