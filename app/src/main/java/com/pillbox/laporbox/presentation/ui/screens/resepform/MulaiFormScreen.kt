package com.pillbox.laporbox.presentation.ui.screens.resepform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.components.BackButtonIconOnprimary
import com.pillbox.laporbox.presentation.ui.components.Button
import com.pillbox.laporbox.presentation.ui.navigation.Screen

@Composable
fun MulaiFormScreen(
    navController: NavController,
    viewModel: FormResepViewModel
) {
    // Gunakan Box untuk menempatkan tombol kembali di pojok kiri atas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        BackButtonIconOnprimary(onClick = { navController.popBackStack() })

        // Gunakan satu Column untuk memusatkan semua konten lainnya
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp), // Beri padding horizontal
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Atur agar konten rata tengah vertikal
        ) {
            Text(
                text = stringResource(R.string.mulai_form),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.padding(24.dp))

            Button(
                text = stringResource(R.string.button_start),
                onClick = {
                    navController.navigate(Screen.FormDokter.route)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}