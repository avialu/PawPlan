package com.avialu.pawplan.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(navController: NavController) {

    val items = listOf(
        MainRoutes.HOME,
        MainRoutes.PETS,
        MainRoutes.PROFILE
    )

    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { route ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        // ✅ behaves like "back" in the sense of clearing nested screens
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                label = {
                    Text(
                        when (route) {
                            MainRoutes.HOME -> "Home"
                            MainRoutes.PETS -> "Pets"
                            MainRoutes.PROFILE -> "Profile"
                            else -> route
                        }
                    )
                },
                icon = {}
            )
        }
    }
}