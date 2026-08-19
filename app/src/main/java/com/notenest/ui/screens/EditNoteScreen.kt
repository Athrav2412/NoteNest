package com.notenest.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notenest.ui.components.CategoryChip
import com.notenest.ui.components.ReminderPickerDialog
import com.notenest.ui.components.formatReminderDateTime
import com.notenest.utils.DateFormatter
import com.notenest.viewmodel.NoteViewModel

@Composable
fun EditNoteScreen(
    noteId: Int,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val noteFlow = remember(noteId) { viewModel.getNoteById(noteId) }
    val noteState by noteFlow.collectAsStateWithLifecycle(initialValue = null)

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var isPinned by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var createdAt by remember { mutableStateOf(0L) }
    var updatedAt by remember { mutableStateOf(0L) }
    var isInitialized by remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Permission launcher for Android 13+ notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showReminderDialog = true
        }
    }

    fun handleReminderClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                showReminderDialog = true
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            showReminderDialog = true
        }
    }

    // Initialize state when note loads from Room
    LaunchedEffect(noteState) {
        val currentNote = noteState
        if (currentNote != null && !isInitialized) {
            title = currentNote.title
            content = currentNote.content
            category = currentNote.category
            isPinned = currentNote.isPinned
            isArchived = currentNote.isArchived
            reminderTime = currentNote.reminderTime
            createdAt = currentNote.createdAt
            updatedAt = currentNote.updatedAt
            isInitialized = true
        }
    }

    // Auto-save modifications when leaving screen
    DisposableEffect(noteId) {
        onDispose {
            if (!isDeleted && isInitialized && (title.isNotBlank() || content.isNotBlank())) {
                viewModel.saveNote(
                    id = noteId,
                    title = title,
                    content = content,
                    category = category,
                    isPinned = isPinned,
                    isArchived = isArchived,
                    reminderTime = reminderTime
                )
            }
        }
    }

    if (showReminderDialog) {
        ReminderPickerDialog(
            currentReminder = reminderTime,
            onDismissRequest = { showReminderDialog = false },
            onReminderSet = { timeMillis ->
                reminderTime = timeMillis
            },
            onReminderClear = {
                reminderTime = null
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Note?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This note and its scheduled reminders will be permanently removed. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleted = true
                        showDeleteDialog = false
                        if (noteId != 0) {
                            viewModel.deleteNoteById(noteId)
                        }
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("edit_note_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("edit_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Actions Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reminder Button
                    IconButton(
                        onClick = { handleReminderClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (reminderTime != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("edit_reminder_button")
                    ) {
                        Icon(
                            imageVector = if (reminderTime != null) Icons.Default.NotificationsActive else Icons.Default.AddAlert,
                            contentDescription = if (reminderTime != null) "Reminder Active" else "Set Reminder",
                            tint = if (reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Pin Button
                    if (!isArchived) {
                        IconButton(
                            onClick = { isPinned = !isPinned },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("edit_pin_button")
                        ) {
                            Icon(
                                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (isPinned) "Unpin" else "Pin",
                                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Archive Button
                    IconButton(
                        onClick = { isArchived = !isArchived },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isArchived) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("edit_archive_button")
                    ) {
                        Icon(
                            imageVector = if (isArchived) Icons.Filled.Unarchive else Icons.Filled.Inventory2,
                            contentDescription = if (isArchived) "Restore" else "Archive",
                            tint = if (isArchived) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("edit_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete note",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            viewModel.saveNote(
                                id = noteId,
                                title = title,
                                content = content,
                                category = category,
                                isPinned = isPinned,
                                isArchived = isArchived,
                                reminderTime = reminderTime
                            ) {
                                onNavigateBack()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("edit_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Done", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Category Selector Chips
            Text(
                text = "CATEGORY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("edit_category_selection_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    CategoryChip(
                        category = cat,
                        isSelected = category.equals(cat, ignoreCase = true),
                        onClick = { category = cat }
                    )
                }
            }

            // Reminder Banner if Set
            if (reminderTime != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { handleReminderClick() }
                        .testTag("edit_reminder_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reminder: ${formatReminderDateTime(reminderTime!!)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = { reminderTime = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Reminder",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Timestamps: Created and Modified Dates
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (createdAt > 0) {
                    Text(
                        text = "Created: ${DateFormatter.formatFullDateTime(createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (updatedAt > 0 && updatedAt != createdAt) {
                    Text(
                        text = "Modified: ${DateFormatter.formatRelativeTime(updatedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (title.isEmpty()) {
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_note_title_input"),
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(bottom = 24.dp)
            ) {
                if (content.isEmpty()) {
                    Text(
                        text = "Start typing your note here…",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            lineHeight = 24.sp
                        )
                    )
                }
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_note_content_input"),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
