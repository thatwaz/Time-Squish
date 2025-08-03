package com.thatwaz.timesquish.ui.screens




import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.R
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onNavigateToActiveSession: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val isClockedIn = activeSession != null
    val defaultHourlyPay by viewModel.hourlyPayFlow.collectAsState(initial = 0.0)

    val scope = rememberCoroutineScope()

    // Auto-navigate if already clocked in
    LaunchedEffect(activeSession) {
        if (isClockedIn) {
            onNavigateToActiveSession()
        }
    }

    // Animate image swap between squished/unsquished
    var isSquished by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(700L)
            isSquished = !isSquished
        }
    }

    val imageRes = if (isSquished) R.drawable.unsquished_clock else R.drawable.squished_clock3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Time Squish",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Stick figure squishing a clock",
            modifier = Modifier
                .size(200.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isClockedIn) "You are clocked in." else "You are clocked out.",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isClockedIn) {
                    viewModel.clockOut()
                } else {
                    scope.launch {
                        viewModel.clockIn(defaultHourlyPay)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isClockedIn) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (isClockedIn) "Clock Out" else "Clock In",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

//        Button(
//            onClick = onNavigateToUserSettings,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            shape = RoundedCornerShape(12.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary
//            )
//        ) {
//            Text("User Settings", style = MaterialTheme.typography.titleMedium)
//        }
    }
}


//
//@Composable
//fun HomeScreen(
//    viewModel: TimeEntryViewModel = hiltViewModel(),
//    onNavigateToActiveSession: () -> Unit,
//    onNavigateToUserSettings: () -> Unit
//) {
//    val activeSession by viewModel.activeSession.collectAsState()
//    val isClockedIn = activeSession != null
//
//    // Auto-navigate if already clocked in
//    LaunchedEffect(activeSession) {
//        if (isClockedIn) {
//            onNavigateToActiveSession()
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = if (isClockedIn) "You are clocked in." else "You are clocked out.",
//            style = MaterialTheme.typography.headlineSmall
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(
//            onClick = {
//                if (isClockedIn) viewModel.clockOut() else viewModel.clockIn()
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            shape = RoundedCornerShape(12.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = if (isClockedIn) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
//                contentColor = Color.White
//            )
//        ) {
//            Text(
//                text = if (isClockedIn) "Clock Out" else "Clock In",
//                style = MaterialTheme.typography.titleMedium.copy(
//                    fontSize = MaterialTheme.typography.titleLarge.fontSize
//                )
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = onNavigateToUserSettings,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            shape = RoundedCornerShape(12.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary
//            )
//        ) {
//            Text(
//                "User Settings",
//                style = MaterialTheme.typography.titleMedium
//            )
//        }
//    }
//}



//package com.thatwaz.timesquish.ui.screens
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
//
//@Composable
//fun HomeScreen(
//    viewModel: TimeEntryViewModel = hiltViewModel(),
//    onNavigateToActiveSession: () -> Unit,
//    onNavigateToUserSettings: () -> Unit
//) {
//    val activeSession by viewModel.activeSession.collectAsState()
//    val isClockedIn = activeSession != null
//
//    // Auto-navigate if already clocked in
//    LaunchedEffect(activeSession) {
//        if (isClockedIn) {
//            onNavigateToActiveSession()
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = if (isClockedIn) "You are clocked in." else "You are clocked out.",
//            style = MaterialTheme.typography.headlineSmall
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(
//            onClick = {
//                if (isClockedIn) viewModel.clockOut() else viewModel.clockIn()
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            shape = RoundedCornerShape(12.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = if (isClockedIn) MaterialTheme.colorScheme.error else Color(0xFF4CAF50), // Green
//                contentColor = Color.White
//            ),
//            elevation = ButtonDefaults.buttonElevation(
//                defaultElevation = 6.dp,
//                pressedElevation = 12.dp
//            )
//        ) {
//            Text(
//                text = if (isClockedIn) "Clock Out" else "Clock In",
//                style = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = onNavigateToUserSettings,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            shape = RoundedCornerShape(12.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary
//            ),
//            elevation = ButtonDefaults.buttonElevation(
//                defaultElevation = 4.dp,
//                pressedElevation = 8.dp
//            )
//        ) {
//            Text(
//                "User Settings",
//                style = MaterialTheme.typography.titleMedium
//            )
//        }
//    }
//}




