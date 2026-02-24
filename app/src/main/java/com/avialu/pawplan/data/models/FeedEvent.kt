package com.avialu.pawplan.data.models

data class FeedEvent(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val type: String = "",
    val note: String? = null,
    val timestamp: Long = 0L,
    val createdBy: String = ""
)