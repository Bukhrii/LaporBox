package com.pillbox.laporbox.presentation.ui.screens.resepform

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.components.FormHeader
import com.pillbox.laporbox.presentation.ui.navigation.Screen
import com.pillbox.laporbox.presentation.ui.theme.TextHeading
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPengingatScreen(
    navController: NavController,
    viewModel: FormResepViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Image(
            painter = painterResource(R.drawable.page7),
            contentDescription = "Onboarding Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
        )

        FormHeader(onBackClick = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 50.dp)
                .verticalScroll(rememberScrollState())) {

            Spacer(modifier = Modifier.height(74.dp))
            Text(
                text = "Jam berapa anda makan?",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                lineHeight = MaterialTheme.typography.displayMedium.fontSize * 1.4,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                "Anda memilih dosis ${state.frekuensiObat}. Silakan atur waktu minum obat untuk ${state.namaObat}.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(62.dp))

            state.jadwalPengingat.forEachIndexed { index, time ->
                Text(
                    text = "Waktu Makan ${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextHeading,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                ReminderTimeSelector(
                    label = "Pilih waktu makan ${index + 1}",
                    selectedTime = time,
                    onTimeSelected = { newTime ->
                        viewModel.onTimeChanged(index, newTime)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }


            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.padding(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ElevatedButton(
                    onClick = { navController.navigate(Screen.FormDetail.route) },
                    elevation = ButtonDefaults.buttonElevation(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 60.dp)
                ) {
                    Text(
                        "Buat Pengingat  >",
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.padding(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeSelector(
    label: String,
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    val timeOptions = remember {
        (0..23).flatMap { hour ->
            listOf(
                String.format(Locale.ROOT, "%02d:00", hour),
                String.format(Locale.ROOT, "%02d:30", hour)
            )
        }
    }
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }) {
        OutlinedTextField(
            value = selectedTime,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                unfocusedLabelColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            timeOptions.forEach { time ->
                DropdownMenuItem(
                    text = { Text(time) },
                    onClick = {
                        onTimeSelected(time)
                        isExpanded = false
                    }
                )
            }
        }
    }
}