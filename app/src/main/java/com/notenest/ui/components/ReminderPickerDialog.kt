package com.notenest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatReminderDateTime(timeMillis: Long): String {
    val now = Calendar.getInstance()
    val reminderCal = Calendar.getInstance().apply { timeInMillis = timeMillis }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeString = timeFormat.format(Date(timeMillis))

    val isToday = now.get(Calendar.YEAR) == reminderCal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == reminderCal.get(Calendar.DAY_OF_YEAR)

    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val isTomorrow = tomorrow.get(Calendar.YEAR) == reminderCal.get(Calendar.YEAR) &&
            tomorrow.get(Calendar.DAY_OF_YEAR) == reminderCal.get(Calendar.DAY_OF_YEAR)

    return when {
        isToday -> "Today, $timeString"
        isTomorrow -> "Tomorrow, $timeString"
        else -> {
            val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            dateFormat.format(Date(timeMillis))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerDialog(
    currentReminder: Long?,
    onDismissRequest: () -> Unit,
    onReminderSet: (Long) -> Unit,
    onReminderClear: () -> Unit
) {
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showCustomTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val currentCal = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = (currentCal.get(Calendar.HOUR_OF_DAY) + 1) % 24,
        initialMinute = 0
    )

    if (showCustomDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            selectedDateMillis = selected
                            showCustomDatePicker = false
                            showCustomTimePicker = true
                        }
                    }
                ) {
                    Text("Next (Set Time)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCustomTimePicker) {
        AlertDialog(
            onDismissRequest = { showCustomTimePicker = false },
            title = {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dateMillis = selectedDateMillis ?: System.currentTimeMillis()
                        val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = dateMillis
                        }
                        
                        val finalCalendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        onReminderSet(finalCalendar.timeInMillis)
                        showCustomTimePicker = false
                        onDismissRequest()
                    }
                ) {
                    Text("Set Reminder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimePicker = false }) {
                    Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Set Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentReminder != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Current Reminder",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatReminderDateTime(currentReminder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    onReminderClear()
                                    onDismissRequest()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Remove Reminder",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = "QUICK PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                // Preset: Later Today (+3 hours)
                PresetItem(
                    icon = Icons.Default.AccessTime,
                    title = "Later Today",
                    subtitle = "In 3 hours",
                    onClick = {
                        val cal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 3) }
                        onReminderSet(cal.timeInMillis)
                        onDismissRequest()
                    }
                )

                // Preset: Tomorrow Morning (9:00 AM)
                PresetItem(
                    icon = Icons.Default.Today,
                    title = "Tomorrow Morning",
                    subtitle = "9:00 AM",
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, 9)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onReminderSet(cal.timeInMillis)
                        onDismissRequest()
                    }
                )

                // Preset: Tomorrow Evening (6:00 PM)
                PresetItem(
                    icon = Icons.Default.Schedule,
                    title = "Tomorrow Evening",
                    subtitle = "6:00 PM",
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, 18)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onReminderSet(cal.timeInMillis)
                        onDismissRequest()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Date & Time Button
                OutlinedButton(
                    onClick = {
                        showCustomDatePicker = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_date_time_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pick Custom Date & Time...",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("reminder_dialog_cancel")
            ) {
                Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun PresetItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
