package com.grullondev.firstapp.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.presentation.ui.components.*
import com.grullondev.firstapp.presentation.viewmodel.CalendarViewModel

private fun zeroPad(n: Int) = n.toString().padStart(2, '0')

@Composable
fun CalendarScreen(viewModel: CalendarViewModel, themeColor: Color = Color(0xFF008069)) {
    val calendarEvents by viewModel.calendarEvents.collectAsState()

    var currentYear by remember { mutableStateOf(2026) }
    var currentMonth by remember { mutableStateOf(5) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    var showFabMenu by remember { mutableStateOf(false) }
    var showEventDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var prefillDate by remember { mutableStateOf("") }

    val selectedDateStr = selectedDay?.let { "${currentYear}-${zeroPad(currentMonth)}-${zeroPad(it)}" }
    val eventsForSelectedDay = if (selectedDateStr != null)
        calendarEvents.filter { it.date == selectedDateStr }
    else emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            MonthNavigationHeader(
                year = currentYear,
                month = currentMonth,
                themeColor = themeColor,
                onPrevious = {
                    if (currentMonth == 1) { currentMonth = 12; currentYear-- }
                    else currentMonth--
                    selectedDay = null
                },
                onNext = {
                    if (currentMonth == 12) { currentMonth = 1; currentYear++ }
                    else currentMonth++
                    selectedDay = null
                }
            )
            CalendarGrid(
                year = currentYear,
                month = currentMonth,
                selectedDay = selectedDay,
                events = calendarEvents,
                themeColor = themeColor,
                onDaySelected = { day -> selectedDay = if (selectedDay == day) null else day }
            )
            if (selectedDay != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                SelectedDayEvents(
                    events = eventsForSelectedDay,
                    themeColor = themeColor,
                    onDelete = { viewModel.deleteCalendarEvent(it) }
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedVisibility(
                    visible = showFabMenu,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FabOption(icon = "📅", label = "Evento", themeColor = themeColor, onClick = {
                            prefillDate = selectedDateStr ?: "${currentYear}-${zeroPad(currentMonth)}-01"
                            showEventDialog = true; showFabMenu = false
                        })
                        FabOption(icon = "🔔", label = "Recordatorio", themeColor = themeColor, onClick = {
                            prefillDate = selectedDateStr ?: "${currentYear}-${zeroPad(currentMonth)}-01"
                            showReminderDialog = true; showFabMenu = false
                        })
                    }
                }
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = themeColor, contentColor = Color.White, shape = CircleShape
                ) {
                    Text(if (showFabMenu) "✕" else "+", fontSize = 24.sp, color = Color.White)
                }
            }
        }

        if (showFabMenu) {
            Box(modifier = Modifier.fillMaxSize().clickable { showFabMenu = false })
        }
    }

    if (showEventDialog) {
        CreateEventDialog(
            initialDate = prefillDate, themeColor = themeColor,
            onDismiss = { showEventDialog = false },
            onCreate = { event -> viewModel.addCalendarEvent(event); showEventDialog = false }
        )
    }

    if (showReminderDialog) {
        CreateReminderDialog(
            initialDate = prefillDate, themeColor = themeColor,
            onDismiss = { showReminderDialog = false },
            onCreate = { event -> viewModel.addCalendarEvent(event); showReminderDialog = false }
        )
    }
}
