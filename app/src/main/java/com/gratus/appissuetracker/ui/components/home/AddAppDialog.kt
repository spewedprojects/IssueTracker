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

package com.gratus.appissuetracker.ui.components.home

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gratus.appissuetracker.ui.InstalledAppInfo
import com.gratus.appissuetracker.ui.components.DiscardChangesDialog
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.theme.dialogContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppDialog(
    installedApps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onAddCustom: (String, String) -> Unit,
    onAddInstalled: (List<InstalledAppInfo>) -> Unit
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
        AddAppDialogContent(
            installedApps = installedApps,
            onDismiss = handleDismissRequest,
            onAddCustom = onAddCustom,
            onAddInstalled = onAddInstalled,
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
fun AddAppDialogContent(
    installedApps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onAddCustom: (String, String) -> Unit,
    onAddInstalled: (List<InstalledAppInfo>) -> Unit,
    initialActiveTab: Int = 0,
    modifier: Modifier = Modifier,
    onHasChangesChanged: (Boolean) -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(initialActiveTab) } // 0 = Custom, 1 = Installed
    
    // Custom App fields
    var customName by remember { mutableStateOf("") }
    var customVersion by remember { mutableStateOf("1.0.0") }

    // Installed App fields
    var searchFilter by remember { mutableStateOf("") }
    val filteredApps = remember(installedApps, searchFilter) {
        if (searchFilter.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.name.contains(searchFilter, ignoreCase = true) ||
                it.packageName.contains(searchFilter, ignoreCase = true)
            }
        }
    }
    var selectedApps by remember { mutableStateOf(emptySet<InstalledAppInfo>()) }

    val hasChanges = remember(customName, customVersion, selectedApps) {
        customName.isNotEmpty() || customVersion != "1.0.0" || selectedApps.isNotEmpty()
    }

    LaunchedEffect(hasChanges) {
        onHasChangesChanged(hasChanges)
    }

    Card(
        modifier = modifier
            .fillMaxWidth(0.85f)
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
            Text(
                text = "Add Application",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Segmented tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            ) {
                val tabs = listOf("Custom Project", "Installed App")
                tabs.forEachIndexed { idx, label ->
                    val active = activeTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTab = idx }
                            .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            fontSize = AppFontSizes.medium,
                            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (activeTab == 0) {
                // Custom Project Form
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Project Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = customVersion,
                        onValueChange = { customVersion = it },
                        label = { Text("Version Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            } else {
                // Installed App Selector
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchFilter,
                        onValueChange = { searchFilter = it },
                        placeholder = { Text("Search installed apps...", fontSize = AppFontSizes.small) },
                        singleLine = true,
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (filteredApps.isEmpty()) {
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
                            items(filteredApps) { appInfo ->
                                val isSelected = selectedApps.contains(appInfo)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable {
                                            selectedApps = if (isSelected) {
                                                selectedApps - appInfo
                                            } else {
                                                selectedApps + appInfo
                                            }
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AppLauncherIcon(packageName = appInfo.packageName, modifier = Modifier.size(32.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = appInfo.name, fontWeight = FontWeight.Bold, fontSize = AppFontSizes.medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = appInfo.packageName, fontSize = AppFontSizes.small, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp).padding(end = 8.dp))
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
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (activeTab == 0) {
                            if (customName.isNotBlank()) {
                                onAddCustom(customName.trim(), customVersion.trim())
                            }
                        } else {
                            if (selectedApps.isNotEmpty()) {
                                onAddInstalled(selectedApps.toList())
                            }
                        }
                    },
                    enabled = if (activeTab == 0) customName.isNotBlank() else selectedApps.isNotEmpty()
                ) {
                    Text(if (activeTab == 1 && selectedApps.size > 1) "Add (${selectedApps.size})" else "Add")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Add App Dialog - Custom Tab", backgroundColor = 0xFF000000)
@Composable
fun AddAppDialogCustomTabPreview() {
    SoftTodoTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            AddAppDialogContent(
                installedApps = listOf(
                    InstalledAppInfo("WhatsApp", "com.whatsapp", "2.23.1"),
                    InstalledAppInfo("Gmail", "com.google.android.gm", "2023.05"),
                    InstalledAppInfo("Spotify", "com.spotify.music", "8.8.2")
                ),
                onDismiss = {},
                onAddCustom = { _, _ -> },
                onAddInstalled = {},
                initialActiveTab = 0
            )
        }
    }
}

@Preview(showBackground = true, name = "Add App Dialog - Installed Tab", backgroundColor = 0xFF000000)
@Composable
fun AddAppDialogInstalledTabPreview() {
    SoftTodoTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            AddAppDialogContent(
                installedApps = listOf(
                    InstalledAppInfo("WhatsApp", "com.whatsapp", "2.23.1"),
                    InstalledAppInfo("Gmail", "com.google.android.gm", "2023.05"),
                    InstalledAppInfo("Spotify", "com.spotify.music", "8.8.2")
                ),
                onDismiss = {},
                onAddCustom = { _, _ -> },
                onAddInstalled = {},
                initialActiveTab = 1
            )
        }
    }
}
