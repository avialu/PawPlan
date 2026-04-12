package com.avialu.pawplan.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun progressColor(current: Int, target: Int): Color {

    val t = target.coerceAtLeast(1)

    return when {
        current >= t -> Color(0xFF2E7D32)   // green
        current > 0  -> Color(0xFFF9A825)   // yellow
        else         -> Color(0xFFC62828)   // red
    }
}