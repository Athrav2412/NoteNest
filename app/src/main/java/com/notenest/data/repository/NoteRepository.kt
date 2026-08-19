package com.notenest.data.repository

import com.notenest.data.database.Note
import com.notenest.data.database.NoteDao
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val allActiveNotes: Flow<List<Note>> = noteDao.getAllActiveNotes()
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()
    val allCategories: Flow<List<String>> = noteDao.getAllCategories()
    val activeNoteCount: Flow<Int> = noteDao.getActiveNoteCount()
    val archivedNoteCount: Flow<Int> = noteDao.getArchivedNoteCount()

    fun getNoteById(id: Int): Flow<Note?> = noteDao.getNoteById(id)

    suspend fun getNoteByIdSync(id: Int): Note? = noteDao.getNoteByIdSync(id)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Int) = noteDao.deleteNoteById(id)

    suspend fun togglePin(id: Int, currentPinState: Boolean) {
        noteDao.updatePinStatus(id, !currentPinState)
    }

    suspend fun toggleArchive(id: Int, currentArchiveState: Boolean) {
        noteDao.updateArchiveStatus(id, !currentArchiveState)
    }
}
