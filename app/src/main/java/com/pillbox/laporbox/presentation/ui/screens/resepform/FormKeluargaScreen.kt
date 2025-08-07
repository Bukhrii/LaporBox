package com.pillbox.laporbox.presentation.ui.screens.resepform

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.components.FormHeader
import com.pillbox.laporbox.presentation.ui.navigation.Screen
import com.pillbox.laporbox.presentation.ui.theme.CardBlue
import com.pillbox.laporbox.presentation.ui.theme.TextHeading

@Composable
fun FormKeluargaScreen(
    navController: NavController,
    viewModel: FormResepViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize()) {
                FormHeader(onBackClick = { navController.popBackStack() })
                Text(
                    text = "Keluarga Pendamping",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    lineHeight = MaterialTheme.typography.displayMedium.fontSize * 1,
                    modifier = Modifier.padding(horizontal = 26.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.hand),
                    contentDescription = "Hand Background",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ElevatedCard(
                colors = CardDefaults.cardColors(
                    CardBlue, contentColor = CardBlue
                ),
                shape = RoundedCornerShape(topEnd = 40.dp, topStart = 40.dp),
                elevation = CardDefaults.elevatedCardElevation(16.dp),

                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp)
                ) {
                    Spacer(modifier = Modifier.padding(16.dp))
                    Text(
                        text = stringResource(R.string.keluarga_form_name),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextHeading,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.namaKeluarga,
                        onValueChange = { viewModel.onNamaKeluargaChange(it) },
                        label = { Text(stringResource(R.string.keluarga_form_hint_name)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.keluarga_form_email),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextHeading,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.emailKeluarga,
                        onValueChange = { viewModel.onEmailKeluargaChange(it) },
                        label = { Text(stringResource(R.string.keluarga_form_hint_email)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.padding(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ElevatedButton(
                            onClick = { navController.navigate(Screen.FormObat.route) },
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
                                "Selanjutnya  >",
                                color = Color.White,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}