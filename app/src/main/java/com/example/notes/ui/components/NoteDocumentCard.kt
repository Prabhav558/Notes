package com.example.notes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notes.data.NoteDocument
import com.example.notes.ui.theme.*

@Composable
fun NoteDocumentCard(
    document: NoteDocument,
    partnerGender: String,
    onClick: () -> Unit,
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
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                .background(animatedAccent)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Icon(
            Icons.Outlined.Description,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 14.dp)
        ) {
            Text(
                text = document.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (document.content.isNotEmpty()) {
                Text(
                    text = document.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

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
