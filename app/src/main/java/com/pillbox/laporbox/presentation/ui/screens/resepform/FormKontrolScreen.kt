package com.pillbox.laporbox.presentation.ui.screens.resepform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.components.DateInput
import com.pillbox.laporbox.presentation.ui.components.convertMillisToDate
import com.pillbox.laporbox.presentation.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormKontrolScreen(
    navController: NavController,
    viewModel: FormResepViewModel
) {
    val state by viewModel.uiState.collectAsState()

    val lastControlDateState = rememberDatePickerState()
    val nextControlDateState = rememberDatePickerState()

    LaunchedEffect(lastControlDateState.selectedDateMillis) {
        lastControlDateState.selectedDateMillis?.let { millis ->
            viewModel.onTanggalKontrolTerakhirChange(convertMillisToDate(millis))
        }
    }

    LaunchedEffect(nextControlDateState.selectedDateMillis) {
        nextControlDateState.selectedDateMillis?.let { millis ->
            viewModel.onTanggalKontrolBerikutnyaChange(convertMillisToDate(millis))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Form Kontrol") },
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
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.kontrol_form_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.kontrol_form_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            DateInput(
                label = "Jadwal terakhir kontrol",
                datePickerState = lastControlDateState
            )

            Spacer(modifier = Modifier.height(16.dp))

            DateInput(
                label = "Jadwal kontrol terdekat berikutnya",
                datePickerState = nextControlDateState
            )

            Spacer(modifier = Modifier.padding(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { navController.popBackStack() }) {
                    Text("Sebelumnya")
                }
                Button(onClick = { navController.navigate(Screen.FormPenyakit.route) }) {
                    Text("Selanjutnya")
                }
            }
        }
    }
}