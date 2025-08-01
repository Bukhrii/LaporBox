package com.pillbox.laporbox.presentation.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.pillbox.laporbox.domain.models.OnboardingModel

@Composable
fun OnboardingGraphic(onboardingModel: OnboardingModel) {
    // Bungkus semua konten di dalam Column yang bisa di-scroll
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // <-- KUNCI PERBAIKANNYA DI SINI
            .padding(horizontal = 24.dp), // Beri padding agar tidak terlalu mepet
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Pusatkan konten jika muat dalam layar
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(onboardingModel.image))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(300.dp)
        )

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = stringResource(id = onboardingModel.title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        onboardingModel.description?.let { descriptionResId ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = descriptionResId),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // Spacer di bawah agar ada ruang saat scroll sampai akhir
        Spacer(modifier = Modifier.height(32.dp))
    }
}