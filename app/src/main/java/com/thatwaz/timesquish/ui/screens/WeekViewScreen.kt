package com.thatwaz.timesquish.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.data.local.TimeEntry
import com.thatwaz.timesquish.ui.SummaryCard
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import com.thatwaz.timesquish.util.getEndOfWeek
import com.thatwaz.timesquish.util.getStartOfWeek
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class ListMode {
    NORMAL,
    SUBMIT,
    SQUISH,
    EDIT
}



@Composable
fun WeekViewScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onEditEntry: (Int) -> Unit
) {
    // List Mode
    var listMode by remember { mutableStateOf(ListMode.NORMAL) }

    // State
    val allEntries by viewModel.allEntries.collectAsState()
    val thisWeekStart = getStartOfWeek(LocalDateTime.now())
    var selectedWeekStart by remember { mutableStateOf(thisWeekStart) }

    // Dropdown
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val weeks = (0..11).map { thisWeekStart.minusWeeks(it.toLong()) }

    // Squish selection
    val selectingDays = remember { mutableStateOf(setOf<DayOfWeek>()) }
    val selectedEntryIdsByDay = remember { mutableStateOf(mapOf<DayOfWeek, Set<Int>>()) }

    // Pending deletion
    var entryPendingDelete by remember { mutableStateOf<TimeEntry?>(null) }

    // Filter entries
    val weekEntries = allEntries.filter { entry ->
        entry.startTime.isAfter(selectedWeekStart.minusNanos(1)) &&
                entry.startTime.isBefore(getEndOfWeek(selectedWeekStart).plusNanos(1))
    }
    val unsubmittedEntries = weekEntries.filter { !it.isSubmitted }
    val submittedEntries = weekEntries.filter { it.isSubmitted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Week dropdown
        WeekSelector(
            selectedWeekStart,
            weeks,
            isDropdownExpanded,
            onWeekSelected = { selectedWeekStart = it },
            onExpandChanged = { isDropdownExpanded = it }
        )

        Spacer(modifier = Modifier.height(12.dp))


        // Mode buttons
        ModeButtons(
            listMode = listMode,
            onModeChange = { newMode ->
                listMode = newMode
                if (newMode != ListMode.SQUISH) {
                    selectingDays.value = emptySet()
                    selectedEntryIdsByDay.value = emptyMap()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        WeeklySummaryRow(weekEntries)

        if (weekEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No entries this week.")
            }
        } else {
            EntriesSection(
                unsubmittedEntries = unsubmittedEntries,
                submittedEntries = submittedEntries,
                listMode = listMode,
                selectingDays = selectingDays,
                selectedEntryIdsByDay = selectedEntryIdsByDay,
                onEditEntry = onEditEntry,
                viewModel = viewModel,
                onDeleteEntry = { entryPendingDelete = it }
            )
        }
    }

    // Confirm delete dialog
    if (entryPendingDelete != null) {
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteTimeEntry(entryPendingDelete!!)
                entryPendingDelete = null
            },
            onDismiss = { entryPendingDelete = null }
        )
    }
}

@Composable
fun WeeklySummaryRow(
    weekEntries: List<TimeEntry>
) {
    val uniqueDays = weekEntries
        .map { it.startTime.toLocalDate() }
        .distinct()
        .size

    val totalMinutes = weekEntries.sumOf { it.durationMinutes ?: 0 }
    val totalHours = totalMinutes / 60.0

    val hourlyRate = 13 // example hourly rate (we can make this user-configurable later)
    val estimatedEarnings = totalHours * hourlyRate

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = "Days Worked",
            value = "$uniqueDays",
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Hours Worked",
            value = "%.1f".format(totalHours),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Earnings",
            value = "$%.2f".format(estimatedEarnings),
            modifier = Modifier.weight(1f)
        )
    }
}



@Composable
fun WeekSelector(
    selectedWeekStart: LocalDateTime,
    weeks: List<LocalDateTime>,
    isDropdownExpanded: Boolean,
    onWeekSelected: (LocalDateTime) -> Unit,
    onExpandChanged: (Boolean) -> Unit
) {
    Box {
        Button(
            onClick = { onExpandChanged(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Week of ${selectedWeekStart.toLocalDate()}")
        }
        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { onExpandChanged(false) }
        ) {
            weeks.forEach { week ->
                DropdownMenuItem(
                    text = { Text(week.toLocalDate().toString()) },
                    onClick = {
                        onWeekSelected(week)
                        onExpandChanged(false)
                    }
                )
            }
        }
    }
}

@Composable
fun ModeButtons(
    listMode: ListMode,
    onModeChange: (ListMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = {
                onModeChange(if (listMode == ListMode.SUBMIT) ListMode.NORMAL else ListMode.SUBMIT)
            },
            enabled = listMode != ListMode.SQUISH && listMode != ListMode.EDIT,
            modifier = Modifier.weight(1f)
        ) {
            Text(if (listMode == ListMode.SUBMIT) "Exit Submit" else "Submit")
        }

        OutlinedButton(
            onClick = {
                onModeChange(if (listMode == ListMode.SQUISH) ListMode.NORMAL else ListMode.SQUISH)
            },
            enabled = listMode != ListMode.SUBMIT && listMode != ListMode.EDIT,
            modifier = Modifier.weight(1f)
        ) {
            Text(if (listMode == ListMode.SQUISH) "Exit Squish" else "Squish")
        }

        OutlinedButton(
            onClick = {
                onModeChange(if (listMode == ListMode.EDIT) ListMode.NORMAL else ListMode.EDIT)
            },
            enabled = listMode != ListMode.SUBMIT && listMode != ListMode.SQUISH,
            modifier = Modifier.weight(1f)
        ) {
            Text(if (listMode == ListMode.EDIT) "Exit Edit" else "Edit")
        }
    }
}





@Composable
fun EntriesSection(
    unsubmittedEntries: List<TimeEntry>,
    submittedEntries: List<TimeEntry>,
    listMode: ListMode,
    selectingDays: MutableState<Set<DayOfWeek>>,
    selectedEntryIdsByDay: MutableState<Map<DayOfWeek, Set<Int>>>,
    onEditEntry: (Int) -> Unit,
    viewModel: TimeEntryViewModel,
    onDeleteEntry: (TimeEntry) -> Unit
) {
    val unsubmittedGrouped = unsubmittedEntries.groupBy { it.startTime.dayOfWeek }.toSortedMap()
    val submittedGrouped = submittedEntries.groupBy { it.startTime.dayOfWeek }.toSortedMap()

    LazyColumn {
        // Unsubmitted Section
        item {
            Text(
                "Unsubmitted Entries",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (unsubmittedGrouped.isEmpty()) {
            item {
                Text(
                    "No unsubmitted entries.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            unsubmittedGrouped.forEach { (dayOfWeek, entriesForDay) ->
                val isSelecting = selectingDays.value.contains(dayOfWeek)
                val selectedIds = selectedEntryIdsByDay.value[dayOfWeek] ?: emptySet()

                item {
                    DayHeader(
                        dayOfWeek = dayOfWeek,
                        entriesForDay = entriesForDay,
                        listMode = listMode,
                        isSelecting = isSelecting,
                        onToggleSelect = {
                            selectingDays.value = if (isSelecting) {
                                selectingDays.value - dayOfWeek
                            } else {
                                selectingDays.value + dayOfWeek
                            }
                            selectedEntryIdsByDay.value =
                                selectedEntryIdsByDay.value.toMutableMap().apply {
                                    put(dayOfWeek, emptySet())
                                }
                        }
                    )
                }

                items(entriesForDay) { entry ->
                    TimeEntryRow(
                        entry = entry,
                        onSetSubmitted = { viewModel.setSubmitted(entry.id, it) },
                        onDelete = { onDeleteEntry(entry) },
                        onEdit = { onEditEntry(entry.id) },
                        onUnsquish = if (entry.label == "Squished Block") {
                            { viewModel.unsquishEntry(entry) }
                        } else null,
                        listMode = listMode,
                        showSelectCheckbox = isSelecting,
                        isSelected = selectedIds.contains(entry.id),
                        onSelectChanged = { checked ->
                            val current = selectedIds
                            selectedEntryIdsByDay.value =
                                selectedEntryIdsByDay.value.toMutableMap().apply {
                                    put(
                                        dayOfWeek,
                                        if (checked) current + entry.id else current - entry.id
                                    )
                                }
                        }
                    )
                    Divider()
                }

                if (isSelecting && selectedIds.isNotEmpty()) {
                    item {
                        Button(
                            onClick = {
                                val entriesToSquish = entriesForDay.filter { selectedIds.contains(it.id) }
                                viewModel.squishEntries(entriesToSquish)
                                selectedEntryIdsByDay.value = selectedEntryIdsByDay.value.toMutableMap().apply {
                                    put(dayOfWeek, emptySet())
                                }
                                selectingDays.value = selectingDays.value - dayOfWeek
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text("Squish Selected Entries")
                        }
                    }
                }
            }
        }

        // Submitted Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Submitted Entries",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (submittedGrouped.isEmpty()) {
            item {
                Text(
                    "No submitted entries.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            submittedGrouped.forEach { (dayOfWeek, entriesForDay) ->
                item {
                    DayHeader(
                        dayOfWeek = dayOfWeek,
                        entriesForDay = entriesForDay,
                        listMode = ListMode.NORMAL,
                        isSelecting = false,
                        onToggleSelect = {}
                    )
                }
                items(entriesForDay) { entry ->
                    TimeEntryRow(
                        entry = entry,
                        onSetSubmitted = { viewModel.setSubmitted(entry.id, it) },
                        onDelete = { onDeleteEntry(entry) },
                        onEdit = { onEditEntry(entry.id) },
                        listMode = listMode
                    )
                    Divider()
                }
            }
        }
    }
}


@Composable
fun DayHeader(
    dayOfWeek: DayOfWeek,
    entriesForDay: List<TimeEntry>,
    listMode: ListMode,
    isSelecting: Boolean,
    onToggleSelect: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
    val dateText = entriesForDay.first().startTime.format(dateFormatter)

    // Calculate total minutes for this day
    val totalMinutes = entriesForDay.sumOf { it.durationMinutes ?: 0 }
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    val timeWorkedText = buildString {
        if (hours > 0) append("${hours}h ")
        append("${minutes}m")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} - $dateText",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                timeWorkedText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (entriesForDay.size > 1 && listMode == ListMode.SQUISH) {
            Button(
                onClick = onToggleSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text(if (isSelecting) "Cancel Selection" else "Select Entries to Squish")
            }
        }
    }
}



@Composable
fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Entry") },
        text = { Text("Are you sure you want to delete this time entry? This cannot be undone.") },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}





@Composable
fun TimeEntryRow(
    entry: TimeEntry,
    onSetSubmitted: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onUnsquish: (() -> Unit)? = null,
    listMode: ListMode,
    showSelectCheckbox: Boolean = false,
    isSelected: Boolean = false,
    onSelectChanged: ((Boolean) -> Unit)? = null
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Squish selection toggle
        if (listMode == ListMode.SQUISH && showSelectCheckbox && onSelectChanged != null) {
            IconToggleButton(
                checked = isSelected,
                onCheckedChange = onSelectChanged
            ) {
                Icon(
                    imageVector = if (isSelected)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Select for squish"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Center: Entry info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = entry.startTime.format(dateFormatter),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${entry.startTime.format(timeFormatter)} - ${entry.endTime?.format(timeFormatter) ?: "In Progress"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Duration: ${entry.durationMinutes ?: "..."} min",
                style = MaterialTheme.typography.bodySmall
            )
            if (!entry.label.isNullOrBlank()) {
                Text(
                    text = "Label: ${entry.label}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (entry.label == "Squished Block" && onUnsquish != null) {
                Button(
                    onClick = onUnsquish,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Unsquish")
                }
            }
        }

        // Right: Conditional action buttons
        if (listMode == ListMode.EDIT) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }

        // Submit checkbox only in submit mode
        if (listMode == ListMode.SUBMIT) {
            Checkbox(
                checked = entry.isSubmitted,
                onCheckedChange = onSetSubmitted
            )
        }
    }
}






