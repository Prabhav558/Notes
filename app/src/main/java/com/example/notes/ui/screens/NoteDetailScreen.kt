package com.example.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.ui.theme.*
import com.example.notes.viewmodel.NoteDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    partnershipId: String,
    documentId: String,
    onBack: () -> Unit
) {
    val viewModel: NoteDetailViewModel = viewModel()
    val document by viewModel.document.collectAsState()

    LaunchedEffect(documentId) {
        viewModel.initialize(partnershipId, documentId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveNow()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveNow()
                        onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val titleValue = document?.title ?: ""
            BasicTextField(
                value = titleValue,
                onValueChange = { viewModel.updateTitle(it) },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(PrimaryAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (titleValue.isEmpty()) {
                            Text(
                                text = "Untitled",
                                style = TextStyle(
                                    color = TextHint,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(16.dp))

            val contentValue = document?.content ?: ""
            BasicTextField(
                value = contentValue,
                onValueChange = { viewModel.updateContent(it) },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(PrimaryAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 300.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (contentValue.isEmpty()) {
                            Text(
                                text = "Start writing...",
                                style = TextStyle(
                                    color = TextHint,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}
