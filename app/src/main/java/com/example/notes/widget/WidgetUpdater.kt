package com.example.notes.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

suspend fun updateAllWidgets(context: Context) {
    try {
        val manager = GlanceAppWidgetManager(context)

        val tasksWidget = TasksWidget()
        manager.getGlanceIds(TasksWidget::class.java).forEach { glanceId ->
            tasksWidget.update(context, glanceId)
        }

        val notesWidget = NotesWidget()
        manager.getGlanceIds(NotesWidget::class.java).forEach { glanceId ->
            notesWidget.update(context, glanceId)
        }
    } catch (_: Exception) {}
}
