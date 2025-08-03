package com.thatwaz.timesquish

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(
        route = "home",
        label = "Timer",
        icon = Icons.Default.Timer
    )

    data object Week : BottomNavItem(
        route = "weekView",
        label = "Week",
        icon = Icons.Default.DateRange
    )

    data object Entries : BottomNavItem(
        route = "entries",
        label = "Entries",
        icon = Icons.Default.List
    )

    data object Settings : BottomNavItem(
        route = "settings",
        label = "Settings",
        icon = Icons.Default.Settings
    )
}

