package com.thatwaz.timesquish.ui.screens




import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.R
import com.thatwaz.timesquish.data.local.TimeEntry
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit


@Composable
fun HomeScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onNavigateToActiveSession: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val isClockedIn = activeSession != null

    // Default hourly from settings
    val defaultHourlyPay by viewModel.hourlyPayFlow.collectAsState(initial = 0.0)

    // All entries (for WTD/PPD totals)
    val entries by viewModel.allEntries.collectAsState()

    // (Optional) theme state – if you already persist this in DataStore, collect it here instead.
    var isDark by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Auto-navigate if already clocked in
    LaunchedEffect(isClockedIn) {
        if (isClockedIn) onNavigateToActiveSession()
    }

    // Tiny squish animation (swap images)
    var isSquished by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(700L)
            isSquished = !isSquished
        }
    }
    val imageRes = if (isSquished) R.drawable.unsquished_clock else R.drawable.squished_clock3

    // ---- Totals ----
    val currency = remember { NumberFormat.getCurrencyInstance() }

    val wtdRange = remember { currentWeekRange(LocalDate.now()) } // Sun..Sat
    val wtdEarnings = remember(entries) { earningsForRange(entries, wtdRange.first, wtdRange.second, defaultHourlyPay) }

    val ppRange = remember { currentBiweeklyRange(LocalDate.now()) } // 2-week window
    val ppdEarnings = remember(entries) { earningsForRange(entries, ppRange.first, ppRange.second, defaultHourlyPay) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // a bit tighter to fit cards
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar: Title + Theme toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Time Squish", style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dark", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = isDark,
                    onCheckedChange = {
                        isDark = it
                        // Wire this to your app theme:
                        // viewModel.setDarkTheme(it)
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Stat cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Hourly Rate",
                value = currency.format(defaultHourlyPay),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "WTD Pay",
                value = currency.format(wtdEarnings),
//                subtitle = "${wtdRange.first}—${wtdRange.second}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pay Period",
                value = currency.format(ppdEarnings),
//                subtitle = "${ppRange.first}—${ppRange.second}",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Stick figure squishing a clock",
            modifier = Modifier.size(200.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (isClockedIn) "You are clocked in." else "You are clocked out.",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (isClockedIn) {
                    viewModel.clockOut()
                } else {
                    scope.launch { viewModel.clockIn(defaultHourlyPay) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isClockedIn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                if (isClockedIn) "Clock Out" else "Clock In",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/* ---------- Period helpers ---------- */

// Week = Sunday..Saturday
private fun currentWeekRange(today: LocalDate): Pair<LocalDate, LocalDate> {
    val start = today.with(DayOfWeek.SUNDAY)
    val end = start.plusDays(6)
    return start to end
}

/**
 * Biweekly pay period aligned to a fixed Sunday anchor (1970-01-04),
 * so it’s stable across devices. If the number of weeks since anchor is even,
 * period starts this Sunday; if odd, period starts last Sunday.
 */
private fun currentBiweeklyRange(today: LocalDate): Pair<LocalDate, LocalDate> {
    val thisSunday = today.with(DayOfWeek.SUNDAY)
    val anchor = LocalDate.of(1970, 1, 4) // a Sunday
    val weeksSince = ChronoUnit.WEEKS.between(anchor, thisSunday).toInt()
    val periodStart = if (weeksSince % 2 == 0) thisSunday else thisSunday.minusWeeks(1)
    val periodEnd = periodStart.plusDays(13)
    return periodStart to periodEnd
}

/* ---------- Earnings calc ---------- */

private fun earningsForRange(
    entries: List<TimeEntry>,
    start: LocalDate,
    end: LocalDate,
    defaultHourly: Double
): Double {
    if (entries.isEmpty()) return 0.0
    return entries
        .asSequence()
        .filter { it.startTime.toLocalDate() in start..end }
        .sumOf { entry ->
            val minutes = (entry.durationMinutes ?: 0L).toDouble()
            val hours = minutes / 60.0
            val rate = (entry.hourlyPay.takeIf { it > 0.0 } ?: defaultHourly)
            hours * rate
        }
}

//@Composable
//fun HomeScreen(
//    viewModel: TimeEntryViewModel = hiltViewModel(),
//    onNavigateToActiveSession: () -> Unit
//) {
//    val activeSession by viewModel.activeSession.collectAsState()
//    val isClockedIn = activeSession != null
//    val defaultHourlyPay by viewModel.hourlyPayFlow.collectAsState(initial = 0.0)
//
//    val scope = rememberCoroutineScope()
//
//    // Auto-navigate if already clocked in
//    LaunchedEffect(activeSession) {
//        if (isClockedIn) {
//            onNavigateToActiveSession()
//        }
//    }
//
//    // Animate image swap between squished/unsquished
//    var isSquished by remember { mutableStateOf(true) }
//
//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(700L)
//            isSquished = !isSquished
//        }
//    }
//
//    val imageRes = if (isSquished) R.drawable.unsquished_clock else R.drawable.squished_clock3
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Time Squish",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        Image(
//            painter = painterResource(id = imageRes),
//            contentDescription = "Stick figure squishing a clock",
//            modifier = Modifier
//                .size(200.dp)
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        Text(
//            text = if (isClockedIn) "You are clocked in." else "You are clocked out.",
//            style = MaterialTheme.typography.headlineSmall
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(
//            onClick = {
//                if (isClockedIn) {
//                    viewModel.clockOut()
//                } else {
//                    scope.launch {
//                        viewModel.clockIn(defaultHourlyPay)
//                    }
//                }
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
//
//    }
//}


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




