package com.pillbox.laporbox.presentation.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pillbox.laporbox.presentation.ui.navigation.RESEP_ROUTE
import com.pillbox.laporbox.presentation.ui.navigation.Screen

@Composable
fun BottomNavigation(navController: NavController, resepIdForLaporan: String) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(contentAlignment = Alignment.Center,modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
                .background(Color.White)
                .height(70.dp)) {
            IconButton(onClick = {
                if (currentRoute != Screen.Home.route) {
                    navController.navigate(Screen.Home.route)
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    tint = if (currentRoute == Screen.Home.route) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(35.dp)
                )
            }

            IconButton(onClick = {
                if (currentRoute != Screen.Profile.route) {
                    navController.navigate(Screen.Profile.route)
                }
            }) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = if (currentRoute == Screen.Profile.route) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(35.dp)
                )
            }

        }

        FloatingActionButton(onClick = {
            if (resepIdForLaporan.isBlank()) {
                navController.navigate(RESEP_ROUTE)
            }
            else {
                Toast.makeText(context, "Cuman 1 resep untuk Hipertensi", Toast.LENGTH_SHORT).show()
            }
        },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.offset(y = (-40).dp)
        ) {
            Icon(imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White)
        }
    }
}