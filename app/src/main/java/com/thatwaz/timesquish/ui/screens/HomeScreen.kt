package com.thatwaz.timesquish.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel

@Composable
fun HomeScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onNavigateToWeekView: () -> Unit,
    onNavigateToManualEntry: () -> Unit
) {
    val isClockedIn by viewModel.isClockedIn.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    viewModel.clockIn()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isClockedIn) "Clock Out" else "Clock In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToManualEntry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Manual Entry")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToWeekView,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View This Week")
        }
    }
}
