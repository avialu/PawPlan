package com.avialu.pawplan.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.avialu.pawplan.ui.screens.main.HomeScreen
import com.avialu.pawplan.ui.screens.main.PetsScreen
import com.avialu.pawplan.ui.screens.main.ProfileScreen
import com.avialu.pawplan.ui.screens.pets.AddActivityScreen
import com.avialu.pawplan.ui.screens.pets.AddPetScreen
import com.avialu.pawplan.ui.screens.pets.EditPetScreen
import com.avialu.pawplan.ui.screens.pets.PetFormScreen
import com.avialu.pawplan.ui.screens.pets.PetProfileScreen

@Composable
fun MainNavGraph(rootNavController: NavController) {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = MainRoutes.HOME,
            modifier = Modifier.padding(padding)
        ) {

            composable(MainRoutes.HOME) {
                HomeScreen(
                    navController = navController,          // פנימי: לפרופיל חיה וכו'
                    rootNavController = rootNavController   // חיצוני: ל-Onboarding
                )
            }

            composable(MainRoutes.PETS) {
                PetsScreen(
                    navController = navController,
                    rootNavController = rootNavController
                )
            }

            // Add pet
            composable(PetsRoutes.ADD) {
                PetFormScreen(navController = navController)
            }

            // Pet profile
            composable(
                route = PetsRoutes.PROFILE,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStack ->
                val petId = backStack.arguments?.getString("petId") ?: return@composable
                PetProfileScreen(navController = navController, petId = petId)
            }

            // Add activity
            composable(
                route = PetsRoutes.ADD_ACTIVITY,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStack ->
                val petId = backStack.arguments?.getString("petId") ?: return@composable
                AddActivityScreen(navController = navController, petId = petId)
            }

            // Edit pet
            composable(
                route = PetsRoutes.EDIT,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStack ->
                val petId = backStack.arguments?.getString("petId")
                PetFormScreen(
                    navController = navController,
                    petId = petId
                )
            }

            // Profile tab (פה כן צריך root בשביל logout)
            composable(MainRoutes.PROFILE) {
                ProfileScreen(rootNavController = rootNavController)
            }
        }
    }
}