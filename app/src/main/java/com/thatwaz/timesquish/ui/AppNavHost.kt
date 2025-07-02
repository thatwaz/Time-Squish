package com.thatwaz.timesquish.ui


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thatwaz.timesquish.ui.screens.ActiveSessionScreen
import com.thatwaz.timesquish.ui.screens.HomeScreen
import com.thatwaz.timesquish.ui.screens.WeekViewScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToWeekView = { navController.navigate("weekView") },
                onNavigateToManualEntry = { /* Later */ },
                onNavigateToActiveSession = { navController.navigate("activeSession") }
            )
        }
        composable("weekView") {
            WeekViewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("activeSession") {
            ActiveSessionScreen(
                onClockOut = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
            )
        }

        // You can add manualEntry here later
    }
}
