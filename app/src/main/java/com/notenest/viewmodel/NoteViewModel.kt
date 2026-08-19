package com.notenest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notenest.data.database.Note
import com.notenest.data.database.NoteDatabase
import com.notenest.data.repository.NoteRepository
import com.notenest.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption {
    NEWEST,
    OLDEST,
    ALPHABETICAL_AZ,
    ALPHABETICAL_ZA
}

enum class NavigationTab {
    NOTES,
    ARCHIVE,
    SETTINGS
}

data class NotesUiState(
    val pinnedNotes: List<Note> = emptyList(),
    val otherNotes: List<Note> = emptyList(),
    val allActiveNotes: List<Note> = emptyList(),
    val archivedNotes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val sortOption: SortOption = SortOption.NEWEST,
    val categories: List<String> = listOf("Work", "Personal", "Ideas", "Study", "General"),
    val activeNoteCount: Int = 0,
    val archivedNoteCount: Int = 0,
    val isDarkMode: Boolean? = true // Default to true as per Geometric Balance dark aesthetic
)

class NoteViewModel(
    application: Application,
    private val repository: NoteRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _currentTab = MutableStateFlow(NavigationTab.NOTES)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(true)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    // Default categories list combined with dynamically discovered ones
    private val baseCategories = listOf("Work", "Personal", "Ideas", "Study", "General")

    val categories: StateFlow<List<String>> = repository.allCategories
        .combine(MutableStateFlow(baseCategories)) { dbCategories, defaults ->
            (defaults + dbCategories).distinct()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = baseCategories
        )

    val activeNotesState: StateFlow<List<Note>> = combine(
        repository.allActiveNotes,
        _searchQuery,
        _selectedCategory,
        _sortOption
    ) { notes, query, category, sort ->
        var result = notes

        // 1. Filter by category
        if (!category.isNullOrBlank()) {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }

        // 2. Filter by search query
        if (query.isNotBlank()) {
            val trimmed = query.trim()
            result = result.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                        it.content.contains(trimmed, ignoreCase = true) ||
                        it.category.contains(trimmed, ignoreCase = true)
            }
        }

        // 3. Sort notes
        result = when (sort) {
            SortOption.NEWEST -> result.sortedByDescending { it.updatedAt }
            SortOption.OLDEST -> result.sortedBy { it.updatedAt }
            SortOption.ALPHABETICAL_AZ -> result.sortedBy { it.title.lowercase() }
            SortOption.ALPHABETICAL_ZA -> result.sortedByDescending { it.title.lowercase() }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val archivedNotesState: StateFlow<List<Note>> = combine(
        repository.archivedNotes,
        _searchQuery
    ) { notes, query ->
        if (query.isBlank()) {
            notes
        } else {
            val trimmed = query.trim()
            notes.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                        it.content.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeNoteCount: StateFlow<Int> = repository.activeNoteCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val archivedNoteCount: StateFlow<Int> = repository.archivedNoteCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun setCurrentTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setDarkMode(isDark: Boolean?) {
        _isDarkMode.value = isDark
    }

    fun getNoteById(id: Int) = repository.getNoteById(id)

    suspend fun getNoteByIdSync(id: Int): Note? = repository.getNoteByIdSync(id)

    fun saveNote(
        id: Int = 0,
        title: String,
        content: String,
        category: String,
        isPinned: Boolean = false,
        isArchived: Boolean = false,
        reminderTime: Long? = null,
        onComplete: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val cleanTitle = title.trim().ifEmpty { "Untitled Note" }
            val cleanCategory = category.trim().ifEmpty { "General" }

            val note = Note(
                id = id,
                title = cleanTitle,
                content = content,
                category = cleanCategory,
                createdAt = if (id == 0) currentTime else (getNoteByIdSync(id)?.createdAt ?: currentTime),
                updatedAt = currentTime,
                isPinned = isPinned,
                isArchived = isArchived,
                reminderTime = reminderTime
            )

            val generatedId = if (id == 0) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
                id.toLong()
            }

            val finalId = generatedId.toInt()
            val context = getApplication<Application>()
            if (reminderTime != null && reminderTime > currentTime && !isArchived) {
                ReminderScheduler.scheduleReminder(
                    context = context,
                    noteId = finalId,
                    title = cleanTitle,
                    content = content,
                    triggerAtMillis = reminderTime
                )
            } else {
                ReminderScheduler.cancelReminder(context, finalId)
            }

            onComplete?.invoke(generatedId)
        }
    }

    fun setNoteReminder(note: Note, reminderTime: Long?) {
        viewModelScope.launch {
            val updated = note.copy(reminderTime = reminderTime, updatedAt = System.currentTimeMillis())
            repository.updateNote(updated)
            val context = getApplication<Application>()
            if (reminderTime != null && reminderTime > System.currentTimeMillis() && !note.isArchived) {
                ReminderScheduler.scheduleReminder(
                    context = context,
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    triggerAtMillis = reminderTime
                )
            } else {
                ReminderScheduler.cancelReminder(context, note.id)
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.togglePin(note.id, note.isPinned)
        }
    }

    fun toggleArchive(note: Note) {
        viewModelScope.launch {
            repository.toggleArchive(note.id, note.isArchived)
            val context = getApplication<Application>()
            if (!note.isArchived) {
                // Was archived, now active - reschedule if reminder is future
                val reminder = note.reminderTime
                if (reminder != null && reminder > System.currentTimeMillis()) {
                    ReminderScheduler.scheduleReminder(
                        context = context,
                        noteId = note.id,
                        title = note.title,
                        content = note.content,
                        triggerAtMillis = reminder
                    )
                }
            } else {
                // Now archived - cancel reminder
                ReminderScheduler.cancelReminder(context, note.id)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(getApplication(), note.id)
            repository.deleteNote(note)
        }
    }

    fun deleteNoteById(id: Int) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(getApplication(), id)
            repository.deleteNoteById(id)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = NoteDatabase.getDatabase(application)
                    val repository = NoteRepository(database.noteDao())
                    return NoteViewModel(application, repository) as T
                }
            }
    }
}
