package com.pillbox.laporbox.presentation.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.presentation.ui.navigation.RESEP_ROUTE
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeResepScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val resepList by viewModel.reseps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
//        Text(
//            text = "Tambah resep obat",
//            modifier = Modifier.clickable {
//                navController.navigate(RESEP_ROUTE)
//            },
//            color = MaterialTheme.colorScheme.primary,
//            fontWeight = FontWeight.Bold
//        )
//        Spacer(modifier = Modifier.height(16.dp))

        if (resepList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada resep. Silakan tambahkan.")
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items = resepList, key = { it.id }) { resep ->
                    ResepItem(
                        resep = resep,
                        onEditClick = {
                            navController.navigate("$RESEP_ROUTE?resepId=${resep.id}")
                        },
                        onLaporClick = {
                            navController.navigate("lapor_screen/${resep.id}")
                        },
                        onDeleteClick = {
                            viewModel.deleteResep(resep)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResepItem(
    resep: ResepModel,
    onEditClick: () -> Unit,
    onLaporClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = resep.namaObat,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = resep.frekuensiObat,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Text("Nama Dokter", style = MaterialTheme.typography.labelMedium)
            Text(resep.namaDokter, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Nama Anggota Keluarga", style = MaterialTheme.typography.labelMedium)
            Text(resep.namaKeluarga, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(onClick = onLaporClick) {
                    Text("Lapor Foto")
                }


                OutlinedButton(onClick = onDeleteClick) {
                    Text("Hapus")
                }
                Spacer(modifier = Modifier.width(8.dp))

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
            }
        }
    }
}