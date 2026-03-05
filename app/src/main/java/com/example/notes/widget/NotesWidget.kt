package com.example.notes.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.notes.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private val WidgetBg = Color(0xFF1A1A1A)
private val WidgetText = Color(0xFFEDEDED)
private val WidgetHint = Color(0xFF555555)
private val WidgetAccent = Color(0xFF7C5CFC)
private val WidgetSubtle = Color(0xFF8A8A8A)

class NotesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val notes = fetchRecentNotes()

        provideContent {
            NotesWidgetContent(notes)
        }
    }

    private suspend fun fetchRecentNotes(): List<WidgetNoteItem> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val db = FirebaseFirestore.getInstance()

        return try {
            val partnershipQuery = db.collection("partnerships")
                .whereEqualTo("partner1Uid", uid)
                .limit(1)
                .get()
                .await()

            val partnership = partnershipQuery.documents.firstOrNull()
                ?: db.collection("partnerships")
                    .whereEqualTo("partner2Uid", uid)
                    .limit(1)
                    .get()
                    .await()
                    .documents.firstOrNull()
                ?: return emptyList()

            val partnershipId = partnership.id

            val docsSnapshot = db.collection("partnerships")
                .document(partnershipId)
                .collection("noteDocuments")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(8)
                .get()
                .await()

            docsSnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                WidgetNoteItem(
                    title = data["title"] as? String ?: "",
                    content = data["content"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Composable
private fun NotesWidgetContent(notes: List<WidgetNoteItem>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(WidgetBg))
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NOTES",
                style = TextStyle(
                    color = ColorProvider(WidgetText),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${notes.size}",
                style = TextStyle(
                    color = ColorProvider(WidgetAccent),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        if (notes.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notes yet",
                    style = TextStyle(
                        color = ColorProvider(WidgetHint),
                        fontSize = 13.sp
                    )
                )
            }
        } else {
            LazyColumn {
                items(notes) { note ->
                    NoteWidgetRow(note)
                }
            }
        }
    }
}

@Composable
private fun NoteWidgetRow(note: WidgetNoteItem) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent bar
            Box(
                modifier = GlanceModifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(ColorProvider(WidgetAccent))
            ) {}

            Spacer(modifier = GlanceModifier.width(10.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    style = TextStyle(
                        color = ColorProvider(WidgetText),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                if (note.content.isNotEmpty()) {
                    Text(
                        text = note.content,
                        style = TextStyle(
                            color = ColorProvider(WidgetSubtle),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

data class WidgetNoteItem(
    val title: String,
    val content: String
)

class NotesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}
