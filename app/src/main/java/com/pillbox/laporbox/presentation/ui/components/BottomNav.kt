package com.pillbox.laporbox.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.navigation.Screen
import com.pillbox.laporbox.presentation.ui.theme.NavbarBlue
import com.pillbox.laporbox.presentation.ui.theme.TextHeading

@Composable
fun BottomNavigation(navController: NavController) { // Hapus parameter resepIdForLaporan
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hapus Box terluar, langsung gunakan Row
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .background(NavbarBlue)
            .height(70.dp)
    ) {
        IconButton(onClick = {
            if (currentRoute != Screen.Home.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        }) {
            Icon(
                painter = if (currentRoute == Screen.Home.route) painterResource(R.drawable.home_on) else painterResource(R.drawable.home_off),
                contentDescription = "Beranda",
                modifier = Modifier.size(35.dp)
            )
        }

        IconButton(onClick = {
            if (currentRoute != Screen.Profile.route) {
                navController.navigate(Screen.Profile.route)
            }
        }) {
            Icon(
                imageVector = if (currentRoute == Screen.Profile.route) Icons.Filled.Person else Icons.Outlined.Person,
                contentDescription = "Profil",
                tint = TextHeading,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}