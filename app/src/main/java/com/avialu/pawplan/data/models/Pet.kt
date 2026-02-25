package com.avialu.pawplan.data.models

data class Pet(
    val id: String = "",
    val type: String = "dog",
    val name: String = "",
    val breed: String = "",
    val birthYear: Int? = null,
    val photoUrl: String? = null,
    val createdAt: Long = 0L,
    val createdBy: String = "",
    val lastVaccinationAt: Long? = null,
    val lastGroomingAt: Long? = null,
    val lastFeedAt: Long? = null,
    val lastFeedByName: String? = null,
    val lastWalkAt: Long? = null,
    val lastWalkByName: String? = null,
    val walkCountDayStart: Long? = null,
    val walkCountToday: Int = 0,
)