package com.thatwaz.timesquish.ui


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
                onNavigateToManualEntry = { /* TODO: implement later */ }
            )
        }
        composable("weekView") {
            WeekViewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // You can add manualEntry here later
    }
}
