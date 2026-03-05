package com.example.notes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.data.NoteDocument
import com.example.notes.data.NoteItem
import com.example.notes.data.Partnership
import com.example.notes.ui.components.NoteDocumentCard
import com.example.notes.ui.components.NoteItemCard
import com.example.notes.ui.theme.*
import com.example.notes.viewmodel.AuthViewModel
import com.example.notes.viewmodel.NotesViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onOpenDocument: (String) -> Unit
) {
    val notesViewModel: NotesViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val noteDocuments by notesViewModel.noteDocuments.collectAsState()
    val tasks by notesViewModel.tasks.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val partnership = authState.partnership

    LaunchedEffect(partnership) {
        partnership?.let {
            notesViewModel.initialize(it.id, currentUid)
        }
    }

    val partnerName = remember(partnership, currentUid) {
        if (partnership == null) ""
        else if (partnership.partner1Uid == currentUid) partnership.partner2Name
        else partnership.partner1Name
    }

    val tabs = listOf("NOTES", "TASKS")

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column(
                modifier = Modifier.background(DarkBackground)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "NOTES",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            if (partnerName.isNotEmpty()) {
                                Text(
                                    text = "with $partnerName",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextHint
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Logout",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkBackground,
                    contentColor = TextPrimary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            Box(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab])
                                    .height(3.dp)
                                    .padding(horizontal = 40.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(OrangeFab)
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) TextPrimary else TextSecondary,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = OrangeFab,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> NotesTab(
                noteDocuments = noteDocuments,
                notesViewModel = notesViewModel,
                partnership = partnership,
                onOpenDocument = onOpenDocument,
                modifier = Modifier.padding(padding)
            )
            1 -> TasksTab(
                tasks = tasks,
                notesViewModel = notesViewModel,
                partnership = partnership,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showAddSheet) {
        when (selectedTab) {
            0 -> AddDocumentSheet(
                onDismiss = { showAddSheet = false },
                onAdd = { title ->
                    notesViewModel.createNoteDocument(title)
                    showAddSheet = false
                }
            )
            1 -> AddTaskSheet(
                onDismiss = { showAddSheet = false },
                onAdd = { text ->
                    notesViewModel.addTask(text)
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
private fun NotesTab(
    noteDocuments: List<NoteDocument>,
    notesViewModel: NotesViewModel,
    partnership: Partnership?,
    onOpenDocument: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (noteDocuments.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No notes yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap + to create your first note",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = noteDocuments,
                key = { it.id }
            ) { doc ->
                NoteDocumentCard(
                    document = doc,
                    partnerGender = notesViewModel.getDocPartnerColor(doc, partnership),
                    onClick = { onOpenDocument(doc.id) },
                    onDelete = { notesViewModel.deleteNoteDocument(doc.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun TasksTab(
    tasks: List<NoteItem>,
    notesViewModel: NotesViewModel,
    partnership: Partnership?,
    modifier: Modifier = Modifier
) {
    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    if (tasks.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No tasks yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap + to add your first task",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (pendingTasks.isNotEmpty()) {
                item { SectionHeader("Pending") }
                items(
                    items = pendingTasks,
                    key = { it.id }
                ) { task ->
                    NoteItemCard(
                        note = task,
                        partnerGender = notesViewModel.getPartnerColor(task, partnership),
                        onToggle = { notesViewModel.toggleTask(task.id, it) },
                        onDelete = { notesViewModel.deleteTask(task.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
            if (completedTasks.isNotEmpty()) {
                item { SectionHeader("Completed") }
                items(
                    items = completedTasks,
                    key = { it.id }
                ) { task ->
                    NoteItemCard(
                        note = task,
                        partnerGender = notesViewModel.getPartnerColor(task, partnership),
                        onToggle = { notesViewModel.toggleTask(task.id, it) },
                        onDelete = { notesViewModel.deleteTask(task.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = TextSecondary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDocumentSheet(
    onDismiss: () -> Unit,
    onAdd: (title: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DividerColor)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "New Note",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Note title...", color = TextHint) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = PrimaryAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (title.isNotBlank()) onAdd(title)
                    }
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onAdd(title) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeFab,
                    disabledContainerColor = OrangeFab.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    "Create Note",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskSheet(
    onDismiss: () -> Unit,
    onAdd: (text: String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DividerColor)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "New Task",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("What needs to be done?", color = TextHint) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = PrimaryAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (text.isNotBlank()) onAdd(text)
                    }
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onAdd(text) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = text.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeFab,
                    disabledContainerColor = OrangeFab.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    "Add Task",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
