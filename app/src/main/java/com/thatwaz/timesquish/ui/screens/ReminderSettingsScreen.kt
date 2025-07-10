package com.thatwaz.timesquish.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import kotlinx.coroutines.launch

@Composable
fun ReminderSettingsScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load saved preference
    val reminderHoursFlow = viewModel.reminderHoursFlow.collectAsState(initial = 2L)
    var sliderValue by remember { mutableStateOf(reminderHoursFlow.value.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Reminder Settings",
            style = MaterialTheme.typography.titleLarge
        )

        Text("Notify me after this many hours if still clocked in:")

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 1f..12f,
            steps = 11
        )

        Text("Hours: ${sliderValue.toInt()}")

        Button(
            onClick = {
                scope.launch {
                    viewModel.saveReminderHours(sliderValue.toLong())
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

