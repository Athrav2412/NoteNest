package com.notenest.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.notenest.MainActivity
import com.notenest.R
import com.notenest.data.database.NoteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "notenest_reminders_channel"
        const val CHANNEL_NAME = "Note Reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule active reminders on boot
            val db = NoteDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                val notes = db.noteDao().getAllNotes().firstOrNull() ?: emptyList()
                val now = System.currentTimeMillis()
                notes.forEach { note ->
                    val reminder = note.reminderTime
                    if (reminder != null && reminder > now && !note.isArchived) {
                        ReminderScheduler.scheduleReminder(
                            context = context,
                            noteId = note.id,
                            title = note.title,
                            content = note.content,
                            triggerAtMillis = reminder
                        )
                    }
                }
            }
            return
        }

        val noteId = intent.getIntExtra(ReminderScheduler.EXTRA_NOTE_ID, 0)
        val noteTitle = intent.getStringExtra(ReminderScheduler.EXTRA_NOTE_TITLE) ?: "Note Reminder"
        val noteContent = intent.getStringExtra(ReminderScheduler.EXTRA_NOTE_CONTENT) ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Create channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled note reminders"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action to open NoteNest
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_note_id", noteId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayText = if (noteContent.isNotBlank()) noteContent else "You have a reminder for this note."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(noteTitle.ifBlank { "Note Reminder" })
            .setContentText(displayText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(displayText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(noteId, notification)
    }
}
