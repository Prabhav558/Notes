package com.example.notes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes.ui.theme.*
import com.example.notes.viewmodel.AuthViewModel

@Composable
fun PairingScreen(
    viewModel: AuthViewModel,
    onPairingComplete: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var partnerCode by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connect",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )

            Text(
                text = "Link with your partner",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Option 1: Partner already registered
            PairingOptionCard(
                icon = Icons.Outlined.Link,
                title = "Partner already registered",
                subtitle = "Enter the code your partner shared",
                selected = selectedOption == 1,
                onClick = { selectedOption = 1 }
            )

            AnimatedVisibility(visible = selectedOption == 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = partnerCode,
                        onValueChange = { if (it.length <= 6) partnerCode = it },
                        label = { Text("6-digit code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            letterSpacing = 8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = PrimaryAccent,
                            unfocusedLabelColor = TextHint,
                            cursorColor = PrimaryAccent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (partnerCode.length == 6) {
                                    viewModel.joinWithCode(partnerCode, onPairingComplete)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.joinWithCode(partnerCode, onPairingComplete)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = partnerCode.length == 6 && !authState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryAccent,
                            disabledContainerColor = PrimaryAccentDim.copy(alpha = 0.3f)
                        )
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TextPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Connect", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option 2: Get code for partner
            PairingOptionCard(
                icon = Icons.Outlined.PersonAdd,
                title = "Get code for partner",
                subtitle = "Share this code after your partner registers",
                selected = selectedOption == 2,
                onClick = {
                    selectedOption = 2
                    if (authState.pairingCode == null) {
                        viewModel.generatePairingCode()
                    }
                }
            )

            AnimatedVisibility(visible = selectedOption == 2) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (authState.isLoading && authState.pairingCode == null) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = PrimaryAccent,
                            strokeWidth = 2.dp
                        )
                    } else if (authState.pairingCode != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurface)
                                .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Your code",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = authState.pairingCode!!,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryAccent,
                                    letterSpacing = 8.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(
                                            AnnotatedString(authState.pairingCode!!)
                                        )
                                    }
                                ) {
                                    Text(
                                        "Copy code",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Share this code with your partner.\nOnce they enter it, you'll be connected.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHint,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            AnimatedVisibility(visible = authState.error != null) {
                Text(
                    text = authState.error ?: "",
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun PairingOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) DarkSurfaceVariant else DarkSurface)
            .border(
                width = 1.5.dp,
                color = if (selected) PrimaryAccent else DividerColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
            .animateContentSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) PrimaryAccent else TextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) TextPrimary else TextSecondary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
