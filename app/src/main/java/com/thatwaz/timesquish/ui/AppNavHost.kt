package com.thatwaz.timesquish.ui


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thatwaz.timesquish.ui.screens.ActiveSessionScreen
import com.thatwaz.timesquish.ui.screens.HomeScreen
import com.thatwaz.timesquish.ui.screens.ManualEntryScreen
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
                onNavigateToManualEntry = { navController.navigate("manualEntry") },
                onNavigateToActiveSession = { navController.navigate("activeSession") }
            )
        }

        composable("weekView") {
            WeekViewScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditEntry = { entryId ->
                    navController.navigate("manualEntry?entryId=$entryId")
                }
            )
        }

        composable("activeSession") {
            ActiveSessionScreen(
                onClockOut = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "manualEntry?entryId={entryId}",
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getInt("entryId")?.takeIf { it != -1 }
            ManualEntryScreen(
                entryId = entryId,
                onEntryAdded = { navController.popBackStack() }
            )
        }
    }
}


