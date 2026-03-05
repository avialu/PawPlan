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

    // Last activity snapshots
    val lastVaccinationAt: Long? = null,
    val lastGroomingAt: Long? = null,
    val lastFeedAt: Long? = null,
    val lastFeedByName: String? = null,
    val lastWalkAt: Long? = null,
    val lastWalkByName: String? = null,

    // Daily WALK counter snapshot
    val walkCountDayStart: Long? = null,
    val walkCountToday: Int = 0,

    // ✅ Daily FEED counter snapshot
    val feedCountDayStart: Long? = null,
    val feedCountToday: Int = 0,

    // ✅ Pet configuration (defaults)
    val feedsPerDay: Int = 2,                 // 1-6
    val walksPerDay: Int = 2,                 // 1-6 (dogs only in UI, but keep default)
    val vaccinationEnabled: Boolean = true,
    val vaccinationEveryMonths: Int = 3,
    val groomingEnabled: Boolean = true,
    val groomingEveryMonths: Int = 4,
)