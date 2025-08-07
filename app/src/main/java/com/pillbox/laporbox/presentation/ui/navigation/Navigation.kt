package com.pillbox.laporbox.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.pillbox.laporbox.presentation.ui.screens.auth.AuthState
import com.pillbox.laporbox.presentation.ui.screens.auth.AuthViewModel
import com.pillbox.laporbox.presentation.ui.screens.auth.LoginScreen
import com.pillbox.laporbox.presentation.ui.screens.auth.SignupScreen
import com.pillbox.laporbox.presentation.ui.screens.home.HomeNavGraph
import com.pillbox.laporbox.presentation.ui.screens.home.HomeViewModel
import com.pillbox.laporbox.presentation.ui.screens.onboarding.OnboardingScreen
import com.pillbox.laporbox.presentation.ui.screens.onboarding.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel


const val AUTH_GRAPH_ROUTE = "auth_graph"
const val MAIN_GRAPH_ROUTE = "main_graph"
const val RESEP_ROUTE = "resep_flow"

@Composable
fun RootNavGraph(
    authViewModel: AuthViewModel = koinViewModel(),
    onboardingViewModel: OnboardingViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val hasCompletedOnboardingState by onboardingViewModel.onboardingCompleted.collectAsState(initial = null)
    val authState by authViewModel.authState.observeAsState()

    val localHasCompletedOnboarding = hasCompletedOnboardingState

    if (localHasCompletedOnboarding == null || authState == null) {
        return // Tunggu state dimuat, splash screen masih aktif
    }

    val startDestination = when {
        !localHasCompletedOnboarding -> Screen.Onboarding.route
        authState is AuthState.Authenticated -> MAIN_GRAPH_ROUTE
        else -> AUTH_GRAPH_ROUTE
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(AUTH_GRAPH_ROUTE) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        authNavGraph(navController = navController)

        // Setelah login, seluruh kontrol diserahkan ke HomeNavGraph
        composable(MAIN_GRAPH_ROUTE) {
            HomeNavGraph(rootNavController = navController)
        }
    }
}

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        route = AUTH_GRAPH_ROUTE,
        startDestination = Screen.LoginScreen.route
    ) {
        composable(Screen.LoginScreen.route) {
            val homeViewModel: HomeViewModel = koinViewModel()
            LoginScreen(
                homeViewModel = homeViewModel,
                onLoginSuccess = {
                    navController.navigate(MAIN_GRAPH_ROUTE) {
                        popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignupScreen.route)
                }
            )
        }
        composable(Screen.SignupScreen.route) {
            SignupScreen(
                onSignUpSuccessToLogin = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}