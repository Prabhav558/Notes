package com.example.notes.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
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
private val WidgetOrange = Color(0xFFFF8C42)

class TasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = fetchPendingTasks()
        provideContent {
            TasksWidgetContent(tasks)
        }
    }

    private suspend fun fetchPendingTasks(): List<WidgetTaskItem> {
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

            val tasksSnapshot = db.collection("partnerships")
                .document(partnershipId)
                .collection("tasks")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            tasksSnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val isCompleted = data["isCompleted"] as? Boolean ?: false
                if (isCompleted) return@mapNotNull null
                WidgetTaskItem(
                    text = data["text"] as? String ?: "",
                    createdBy = data["createdBy"] as? String ?: ""
                )
            }.take(10)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Composable
private fun TasksWidgetContent(tasks: List<WidgetTaskItem>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBg)
            .cornerRadius(16.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TASKS",
                style = TextStyle(
                    color = ColorProvider(WidgetText),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${tasks.size}",
                style = TextStyle(
                    color = ColorProvider(WidgetOrange),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No pending tasks",
                    style = TextStyle(
                        color = ColorProvider(WidgetHint),
                        fontSize = 13.sp
                    )
                )
            }
        } else {
            LazyColumn {
                items(tasks) { task ->
                    TaskWidgetRow(task)
                }
            }
        }
    }
}

@Composable
private fun TaskWidgetRow(task: WidgetTaskItem) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(8.dp)
                .cornerRadius(4.dp)
                .background(WidgetOrange)
        ) {}

        Spacer(modifier = GlanceModifier.width(10.dp))

        Text(
            text = task.text,
            style = TextStyle(
                color = ColorProvider(WidgetText),
                fontSize = 13.sp
            ),
            maxLines = 2,
            modifier = GlanceModifier.defaultWeight()
        )
    }
}

data class WidgetTaskItem(
    val text: String,
    val createdBy: String
)

class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasksWidget()
}
