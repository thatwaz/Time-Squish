package com.thatwaz.timesquish.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector

enum class ListMode { NORMAL, SUBMIT, SQUISH, EDIT }

@Composable
fun ListMode.label(): String = when (this) {
    ListMode.NORMAL -> ""
    ListMode.SUBMIT -> "Submit Mode"
    ListMode.SQUISH -> "Squish Mode"
    ListMode.EDIT   -> "Edit Mode"
}

@Composable
fun ListMode.icon(): ImageVector = when (this) {
    ListMode.NORMAL -> Icons.Default.Check // unused
    ListMode.SUBMIT -> Icons.Default.DoneAll
    ListMode.SQUISH -> Icons.Default.Merge // or any icon you prefer
    ListMode.EDIT   -> Icons.Default.Edit
}

fun Color.darken(factor: Float): Color {
    return copy(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f)
    )
}

@Composable
fun ListMode.bgColor(): Color {
    val cs = MaterialTheme.colorScheme
    val base = cs.background
    val target = when (this) {
        ListMode.NORMAL -> base
        ListMode.SUBMIT -> cs.secondaryContainer
        ListMode.SQUISH -> cs.primaryContainer
        ListMode.EDIT   -> cs.tertiaryContainer
    }
    return lerp(base, target, if (this == ListMode.NORMAL) 0f else 0.6f)
        .darken(0.85f) // 0.85f makes it 15% darker
}


@Composable
fun ListMode.bannerColor(): Color = when (this) {
    ListMode.NORMAL -> Color.Transparent
    ListMode.SUBMIT -> MaterialTheme.colorScheme.secondaryContainer
    ListMode.SQUISH -> MaterialTheme.colorScheme.primaryContainer
    ListMode.EDIT   -> MaterialTheme.colorScheme.tertiaryContainer
}