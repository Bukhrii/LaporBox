package com.pillbox.laporbox.presentation.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("Onboarding_Screen")
    object LoginScreen : Screen("Login_Screen")
    object SignupScreen : Screen("Signup_Screen")
    object Home : Screen("Home_Screen")
    object MulaiForm : Screen("MulaiForm_Screen")
    object FormDokter : Screen("FormDokter_Screen")
    object FormKontrol : Screen("FormKontrol_Screen")
    object FormPenyakit : Screen("FormPenyakit_Screen")
    object FormKeluarga : Screen("FormKeluarga_Screen")
    object FormObat : Screen("FormObat_Screen")
    object FormDetail : Screen("FormDetail_Screen")
    object Profile : Screen("Profile_Screen")
}