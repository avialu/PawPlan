package com.avialu.pawplan.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun progressColor(done: Int, target: Int): Color {
    if (target <= 0) return MaterialTheme.colorScheme.onSurfaceVariant
    return when {
        done <= 0 -> MaterialTheme.colorScheme.error
        done < target -> Color(0xFFFFC107) // amber
        else -> Color(0xFF2E7D32)          // green
    }
}