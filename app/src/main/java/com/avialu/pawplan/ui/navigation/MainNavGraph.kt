package com.avialu.pawplan.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.avialu.pawplan.ui.screens.main.HomeScreen
import com.avialu.pawplan.ui.screens.main.ProfileScreen
import com.avialu.pawplan.ui.screens.pets.AddPetScreen
import com.avialu.pawplan.ui.screens.pets.EditPetScreen
import com.avialu.pawplan.ui.screens.pets.PetProfileScreen
import com.avialu.pawplan.ui.screens.pets.PetsScreen

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
                HomeScreen(navController = rootNavController)
            }

            // Pets list (tab)
            composable(MainRoutes.PETS) {
                PetsScreen(navController = navController)
            }

            // Pets add
            composable(PetsRoutes.ADD) {
                AddPetScreen(navController = navController)
            }

            // Pets profile
            composable(
                route = PetsRoutes.PROFILE,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStack ->
                val petId = backStack.arguments?.getString("petId")!!
                PetProfileScreen(navController = navController, petId = petId)
            }

            // Pets edit
            composable(
                route = PetsRoutes.EDIT,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStack ->
                val petId = backStack.arguments?.getString("petId")!!
                EditPetScreen(navController = navController, petId = petId)
            }

            composable(MainRoutes.PROFILE) {
                ProfileScreen(rootNavController = rootNavController)
            }
        }
    }
}