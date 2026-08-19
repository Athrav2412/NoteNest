package com.notenest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notenest.data.database.Note
import com.notenest.utils.DateFormatter

@Composable
fun getCategoryAccentColor(category: String): Color {
    return when (category.lowercase()) {
        "work" -> Color(0xFF5C9DFF)
        "personal" -> Color(0xFFFF8B5E)
        "ideas" -> Color(0xFFFFCA58)
        "study" -> Color(0xFF6AE39C)
        else -> Color(0xFFC48BFF)
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val accentColor = getCategoryAccentColor(category)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Text(
            text = category.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = accentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0,
    onPinClick: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (note.isPinned) 3.dp else 1.dp,
            pressedElevation = 4.dp
        ),
        border = if (note.isPinned) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Category Badge, Pin status & Quick Actions Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(category = note.category)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isPinned) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned note",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(2.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("note_card_menu_button_${note.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (onPinClick != null) {
                                DropdownMenuItem(
                                    text = { Text(if (note.isPinned) "Unpin note" else "Pin note") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (note.isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onPinClick()
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text(if (note.isArchived) "Restore note" else "Archive note") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (note.isArchived) Icons.Rounded.Unarchive else Icons.Filled.Inventory2,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onArchiveClick()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete note", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Content snippet
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Footer: Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateFormatter.formatRelativeDate(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (note.createdAt != note.updatedAt) {
                    Text(
                        text = "Edited",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
