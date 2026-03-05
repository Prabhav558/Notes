package com.example.notes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notes.data.NoteItem
import com.example.notes.ui.theme.*

@Composable
fun NoteItemCard(
    note: NoteItem,
    partnerGender: String,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (partnerGender == "woman") PartnerPink else PartnerBlue
    val animatedAccent by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(300),
        label = "accent"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(start = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                .background(animatedAccent)
        )

        if (note.isTask) {
            Checkbox(
                checked = note.isCompleted,
                onCheckedChange = { onToggle(!note.isCompleted) },
                modifier = Modifier.padding(start = 8.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = GreenCheck,
                    uncheckedColor = TextHint,
                    checkmarkColor = DarkBackground
                )
            )
        } else {
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(
            text = note.text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (note.isCompleted) TextHint else TextPrimary,
            textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp, horizontal = 4.dp)
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Delete",
                tint = TextHint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
