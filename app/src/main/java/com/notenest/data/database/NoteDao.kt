package com.notenest.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Flow<Note?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdSync(id: Int): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePinStatus(id: Int, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isArchived = :isArchived, isPinned = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateArchiveStatus(id: Int, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT DISTINCT category FROM notes WHERE category IS NOT NULL AND category != ''")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM notes WHERE isArchived = 0")
    fun getActiveNoteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isArchived = 1")
    fun getArchivedNoteCount(): Flow<Int>
}
