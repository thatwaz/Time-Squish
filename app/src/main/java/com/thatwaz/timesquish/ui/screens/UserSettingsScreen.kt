package com.thatwaz.timesquish.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UserSettingsScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val reminderHours by viewModel.reminderHoursFlow.collectAsState(initial = 2L)
    val hourlyPay by viewModel.hourlyPayFlow.collectAsState(initial = 0.0)

    var sliderValue by remember { mutableStateOf(reminderHours.toFloat()) }
    var hourlyPayInput by remember { mutableStateOf(hourlyPay.takeIf { it != 0.0 }?.toString() ?: "") }
    var isHourlyPayEditable by remember { mutableStateOf(false) }

    val hourlyPayFocusRequester = remember { FocusRequester() }

    // Sync with stored value
    LaunchedEffect(hourlyPay) {
        if (!isHourlyPayEditable && hourlyPay > 0.0) {
            hourlyPayInput = hourlyPay.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("User Settings", style = MaterialTheme.typography.titleLarge)

        // 🔔 Reminder Settings
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

        // 💵 Hourly Pay
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("💵 Hourly Pay", style = MaterialTheme.typography.titleMedium)

            // Moved Edit option just under the heading
            if (!isHourlyPayEditable) {
                Text(
                    text = "✏️ Edit Hourly Rate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            isHourlyPayEditable = true
                            focusManager.clearFocus()
                            scope.launch {
                                delay(100)
                                hourlyPayFocusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        }
                        .padding(bottom = 4.dp)
                )
            }

            OutlinedTextField(
                value = hourlyPayInput,
                onValueChange = { hourlyPayInput = it },
                readOnly = !isHourlyPayEditable,
                label = { Text("Enter hourly rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(hourlyPayFocusRequester)
                    .alpha(if (isHourlyPayEditable) 1f else 0.6f)
                    .pointerInput(isHourlyPayEditable) {
                        if (!isHourlyPayEditable) {
                            awaitPointerEventScope {
                                awaitPointerEvent() // Consume touch events to suppress focus/border
                            }
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isHourlyPayEditable) MaterialTheme.colorScheme.primary else Color.Transparent,
                    unfocusedBorderColor = if (isHourlyPayEditable) MaterialTheme.colorScheme.onSurfaceVariant else Color.Transparent,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isHourlyPayEditable) 1f else 0.6f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Button(
                onClick = {
                    val parsed = hourlyPayInput.toDoubleOrNull()
                    if (parsed != null && parsed > 0.0) {
                        scope.launch {
                            viewModel.saveHourlyPay(parsed)
                            Toast.makeText(context, "Hourly rate saved", Toast.LENGTH_SHORT).show()
                            isHourlyPayEditable = false
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    } else {
                        Toast.makeText(context, "Please enter a valid rate", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = isHourlyPayEditable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Hourly Pay")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}





