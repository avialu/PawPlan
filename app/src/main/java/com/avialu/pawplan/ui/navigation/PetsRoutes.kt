package com.avialu.pawplan.ui.navigation

object PetsRoutes {
    const val LIST = MainRoutes.PETS          // "main_pets"
    const val ADD = "pet_add"
    const val PROFILE = "pet_profile/{petId}"
    const val EDIT = "pet_edit/{petId}"

    fun profile(petId: String) = "pet_profile/$petId"
    fun edit(petId: String) = "pet_edit/$petId"
}