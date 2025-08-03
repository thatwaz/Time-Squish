package com.thatwaz.timesquish.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thatwaz.timesquish.BottomNavItem
import com.thatwaz.timesquish.ui.screens.ActiveSessionScreen
import com.thatwaz.timesquish.ui.screens.HomeScreen
import com.thatwaz.timesquish.ui.screens.ManualEntryScreen
import com.thatwaz.timesquish.ui.screens.UserSettingsScreen
import com.thatwaz.timesquish.ui.screens.WeekViewScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    ) {
        // === BOTTOM NAV SCREENS ===
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onNavigateToActiveSession = { navController.navigate("activeSession") }
            )
        }

        composable(BottomNavItem.Week.route) {
            WeekViewScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditEntry = { entryId ->
                    navController.navigate("manualEntry?entryId=$entryId")
                }
            )
        }

        composable(BottomNavItem.Entries.route) {
            ManualEntryScreen(
                onEntryAdded = { navController.popBackStack() }
            )
        }

        composable(BottomNavItem.Settings.route) {
            UserSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // === NON-BOTTOM NAV ROUTES ===

        composable("activeSession") {
            ActiveSessionScreen(
                onClockOut = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(BottomNavItem.Home.route) { inclusive = true }
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


//@Composable
//fun AppNavHost(
//    navController: NavHostController,
//    modifier: Modifier = Modifier
//) {
//    NavHost(
//        navController = navController,
//        startDestination = "home",
//        modifier = modifier
//    ) {
//        composable("home") {
//            HomeScreen(
//                onNavigateToWeekView = { navController.navigate("weekView") },
//                onNavigateToManualEntry = { navController.navigate("manualEntry") },
//                onNavigateToActiveSession = { navController.navigate("activeSession") },
//                onNavigateToReminderSettings = { navController.navigate("reminderSettings") }
//            )
//        }
//
//        composable("weekView") {
//            WeekViewScreen(
//                onNavigateBack = { navController.popBackStack() },
//                onEditEntry = { entryId ->
//                    navController.navigate("manualEntry?entryId=$entryId")
//                }
//            )
//        }
//
//        composable("activeSession") {
//            ActiveSessionScreen(
//                onClockOut = {
//                    navController.navigate("home") {
//                        popUpTo("home") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        composable(
//            route = "manualEntry?entryId={entryId}",
//            arguments = listOf(
//                navArgument("entryId") {
//                    type = NavType.IntType
//                    defaultValue = -1
//                }
//            )
//        ) { backStackEntry ->
//            val entryId = backStackEntry.arguments?.getInt("entryId")?.takeIf { it != -1 }
//            ManualEntryScreen(
//                entryId = entryId,
//                onEntryAdded = { navController.popBackStack() }
//            )
//        }
//
//        composable("reminderSettings") {
//            ReminderSettingsScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//    }
//}
//
//
