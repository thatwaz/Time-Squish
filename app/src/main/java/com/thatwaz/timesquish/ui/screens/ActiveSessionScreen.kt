package com.thatwaz.timesquish.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ActiveSessionScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onClockOut: () -> Unit
) {
    val activeSession = viewModel.activeSession.collectAsState().value
    val startTime = activeSession?.startTime

    var elapsed by remember { mutableStateOf(Duration.ZERO) }

    LaunchedEffect(startTime) {
        if (startTime != null) {
            while (true) {
                elapsed = Duration.between(startTime, LocalDateTime.now())
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (startTime != null) {
            Text(
                text = "Clocked in at: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Elapsed: ${elapsed.toMinutes()} min ${elapsed.seconds % 60} sec",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                viewModel.clockOut()
                onClockOut()
            }) {
                Text("Clock Out")
            }
        } else {
            Text("No active session.")
        }
    }
}


