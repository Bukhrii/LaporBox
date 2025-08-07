package com.pillbox.laporbox.presentation.ui.screens.resepform

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MedicalInformation
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pillbox.laporbox.presentation.ui.components.FormHeader
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.navigation.Screen
import com.pillbox.laporbox.presentation.ui.theme.CardBlue

@Composable
fun FormDetailScreen(
    navController: NavController,
    viewModel: FormResepViewModel,
    onFinish: () -> Unit // Callback untuk kembali ke Beranda
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.saveStatus) {
        when (state.saveStatus) {
            SaveStatus.SUCCESS -> {
                Toast.makeText(context, "Resep berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                onFinish() // Panggil callback untuk navigasi
            }
            SaveStatus.ERROR -> {
                Toast.makeText(context, "Gagal menambahkan resep: ${state.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FormHeader(onBackClick = { navController.popBackStack() })

            Text(
                text = "Berikut Detail Resep Obatmu!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                lineHeight = MaterialTheme.typography.displayMedium.fontSize * 1,
                modifier = Modifier.padding(horizontal = 26.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        CardBlue, contentColor = CardBlue
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(16.dp),

                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.user_profile),
                            contentDescription = "Placeholder Foto",
                            modifier = Modifier.size(52.dp)
                        )
                        
                        Spacer(modifier = Modifier.padding(6.dp))
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {Text(
                            text = "dr. ${state.namaDokter}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Email Faskes",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.padding(2.dp))

                                Text(
                                    text = state.emailFaskes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(4.dp))

                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        CardBlue, contentColor = CardBlue
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(16.dp),

                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MedicalInformation,
                                contentDescription = "Icon Obat",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.padding(4.dp))

                            Text(
                                text = "Informasi Medis",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.padding(6.dp))

                        Text(
                            text = "Penyakit yang sedang diobati",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ElevatedCard(
                            colors = CardDefaults.cardColors(
                                Color.White, contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.elevatedCardElevation(16.dp),
                        ) {
                            Text(
                                state.penyakit,
                                color = Color.Black,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = DividerDefaults.Thickness,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.pill),
                                contentDescription = "Icon Obat",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.padding(4.dp))

                            Text(
                                text = "Obat dan Dosis",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ElevatedCard(
                            colors = CardDefaults.cardColors(
                                Color.White, contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.elevatedCardElevation(16.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp).fillMaxWidth()
                            ) {
                                Text(
                                    state.namaObat,
                                    color = Color.Black,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${state.frekuensiObat} | ${state.aturanMakan}",
                                    color = Color.Black,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Spacer(modifier = Modifier.padding(4.dp))

                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        CardBlue, contentColor = CardBlue
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(16.dp),

                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.user_profile),
                            contentDescription = "Placeholder Foto",
                            modifier = Modifier.size(52.dp)
                        )

                        Spacer(modifier = Modifier.padding(6.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = "Icon Keluarga",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.padding(4.dp))

                                Text(
                                    text = "Anggota Keluarga",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.padding(6.dp))

                            Text (
                                text = state.namaKeluarga,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Email Keluarga",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.padding(2.dp))

                                Text(
                                    text = state.emailKeluarga,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (state.saveStatus == SaveStatus.LOADING) {
                    CircularProgressIndicator()
                } else {
                    ElevatedButton(
                        onClick = { viewModel.saveOrUpdateResep() },
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
            }

            Spacer(modifier = Modifier.padding(10.dp))
        }
}