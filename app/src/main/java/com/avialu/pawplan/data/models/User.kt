package com.avialu.pawplan.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val activeHouseholdId: String? = null,
    val createdAt: Long = 0L
)