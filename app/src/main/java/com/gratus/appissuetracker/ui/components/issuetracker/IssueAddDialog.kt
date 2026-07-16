/*
 * Issue Tracker
 * Copyright (C) 2026 spewedprojects <rkharat98@live.com>
 *
 * This file is part of Issue Tracker Application.
 *
 * Issue Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See the LICENSE file for details.
 */

package com.gratus.appissuetracker.ui.components.issuetracker

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.data.TrackedApp
import com.gratus.appissuetracker.ui.components.DiscardChangesDialog
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.theme.dialogContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueAddDialog(
    initialItem: IssueItem?,
    app: TrackedApp,
    issues: List<IssueItem>,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String, priority: String, appVersion: String?) -> Unit
) {
    var hasChanges by remember { mutableStateOf(false) }
    var showDiscardConfirmation by remember { mutableStateOf(false) }

    val handleDismissRequest = {
        if (hasChanges) {
            showDiscardConfirmation = true
        } else {
            onDismiss()
        }
    }

    Dialog(onDismissRequest = handleDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        IssueAddDialogContent(
            initialItem = initialItem,
            app = app,
            issues = issues,
            onDismiss = handleDismissRequest,
            onSave = onSave,
            onHasChangesChanged = { hasChanges = it }
        )
    }

    if (showDiscardConfirmation) {
        DiscardChangesDialog(
            onConfirm = {
                showDiscardConfirmation = false
                onDismiss()
            },
            onDismiss = {
                showDiscardConfirmation = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueAddDialogContent(
    initialItem: IssueItem?,
    app: TrackedApp,
    issues: List<IssueItem>,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String, priority: String, appVersion: String?) -> Unit,
    modifier: Modifier = Modifier,
    onHasChangesChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(initialItem?.title ?: "") }
    var description by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(initialItem?.description ?: "")) }
    var category by rememberSaveable { mutableStateOf(initialItem?.category ?: "Issue") }
    var priority by rememberSaveable { mutableStateOf(initialItem?.let { IssueItem.getPriorityLabel(it.priority) } ?: "Normal") }

    val prefilledVersionName = remember {
        if (initialItem != null) {
            initialItem.appVersion ?: ""
        } else {
            if (app.packageName != null) {
                try {
                    context.packageManager.getPackageInfo(app.packageName, 0).versionName ?: app.versionName
                } catch (e: Exception) {
                    app.versionName
                }
            } else {
                val lastUsedVersion = issues.firstOrNull { !it.appVersion.isNullOrBlank() }?.appVersion
                lastUsedVersion ?: app.versionName.ifBlank { "1.0.0" }
            }
        }
    }

    val isVersionEditable = remember {
        if (initialItem != null) {
            false
        } else {
            if (app.packageName == null) {
                true
            } else {
                app.versionName.isBlank()
            }
        }
    }

    var versionName by rememberSaveable { mutableStateOf(prefilledVersionName) }
    
    val initialTitle = remember { initialItem?.title ?: "" }
    val initialDescription = remember { initialItem?.description ?: "" }
    val initialCategory = remember { initialItem?.category ?: "Issue" }
    val initialPriority = remember { initialItem?.let { IssueItem.getPriorityLabel(it.priority) } ?: "Normal" }

    val hasChanges = remember(title, description.text, category, priority, versionName) {
        title != initialTitle ||
        description.text != initialDescription ||
        category != initialCategory ||
        priority != initialPriority ||
        versionName != prefilledVersionName
    }

    LaunchedEffect(hasChanges) {
        onHasChangesChanged(hasChanges)
    }

    val categories = listOf("Issue", "Feature", "Idea")
    val priorities = listOf("Low", "Normal", "High")

    Card(
        modifier = modifier
            .fillMaxWidth(0.88f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.dialogContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (initialItem == null) "New Issue" else "Edit Issue",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = versionName,
                onValueChange = { versionName = it },
                label = { Text("Version Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = isVersionEditable,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Category", fontSize = AppFontSizes.small)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val catColor = getCategoryColor(cat)
                    val isSelected = category == cat

                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = catColor.copy(alpha = 0.2f),
                            selectedLabelColor = catColor,
                            containerColor = Color.Transparent,
                            labelColor = catColor.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(
                            color = if (isSelected) catColor else catColor.copy(alpha = 0.4f),
                            width = 1.5.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text("Priority", fontSize = AppFontSizes.small)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                priorities.forEach { prio ->
                    val prioColor = getPriorityColor(prio)
                    val isSelected = priority == prio

                    FilterChip(
                        selected = isSelected,
                        onClick = { priority = prio },
                        label = { Text(prio) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = prioColor.copy(alpha = 0.2f),
                            selectedLabelColor = prioColor,
                            containerColor = Color.Transparent,
                            labelColor = prioColor.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(
                            color = if (isSelected) prioColor else prioColor.copy(alpha = 0.4f),
                            width = 1.5.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            // Formatting toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = {
                    val text = description.text
                    val selection = description.selection
                    if (selection.start != selection.end) {
                        val selectedText = text.substring(selection.start, selection.end)
                        val formatted = "**$selectedText**"
                        val newText = text.replaceRange(selection.start, selection.end, formatted)
                        description = TextFieldValue(
                            text = newText,
                            selection = TextRange(selection.start + 2, selection.start + 2 + selectedText.length)
                        )
                    } else {
                        Toast.makeText(context, "Select text to format bold", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold")
                }
                IconButton(
                    onClick = {
                    val text = description.text
                    val selection = description.selection
                    if (selection.start != selection.end) {
                        val selectedText = text.substring(selection.start, selection.end)
                        val formatted = "__${selectedText}__"
                        val newText = text.replaceRange(selection.start, selection.end, formatted)
                        description = TextFieldValue(newText, TextRange(selection.start + formatted.length))
                    } else {
                        Toast.makeText(context, "Select text to format italic", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
                }
                IconButton(onClick = {
                    val text = description.text
                    val selection = description.selection
                    val newText = text.replaceRange(selection.start, selection.start, "- ")
                    description = TextFieldValue(newText, TextRange(selection.start + 2))
                }) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet List")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSave(title, description.text, category, priority, versionName) },
                    enabled = title.isNotBlank() && (versionName.isNotBlank() || !isVersionEditable)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Add/Edit Issue Dialog", backgroundColor = 0xFF000000)
@Composable
fun IssueAddDialogPreview() {
    val previewIssues = listOf(
        IssueItem(
            id = "1342",
            serialNumber = 3,
            title = "Fix crash on login screen",
            description = "NullPointerException when tapping login button rapidly.",
            category = "Issue",
            priority = 1,
            isClosed = false,
            timestamp = System.currentTimeMillis() - 86400000
        )
    )
    SoftTodoTheme(colorSchemeType = "minimal", themeMode = "light") {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            IssueAddDialogContent(
                initialItem = previewIssues[0],
                app = TrackedApp("1", "Mock App", "com.mock", "1.0.0", isCustom = false),
                issues = previewIssues,
                onDismiss = {},
                onSave = { _, _, _, _, _ -> }
            )
        }
    }
}
