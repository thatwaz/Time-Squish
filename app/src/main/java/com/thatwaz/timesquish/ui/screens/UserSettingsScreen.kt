package com.thatwaz.timesquish.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import kotlinx.coroutines.launch

@Composable
fun UserSettingsScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Reminder hours
    val reminderHoursFlow = viewModel.reminderHoursFlow.collectAsState(initial = 2L)
    var sliderValue by remember { mutableStateOf(reminderHoursFlow.value.toFloat()) }

    // Hourly pay
    val hourlyPayFlow = viewModel.hourlyPayFlow.collectAsState(initial = 0.0)
    var hourlyPayInput by remember { mutableStateOf(hourlyPayFlow.value.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("User Settings", style = MaterialTheme.typography.titleLarge)

        // Reminder Settings Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🔔 Reminder Settings", style = MaterialTheme.typography.titleMedium)
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
                        Toast.makeText(context, "Reminder time saved", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Reminder Hours")
            }
        }

        // Hourly Pay Settings Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("💵 Hourly Pay", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = hourlyPayInput,
                onValueChange = { hourlyPayInput = it },
                label = { Text("Enter hourly rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    scope.launch {
                        val parsed = hourlyPayInput.toDoubleOrNull()
                        if (parsed != null) {
                            viewModel.saveHourlyPay(parsed)
                            Toast.makeText(context, "Hourly rate saved", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Invalid hourly rate", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Hourly Pay")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Done button
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

