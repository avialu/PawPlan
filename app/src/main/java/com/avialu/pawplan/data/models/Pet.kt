package com.avialu.pawplan.data.models

data class Pet(
    val id: String = "",
    val type: String = "dog",          // dog/cat/...
    val name: String = "",
    val breed: String = "",
    val birthYear: Int? = null,
    val photoUrl: String? = null,
    val createdAt: Long = 0L,
    val createdBy: String = ""
)