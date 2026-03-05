package com.avialu.pawplan.data.models

data class PetActivity(
    val id: String = "",
    val type: String = "",
    val note: String? = null,
    val timestamp: Long = 0L,
    val createdBy: String = "",
    val createdByName: String? = null, // ✅ add this
)