package com.thatwaz.timesquish.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import com.thatwaz.timesquish.util.showDatePicker
import com.thatwaz.timesquish.util.showTimePicker
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ManualEntryScreen(
    entryId: Int? = null,
    onEntryAdded: () -> Unit,
    viewModel: TimeEntryViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Load the current entries so we can find the one to edit
    val allEntries by viewModel.allEntries.collectAsState()
    val entryToEdit = allEntries.find { it.id == entryId }

    // Initial state
    val initialStart = remember(entryToEdit) {
        entryToEdit?.startTime ?: LocalDateTime.now().withSecond(0).withNano(0)
    }
    val initialEnd = remember(entryToEdit) {
        entryToEdit?.endTime ?: LocalDateTime.now().withSecond(0).withNano(0).plusHours(1)
    }
    val initialLabel = remember(entryToEdit) {
        entryToEdit?.label ?: ""
    }

    var startDateTime by remember { mutableStateOf(initialStart) }
    var endDateTime by remember { mutableStateOf(initialEnd) }
    var label by remember { mutableStateOf(initialLabel) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Start Date
        OutlinedButton(
            onClick = {
                showDatePicker(
                    context = context,
                    initialDate = startDateTime.toLocalDate()
                ) { selectedDate ->
                    startDateTime = startDateTime.with(selectedDate)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Date: ${startDateTime.format(dateFormatter)}")
        }

        // Start Time
        OutlinedButton(
            onClick = {
                showTimePicker(
                    context = context,
                    initialTime = startDateTime.toLocalTime()
                ) { selectedTime ->
                    startDateTime = startDateTime.with(selectedTime)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Time: ${startDateTime.format(timeFormatter)}")
        }

        // End Date
        OutlinedButton(
            onClick = {
                showDatePicker(
                    context = context,
                    initialDate = endDateTime.toLocalDate()
                ) { selectedDate ->
                    endDateTime = endDateTime.with(selectedDate)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("End Date: ${endDateTime.format(dateFormatter)}")
        }

        // End Time
        OutlinedButton(
            onClick = {
                showTimePicker(
                    context = context,
                    initialTime = endDateTime.toLocalTime()
                ) { selectedTime ->
                    endDateTime = endDateTime.with(selectedTime)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("End Time: ${endDateTime.format(timeFormatter)}")
        }

        // Label
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Label (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Save button
        Button(
            onClick = {
                if (endDateTime.isAfter(startDateTime)) {
                    if (entryToEdit != null) {
                        // Update existing entry
                        viewModel.updateTimeEntry(
                            entryToEdit.copy(
                                startTime = startDateTime,
                                endTime = endDateTime,
                                durationMinutes = java.time.Duration.between(startDateTime, endDateTime).toMinutes(),
                                label = label.ifBlank { null },
                                isManual = true
                            )
                        )
                    } else {
                        // Insert new entry
                        viewModel.insertTimeEntry(
                            startTime = startDateTime,
                            endTime = endDateTime,
                            isManual = true,
                            label = label.ifBlank { null }
                        )
                    }
                    onEntryAdded()
                } else {
                    Toast.makeText(context, "End time must be after start time.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (entryToEdit != null) "Update Entry" else "Save Entry")
        }
    }
}


