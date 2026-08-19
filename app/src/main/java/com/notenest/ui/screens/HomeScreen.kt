package com.notenest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notenest.data.database.Note
import com.notenest.ui.components.CategoryChip
import com.notenest.ui.components.EmptyState
import com.notenest.ui.components.NoteCard
import com.notenest.ui.components.NoteNestTopBar
import com.notenest.ui.components.NoteSearchBar
import com.notenest.viewmodel.NavigationTab
import com.notenest.viewmodel.NoteViewModel
import com.notenest.viewmodel.SortOption

@Composable
fun HomeScreen(
    viewModel: NoteViewModel,
    onNavigateToAddNote: () -> Unit,
    onNavigateToEditNote: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeNotes by viewModel.activeNotesState.collectAsStateWithLifecycle()
    val archivedNotes by viewModel.archivedNotesState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    val pinnedNotes = activeNotes.filter { it.isPinned }
    val unpinnedNotes = activeNotes.filter { !it.isPinned }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("home_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (currentTab == NavigationTab.NOTES) {
                FloatingActionButton(
                    onClick = onNavigateToAddNote,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("add_note_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new note",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        bottomBar = {
            GeometricBottomNavBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    if (tab == NavigationTab.SETTINGS) {
                        onNavigateToSettings()
                    } else {
                        viewModel.setCurrentTab(tab)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Top Bar
            NoteNestTopBar(
                title = when (currentTab) {
                    NavigationTab.NOTES -> "NoteNest"
                    NavigationTab.ARCHIVE -> "Archive"
                    NavigationTab.SETTINGS -> "Settings"
                },
                showLogo = currentTab == NavigationTab.NOTES,
                currentSortOption = if (currentTab == NavigationTab.NOTES) sortOption else null,
                onSortOptionSelected = { viewModel.setSortOption(it) },
                onSettingsClick = onNavigateToSettings
            )

            // Search Bar
            NoteSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Category Filter Row (Only shown on Notes tab)
            if (currentTab == NavigationTab.NOTES) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("category_filter_row"),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoryChip(
                            category = "All",
                            isSelected = selectedCategory == null,
                            onClick = { viewModel.setSelectedCategory(null) }
                        )
                    }
                    items(categories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory?.equals(category, ignoreCase = true) == true,
                            onClick = {
                                if (selectedCategory?.equals(category, ignoreCase = true) == true) {
                                    viewModel.setSelectedCategory(null)
                                } else {
                                    viewModel.setSelectedCategory(category)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Area
            if (currentTab == NavigationTab.NOTES) {
                if (activeNotes.isEmpty()) {
                    if (searchQuery.isNotBlank() || selectedCategory != null) {
                        EmptyState(
                            title = "No notes found",
                            description = "No notes matched your search query or selected category.",
                            icon = Icons.Outlined.SearchOff,
                            actionButtonText = "Clear Filters",
                            onActionClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setSelectedCategory(null)
                            }
                        )
                    } else {
                        EmptyState(
                            title = "No notes yet",
                            description = "Capture your thoughts, ideas, tasks, or reminders with NoteNest.",
                            icon = Icons.Outlined.Home,
                            actionButtonText = "Create Note",
                            onActionClick = onNavigateToAddNote
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notes_list"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Pinned Notes Section
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PINNED NOTES (${pinnedNotes.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            itemsIndexed(pinnedNotes, key = { _, note -> "pinned_${note.id}" }) { index, note ->
                                NoteCard(
                                    note = note,
                                    index = index,
                                    onClick = { onNavigateToEditNote(note.id) },
                                    onPinClick = { viewModel.togglePin(note) },
                                    onArchiveClick = { viewModel.toggleArchive(note) },
                                    onDeleteClick = { viewModel.deleteNote(note) }
                                )
                            }

                            if (unpinnedNotes.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "OTHER NOTES (${unpinnedNotes.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Unpinned Notes Section
                        itemsIndexed(unpinnedNotes, key = { _, note -> "note_${note.id}" }) { index, note ->
                            NoteCard(
                                note = note,
                                index = index + pinnedNotes.size,
                                onClick = { onNavigateToEditNote(note.id) },
                                onPinClick = { viewModel.togglePin(note) },
                                onArchiveClick = { viewModel.toggleArchive(note) },
                                onDeleteClick = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                }
            } else if (currentTab == NavigationTab.ARCHIVE) {
                if (archivedNotes.isEmpty()) {
                    EmptyState(
                        title = "No archived notes",
                        description = "Archived notes will appear here. Archive notes you want to keep organized without deleting them.",
                        icon = Icons.Outlined.Inventory2
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("archived_notes_list"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(archivedNotes, key = { _, note -> "archived_${note.id}" }) { index, note ->
                            NoteCard(
                                note = note,
                                index = index,
                                onClick = { onNavigateToEditNote(note.id) },
                                onArchiveClick = { viewModel.toggleArchive(note) },
                                onDeleteClick = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeometricBottomNavBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar"),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home / Notes Tab
            BottomNavItem(
                title = "Home",
                isSelected = currentTab == NavigationTab.NOTES,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onTabSelected(NavigationTab.NOTES) },
                testTag = "nav_tab_notes"
            )

            // Archive Tab
            BottomNavItem(
                title = "Archive",
                isSelected = currentTab == NavigationTab.ARCHIVE,
                selectedIcon = Icons.Filled.Inventory2,
                unselectedIcon = Icons.Outlined.Inventory2,
                onClick = { onTabSelected(NavigationTab.ARCHIVE) },
                testTag = "nav_tab_archive"
            )

            // Settings Tab
            BottomNavItem(
                title = "Settings",
                isSelected = currentTab == NavigationTab.SETTINGS,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                onClick = { onTabSelected(NavigationTab.SETTINGS) },
                testTag = "nav_tab_settings"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    title: String,
    isSelected: Boolean,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = unselectedIcon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
