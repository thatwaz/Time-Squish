package com.thatwaz.timesquish.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun ActiveSessionScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onClockOut: () -> Unit
) {
    val activeSession = viewModel.activeSession.collectAsState().value
    val allEntries by viewModel.allEntries.collectAsState()
    var showEarnings by rememberSaveable { mutableStateOf(true) }
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    // Force update every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }

    val hourlyRate = activeSession?.hourlyPay ?: 0.0

    val formatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US)

    val greeting = remember(currentTime) {
        val hour = currentTime.hour
        when (hour) {
            in 5..11 -> "Good morning!"
            in 12..16 -> "Good afternoon!"
            in 17..22 -> "Good evening!"
            else -> "Burning the midnight oil?"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (activeSession != null) {
            val elapsed = Duration.between(activeSession.startTime, currentTime)
            val totalSeconds = elapsed.seconds
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            val liveSessionMinutes = elapsed.toMinutes()
            val sessionEarnings = (elapsed.seconds / 3600.0) * hourlyRate


            // Week starts on SUNDAY
            val startOfWeek = currentTime.toLocalDate().with(DayOfWeek.SUNDAY)
            val endOfWeek = startOfWeek.plusDays(6)
            val weeklyMinutes = allEntries
                .filter {
                    val date = it.startTime.toLocalDate()
                    date in startOfWeek..endOfWeek
                }
                .sumOf { it.durationMinutes ?: 0 }

            val weeklyEarnings = ((weeklyMinutes + liveSessionMinutes) / 60.0) * hourlyRate

            Text(text = greeting, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Clocked in at: ${activeSession.startTime.format(formatter)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (elapsed.toMinutes() % 60) / 60f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (showEarnings) {
                Text(
                    text = String.format("This session: $%.2f", sessionEarnings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format("Week to date: $%.2f", weeklyEarnings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(onClick = { showEarnings = !showEarnings }) {
                Text(if (showEarnings) "Hide Earnings" else "Show Earnings")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.clockOut()
                    onClockOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                elevation = ButtonDefaults.buttonElevation(8.dp, 12.dp)
            ) {
                Text(
                    "Clock Out",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
                )
            }
        } else {
            Text("No active session.")
        }
    }
}








//@Composable
//fun ActiveSessionScreen(
//    viewModel: TimeEntryViewModel = hiltViewModel(),
//    onClockOut: () -> Unit
//) {
//    val activeSession = viewModel.activeSession.collectAsState().value
//    var elapsed by remember { mutableStateOf(Duration.ZERO) }
//
//    LaunchedEffect(activeSession) {
//        if (activeSession != null) {
//            while (isActive) {
//                elapsed = Duration.between(activeSession.startTime, LocalDateTime.now())
//                delay(1000)
//            }
//        }
//    }
//
//    val formatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US)
//
//    val greeting = remember {
//        val hour = LocalDateTime.now().hour
//        when (hour) {
//            in 5..11 -> "Good morning!"
//            in 12..16 -> "Good afternoon!"
//            in 17..22 -> "Good evening!"
//            else -> "Burning the midnight oil?"
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
//        if (activeSession != null) {
//            val startTime = activeSession.startTime
//            val totalSeconds = elapsed.seconds
//            val hours = totalSeconds / 3600
//            val minutes = (totalSeconds % 3600) / 60
//            val seconds = totalSeconds % 60
//
//            Text(
//                text = greeting,
//                style = MaterialTheme.typography.headlineSmall
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Clocked in at: ${startTime.format(formatter)}",
//                style = MaterialTheme.typography.bodyLarge
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Text(
//                text = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
//                style = MaterialTheme.typography.displayMedium
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            LinearProgressIndicator(
//                progress = (elapsed.toMinutes() % 60) / 60f,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(8.dp)
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Button(
//                onClick = {
//                    viewModel.clockOut()
//                    onClockOut()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp)
//                    .padding(horizontal = 4.dp),
//                shape = RoundedCornerShape(12.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.error,
//                    contentColor = MaterialTheme.colorScheme.onError
//                ),
//                elevation = ButtonDefaults.buttonElevation(
//                    defaultElevation = 8.dp,
//                    pressedElevation = 12.dp
//                )
//            ) {
//                Text(
//                    "Clock Out",
//                    style = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize)
//                )
//            }
//
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "You're doing great. Keep going!",
//                style = MaterialTheme.typography.bodyMedium
//            )
//        } else {
//            Text("No active session.")
//        }
//    }
//}



//@Composable
//fun ActiveSessionScreen(
//    viewModel: TimeEntryViewModel = hiltViewModel(),
//    onClockOut: () -> Unit
//) {
//    val activeSession = viewModel.activeSession.collectAsState().value
//    val startTime = activeSession?.startTime
//
//    var elapsed by remember { mutableStateOf(Duration.ZERO) }
//
//    LaunchedEffect(startTime) {
//        if (startTime != null) {
//            while (true) {
//                elapsed = Duration.between(startTime, LocalDateTime.now())
//                kotlinx.coroutines.delay(1000)
//            }
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
//        if (startTime != null) {
//            Text(
//                text = "Clocked in at: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}",
//                style = MaterialTheme.typography.titleMedium
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "Elapsed: ${elapsed.toMinutes()} min ${elapsed.seconds % 60} sec",
//                style = MaterialTheme.typography.bodyLarge
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Button(onClick = {
//                viewModel.clockOut()
//                onClockOut()
//            }) {
//                Text("Clock Out")
//            }
//        } else {
//            Text("No active session.")
//        }
//    }
//}
//
//
