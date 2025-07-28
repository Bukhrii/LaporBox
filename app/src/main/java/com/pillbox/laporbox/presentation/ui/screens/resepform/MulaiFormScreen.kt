package com.pillbox.laporbox.presentation.ui.screens.resepform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.components.BackButtonIconOnprimary
import com.pillbox.laporbox.presentation.ui.components.Button
import com.pillbox.laporbox.presentation.ui.navigation.Screen

@Composable
fun MulaiFormScreen(
    navController: NavController,
    viewModel: FormResepViewModel
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .verticalScroll(rememberScrollState())
    ) {

        BackButtonIconOnprimary(onClick = { navController.popBackStack() })

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
            .fillMaxSize()
            .padding(50.dp)) {
            Text(
                text = stringResource( R.string.mulai_form),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.padding(16.dp))

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