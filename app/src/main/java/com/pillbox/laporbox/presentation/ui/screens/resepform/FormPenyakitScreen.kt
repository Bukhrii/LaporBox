package com.pillbox.laporbox.presentation.ui.screens.resepform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPenyakitScreen(
    navController: NavController,
    viewModel: FormResepViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(R.string.penyakit_form_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.penyakit_form_description), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            // Opsi Pilihan
//            SelectableOptionCard(
//                text = stringResource(R.string.tbc),
//                isSelected = state.penyakit == "TBC",
//                onClick = { viewModel.onPenyakitSelected("TBC") }
//            )
//            Spacer(modifier = Modifier.height(16.dp))
            SelectableOptionCard(
                text = stringResource(R.string.hipertensi),
                isSelected = state.penyakit == "Hipertensi",
                onClick = { viewModel.onPenyakitSelected("Hipertensi") }
            )
//            Spacer(modifier = Modifier.height(16.dp))
//            SelectableOptionCard(
//                text = stringResource(R.string.lainnya),
//                isSelected = state.penyakit == "Lainnya",
//                onClick = { viewModel.onPenyakitSelected("Lainnya") }
//            )
//
//            // Input manual jika "Lainnya" dipilih
//            AnimatedVisibility(visible = state.penyakit == "Lainnya") {
//                Column(modifier = Modifier.padding(top = 24.dp)) {
//                    Text("Silakan isi secara manual jika jenis penyakitmu belum ada di daftar")
//                    Spacer(modifier = Modifier.height(8.dp))
//                    OutlinedTextField(
//                        value = state.penyakitLainnya,
//                        onValueChange = { viewModel.onPenyakitLainnyaChange(it) },
//                        modifier = Modifier.fillMaxWidth(),
//                        placeholder = { Text("Ketikkan di sini") }
//                    )
//                }
//            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { navController.popBackStack() }) {
                    Text(stringResource(R.string.button_back))
                }
                Button(onClick = { navController.navigate(Screen.FormKeluarga.route) }) {
                    Text(stringResource(R.string.button_next))
                }
            }
        }
    }
}

// Composable terpisah untuk kartu pilihan
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = text, color = contentColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}