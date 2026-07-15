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

package com.gratus.appissuetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gratus.appissuetracker.data.TrackedApp
import com.gratus.appissuetracker.ui.InstalledAppInfo
import com.gratus.appissuetracker.ui.MainViewModel
import com.gratus.appissuetracker.ui.screens.AppLauncherIcon
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.dialogContainerColor

import androidx.compose.ui.tooling.preview.Preview
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.data.IssueItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportConflictDialog(
    task: MainViewModel.PendingImportTask,
    trackedApps: List<TrackedApp>,
    installedApps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onImport: (
        targetOption: Int,
        customName: String,
        customVersion: String,
        selectedTrackedApp: TrackedApp?,
        selectedInstalledApp: InstalledAppInfo?
    ) -> Unit,
    initialTargetOption: Int = 0
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
        ImportConflictDialogContent(
            task = task,
            trackedApps = trackedApps,
            installedApps = installedApps,
            onDismiss = handleDismissRequest,
            onImport = onImport,
            initialTargetOption = initialTargetOption,
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
fun ImportConflictDialogContent(
    task: MainViewModel.PendingImportTask,
    trackedApps: List<TrackedApp>,
    installedApps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onImport: (
        targetOption: Int, // 0 = New Custom, 1 = Existing Tracked, 2 = Installed App
        customName: String,
        customVersion: String,
        selectedTrackedApp: TrackedApp?,
        selectedInstalledApp: InstalledAppInfo?
    ) -> Unit,
    initialTargetOption: Int = 0,
    modifier: Modifier = Modifier,
    onHasChangesChanged: (Boolean) -> Unit = {}
) {
    var targetOption by remember { mutableStateOf(initialTargetOption) } // 0 = New Custom, 1 = Existing, 2 = Installed
    
    // Parse default app name from filename
    val defaultAppName = remember(task.fileName) {
        if (task.fileName.startsWith("issues_")) {
            val temp = task.fileName.removePrefix("issues_")
            val exportIndex = temp.indexOf("_export_")
            if (exportIndex != -1) {
                temp.substring(0, exportIndex).replace("_", " ")
            } else {
                temp.removeSuffix(".json").replace("_", " ")
            }
        } else {
            task.fileName.removeSuffix(".json").replace("_", " ")
        }
    }

    var customName by remember { mutableStateOf(defaultAppName) }
    var customVersion by remember { mutableStateOf("1.0.0") }

    var selectedTrackedApp by remember { mutableStateOf<TrackedApp?>(null) }
    var selectedInstalledApp by remember { mutableStateOf<InstalledAppInfo?>(null) }

    val hasChanges = remember(targetOption, customName, customVersion, selectedTrackedApp, selectedInstalledApp) {
        targetOption != initialTargetOption ||
        customName != defaultAppName ||
        customVersion != "1.0.0" ||
        selectedTrackedApp != null ||
        selectedInstalledApp != null
    }

    LaunchedEffect(hasChanges) {
        onHasChangesChanged(hasChanges)
    }

    var trackedSearchQuery by remember { mutableStateOf("") }
    var installedSearchQuery by remember { mutableStateOf("") }

    val filteredTrackedApps = remember(trackedApps, trackedSearchQuery) {
        if (trackedSearchQuery.isBlank()) trackedApps
        else trackedApps.filter { it.name.contains(trackedSearchQuery, ignoreCase = true) }
    }

    val filteredInstalledApps = remember(installedApps, installedSearchQuery) {
        if (installedSearchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.name.contains(installedSearchQuery, ignoreCase = true) ||
            it.packageName.contains(installedSearchQuery, ignoreCase = true)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(0.85f)
            .heightIn(max = 750.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.dialogContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Customize JSON Import",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "File: ${task.fileName} (${task.issues.size} issues)",
                        fontSize = AppFontSizes.extraSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Selector tabs
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Import Destination", fontSize = AppFontSizes.small, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    ) {
                        val options = listOf("New App", "Tracked App", "Installed App")
                        options.forEachIndexed { idx, label ->
                            val active = targetOption == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { targetOption = idx }
                                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = AppFontSizes.micro,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Dynamic inputs based on selection
                Box(modifier = Modifier.weight(1f)) {
                    when (targetOption) {
                        0 -> {
                            // New custom project inputs
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    label = { Text("App/Project Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = customVersion,
                                    onValueChange = { customVersion = it },
                                    label = { Text("Version") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                        1 -> {
                            // Merge into existing tracked app
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = trackedSearchQuery,
                                    onValueChange = { trackedSearchQuery = it },
                                    placeholder = { Text("Search tracked projects...", fontSize = AppFontSizes.small) },
                                    singleLine = true,
                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                if (filteredTrackedApps.isEmpty()) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("No tracked apps found", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                                        contentPadding = PaddingValues(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(filteredTrackedApps) { app ->
                                            val isSelected = selectedTrackedApp?.id == app.id
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                                    .clickable { selectedTrackedApp = app }
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                AppLauncherIcon(packageName = app.packageName ?: app.name, modifier = Modifier.size(32.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = app.name, fontWeight = FontWeight.Bold, fontSize = AppFontSizes.small, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text(text = "v${app.versionName}", fontSize = AppFontSizes.pico, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                                }
                                                if (isSelected) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Merge into installed app
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = installedSearchQuery,
                                    onValueChange = { installedSearchQuery = it },
                                    placeholder = { Text("Search installed apps...", fontSize = AppFontSizes.small) },
                                    singleLine = true,
                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                if (filteredInstalledApps.isEmpty()) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("No installed apps found", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                                        contentPadding = PaddingValues(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(filteredInstalledApps) { appInfo ->
                                            val isSelected = selectedInstalledApp?.packageName == appInfo.packageName
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                                    .clickable { selectedInstalledApp = appInfo }
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                AppLauncherIcon(packageName = appInfo.packageName, modifier = Modifier.size(32.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = appInfo.name, fontWeight = FontWeight.Bold, fontSize = AppFontSizes.small, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text(text = appInfo.packageName, fontSize = AppFontSizes.pico, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                if (isSelected) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Skip")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onImport(
                                targetOption,
                                customName,
                                customVersion,
                                selectedTrackedApp,
                                selectedInstalledApp
                            )
                        },
                        enabled = when (targetOption) {
                            0 -> customName.isNotBlank()
                            1 -> selectedTrackedApp != null
                            2 -> selectedInstalledApp != null
                            else -> false
                        }
                    ) {
                        Text("Import")
                    }
                }
            }
        }
}

private val mockTask = MainViewModel.PendingImportTask(
    uri = android.net.Uri.EMPTY,
    fileName = "issues_RocketApp_export_2026.json",
    issues = listOf(
        IssueItem(serialNumber = 1, title = "Crash on launch", description = "", category = "Issue", isClosed = false, timestamp = 0L),
        IssueItem(serialNumber = 2, title = "Settings tab addition", description = "", category = "Feature", isClosed = false, timestamp = 0L)
    )
)

private val mockTrackedApps = listOf(
    TrackedApp("1", "RocketApp", "com.example.rocket", "1.2.3", isCustom = false),
    TrackedApp("2", "ChatFlow", null, "0.1.0", isCustom = true)
)

private val mockInstalledApps = listOf(
    InstalledAppInfo("Gmail", "com.google.android.gm", "2023.05"),
    InstalledAppInfo("WhatsApp", "com.whatsapp", "2.23.1")
)

@Preview(showBackground = true)
@Composable
fun ImportConflictDialogNewAppPreview() {
    SoftTodoTheme {
        ImportConflictDialogContent(
            task = mockTask,
            trackedApps = mockTrackedApps,
            installedApps = mockInstalledApps,
            onDismiss = {},
            onImport = { _, _, _, _, _ -> },
            initialTargetOption = 0
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImportConflictDialogTrackedAppPreview() {
    SoftTodoTheme {
        ImportConflictDialogContent(
            task = mockTask,
            trackedApps = mockTrackedApps,
            installedApps = mockInstalledApps,
            onDismiss = {},
            onImport = { _, _, _, _, _ -> },
            initialTargetOption = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImportConflictDialogInstalledAppPreview() {
    SoftTodoTheme {
        ImportConflictDialogContent(
            task = mockTask,
            trackedApps = mockTrackedApps,
            installedApps = mockInstalledApps,
            onDismiss = {},
            onImport = { _, _, _, _, _ -> },
            initialTargetOption = 2
        )
    }
}
