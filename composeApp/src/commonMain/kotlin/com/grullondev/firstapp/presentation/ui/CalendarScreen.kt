package com.grullondev.firstapp.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.grullondev.firstapp.domain.model.CalendarEvent
import com.grullondev.firstapp.domain.model.EventType
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)
private val DAY_NAMES = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do")

private fun daysInMonth(year: Int, month: Int): Int {
    val days30 = setOf(4, 6, 9, 11)
    return when {
        month == 2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        month in days30 -> 30
        else -> 31
    }
}

private fun dayOfWeek(year: Int, month: Int, day: Int): Int {
    val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    val y = if (month < 3) year - 1 else year
    val dow = (y + y / 4 - y / 100 + y / 400 + t[month - 1] + day) % 7
    return (dow + 6) % 7
}

private fun generateId(): String = kotlin.random.Random.nextLong().toString(36)

private fun zeroPad(n: Int) = n.toString().padStart(2, '0')

@Composable
fun CalendarScreen(viewModel: ChatViewModel) {
    val themeColor by viewModel.themeColor.collectAsState()
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

@Composable
private fun MonthNavigationHeader(year: Int, month: Int, themeColor: Color, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = onPrevious) { Text("◀", fontSize = 18.sp, color = themeColor) }
        Text("${MONTH_NAMES[month - 1]} $year", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        IconButton(onClick = onNext) { Text("▶", fontSize = 18.sp, color = themeColor) }
    }
}

@Composable
private fun CalendarGrid(year: Int, month: Int, selectedDay: Int?, events: List<CalendarEvent>, themeColor: Color, onDaySelected: (Int) -> Unit) {
    val days = daysInMonth(year, month)
    val startDow = dayOfWeek(year, month, 1)

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_NAMES.forEach { name ->
                Text(name, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        val cells = MutableList<Int?>(startDow) { null }
        for (d in 1..days) cells.add(d)
        while (cells.size % 7 != 0) cells.add(null)

        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (day != null) {
                            val dateStr = "${year}-${zeroPad(month)}-${zeroPad(day)}"
                            val hasEvents = events.any { it.date == dateStr }
                            val isSelected = selectedDay == day
                            Box(
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    .background(if (isSelected) themeColor else Color.Transparent)
                                    .clickable { onDaySelected(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(day.toString(), fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                                    if (hasEvents) {
                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape)
                                            .background(if (isSelected) Color.White else themeColor))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDayEvents(events: List<CalendarEvent>, themeColor: Color, onDelete: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (events.isEmpty()) "Sin eventos este día" else "Eventos del día",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(events, key = { it.id }) { event ->
                EventItem(event = event, themeColor = themeColor, onDelete = { onDelete(event.id) })
            }
        }
    }
}

@Composable
private fun EventItem(event: CalendarEvent, themeColor: Color, onDelete: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(themeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Text(if (event.type == EventType.REMINDER) "🔔" else "📅", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.time, style = MaterialTheme.typography.bodySmall, color = themeColor)
                    if (event.description.isNotBlank()) {
                        Text(" • ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(event.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = onDelete) { Text("🗑️", fontSize = 16.sp) }
        }
    }
}

@Composable
private fun FabOption(icon: String, label: String, themeColor: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onClick)) {
        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp), shadowElevation = 2.dp) {
            Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(themeColor), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 18.sp)
        }
    }
}

@Composable
fun CreateEventDialog(initialDate: String, themeColor: Color, onDismiss: () -> Unit, onCreate: (CalendarEvent) -> Unit) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf("09:00") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nuevo Evento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = themeColor)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título *") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Fecha (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor))
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Hora (HH:MM)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (title.isNotBlank() && date.isNotBlank())
                            onCreate(CalendarEvent(id = generateId(), title = title, description = description,
                                date = date, time = time.ifBlank { "09:00" }, type = EventType.EVENT))
                    }, colors = ButtonDefaults.buttonColors(containerColor = themeColor)) {
                        Text("Crear", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateReminderDialog(initialDate: String, themeColor: Color, onDismiss: () -> Unit, onCreate: (CalendarEvent) -> Unit) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf("09:00") }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nuevo Recordatorio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = themeColor)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título *") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Fecha (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor))
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Hora (HH:MM)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Nota") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (title.isNotBlank() && date.isNotBlank())
                            onCreate(CalendarEvent(id = generateId(), title = title, description = note,
                                date = date, time = time.ifBlank { "09:00" }, type = EventType.REMINDER))
                    }, colors = ButtonDefaults.buttonColors(containerColor = themeColor)) {
                        Text("Recordar", color = Color.White)
                    }
                }
            }
        }
    }
}