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
import androidx.compose.runtime.LaunchedEffect
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
    onNavigateToActiveSession: () -> Unit,
    onNavigateToReminderSettings: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()

    // Auto-navigate if already clocked in
    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            onNavigateToActiveSession()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (activeSession != null) "You are clocked in." else "You are clocked out.",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (activeSession != null) {
                    viewModel.clockOut()
                } else {
                    viewModel.clockIn()
                    // LaunchedEffect handles redirect
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (activeSession != null) "Clock Out" else "Clock In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToReminderSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reminder Settings")
        }
    }
}



