package com.avialu.pawplan.data.models

data class PetActivity(
    val id: String = "",
    val type: String = "",          // נשמור כמחרוזת כדי שיהיה פשוט בפיירסטור
    val note: String? = null,
    val timestamp: Long = 0L,
    val createdBy: String = ""
)