package com.thatwaz.timesquish

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem("home", "Timer", Icons.Default.Timer)
    data object Week : BottomNavItem("weekView", "Week", Icons.Default.DateRange)
    data object Entries : BottomNavItem("entries", "Entries", Icons.Default.List)
}
