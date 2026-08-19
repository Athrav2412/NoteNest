package com.notenest.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Note::class],
    version = 2,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "notenest_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.noteDao()?.let { dao ->
                                    val now = System.currentTimeMillis()
                                    dao.insertNote(
                                        Note(
                                            title = "Welcome to NoteNest 🪺",
                                            content = "NoteNest is your offline, lightning-fast workspace for thoughts, tasks, and ideas.\n\n• Tap the Pin icon to keep important notes at the top.\n• Organize using categories like Work, Personal, and Ideas.\n• Use the Search bar to quickly find any note.\n• All your notes are automatically saved and stay on your device.",
                                            category = "General",
                                            createdAt = now - 60000,
                                            updatedAt = now - 60000,
                                            isPinned = true,
                                            isArchived = false
                                        )
                                    )
                                    dao.insertNote(
                                        Note(
                                            title = "Project Milestones 🎯",
                                            content = "1. Finalize mobile user flow\n2. Integrate Room local database\n3. Polish Material 3 Dark theme\n4. Review responsive layout on tablets",
                                            category = "Work",
                                            createdAt = now - 120000,
                                            updatedAt = now - 120000,
                                            isPinned = false,
                                            isArchived = false
                                        )
                                    )
                                    dao.insertNote(
                                        Note(
                                            title = "Book Recommendations 📚",
                                            content = "• Design Systems by Alla Kholmatova\n• Clean Architecture by Robert C. Martin\n• Atomic Habits by James Clear",
                                            category = "Ideas",
                                            createdAt = now - 180000,
                                            updatedAt = now - 180000,
                                            isPinned = false,
                                            isArchived = false
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
