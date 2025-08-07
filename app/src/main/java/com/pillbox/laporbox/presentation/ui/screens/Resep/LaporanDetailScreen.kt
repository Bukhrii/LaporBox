package com.pillbox.laporbox.presentation.ui.screens.resep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pillbox.laporbox.presentation.ui.components.BackButtonIcon
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanDetailScreen(
    navController: NavController,
    imageUrl: String,
    timestamp: String
) {
    val date = Date(timestamp.toLong())
    val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale("id", "ID"))
    val formattedDate = formatter.format(date)

    Column {
        BackButtonIcon(onClick = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                shape = CircleShape,
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 8.dp
                ),
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Foto Laporan",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = "Waktu Laporan",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

    }