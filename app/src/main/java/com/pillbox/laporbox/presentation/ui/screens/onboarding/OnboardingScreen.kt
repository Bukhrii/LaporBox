package com.pillbox.laporbox.presentation.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.pillbox.laporbox.domain.models.OnboardingModel
import com.pillbox.laporbox.presentation.ui.theme.BrandBlue
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinish: () -> Unit,
) {

    val pages = listOf(
        OnboardingModel.FirstPages,
        OnboardingModel.SecondPages,
        OnboardingModel.ThirdPages,
        OnboardingModel.FourthPages,
        OnboardingModel.FifthPages,
    )
    val pagerState = rememberPagerState(initialPage = 0) { pages.size }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            BrandBlue.copy(alpha = 0.95f),
            Color.White
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            val currentPage = pages[pageIndex]

            when (pageIndex) {
                0 -> WelcomePage(onboardingModel = currentPage)
                1 -> SecondPage(onboardingModel = currentPage)
                2 -> ThirdPage(onboardingModel = currentPage)
                3 -> FourthPage(onboardingModel = currentPage)
                4 -> StartPage(onboardingModel = currentPage)
            }
        }

        BottomNavigator(
            pagerState = pagerState,
            totalPages = pages.size,
            onFinish = {
                viewModel.finishOnboarding()
                onFinish()
            },
            onNavigateToLogin = {
                viewModel.finishOnboarding()
                onFinish()
            }
        )
    }
}

