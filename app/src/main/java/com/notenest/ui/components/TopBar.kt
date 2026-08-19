package com.notenest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notenest.R
import com.notenest.viewmodel.SortOption

@Composable
fun NoteNestTopBar(
    title: String = "NoteNest",
    showLogo: Boolean = true,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    currentSortOption: SortOption? = null,
    onSortOptionSelected: ((SortOption) -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton && onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else if (showLogo) {
                // App Logo Box
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("app_logo_icon"),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.notenest.R.drawable.ic_app_icon),
                        contentDescription = "NoteNest Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (actions != null) {
                actions()
            }

            if (currentSortOption != null && onSortOptionSelected != null) {
                Box {
                    IconButton(
                        onClick = { sortMenuExpanded = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("sort_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort notes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest first") },
                            leadingIcon = {
                                if (currentSortOption == SortOption.NEWEST) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                onSortOptionSelected(SortOption.NEWEST)
                                sortMenuExpanded = false
                            },
                            modifier = Modifier.testTag("sort_newest_option")
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest first") },
                            leadingIcon = {
                                if (currentSortOption == SortOption.OLDEST) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                onSortOptionSelected(SortOption.OLDEST)
                                sortMenuExpanded = false
                            },
                            modifier = Modifier.testTag("sort_oldest_option")
                        )
                        DropdownMenuItem(
                            text = { Text("Title (A to Z)") },
                            leadingIcon = {
                                if (currentSortOption == SortOption.ALPHABETICAL_AZ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                onSortOptionSelected(SortOption.ALPHABETICAL_AZ)
                                sortMenuExpanded = false
                            },
                            modifier = Modifier.testTag("sort_az_option")
                        )
                        DropdownMenuItem(
                            text = { Text("Title (Z to A)") },
                            leadingIcon = {
                                if (currentSortOption == SortOption.ALPHABETICAL_ZA) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                onSortOptionSelected(SortOption.ALPHABETICAL_ZA)
                                sortMenuExpanded = false
                            },
                            modifier = Modifier.testTag("sort_za_option")
                        )
                    }
                }
            }

            if (onSettingsClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
