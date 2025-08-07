package com.pillbox.laporbox.presentation.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

class Gradient {
    companion object {
        val appGradientBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2260FF).copy(alpha = 1f),
                Color(0xFFFFFFFF),
                Color(0xFFFFFFFF)
            )
        )
    }
}
