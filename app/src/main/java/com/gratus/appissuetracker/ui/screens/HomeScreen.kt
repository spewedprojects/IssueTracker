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

package com.gratus.appissuetracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.appissuetracker.R
import com.gratus.appissuetracker.data.TrackedApp
import com.gratus.appissuetracker.ui.InstalledAppInfo
import com.gratus.appissuetracker.ui.MainViewModel
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.theme.dialogContainerColor
import com.gratus.appissuetracker.ui.components.home.*
import com.gratus.appissuetracker.ui.components.DeleteConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTracker: (TrackedApp, String?) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val apps by viewModel.apps.collectAsState()
    val openCounts by viewModel.openIssuesCounts.collectAsState()
    val totalCounts by viewModel.totalIssuesCounts.collectAsState()
    val searchQuery by viewModel.globalSearchQuery.collectAsState()
    val searchResults by viewModel.globalSearchResults.collectAsState()
    val installedAppsList by viewModel.installedApps.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    if (isSearchActive) {
        BackHandler {
            isSearchActive = false
            viewModel.setGlobalSearchQuery("")
        }
        // Whole screen search layout
        SearchScreenOverlay(
            searchQuery = searchQuery,
            searchResults = searchResults,
            searchHistory = searchHistory,
            onSearchQueryChange = { viewModel.setGlobalSearchQuery(it) },
            onClearSearch = { viewModel.setGlobalSearchQuery("") },
            onExitSearch = {
                isSearchActive = false
                viewModel.setGlobalSearchQuery("")
            },
            onNavigateToTracker = { app, issueId ->
                if (searchQuery.isNotBlank()) {
                    viewModel.saveSearchToHistory(searchQuery)
                }
                onNavigateToTracker(app, issueId)
            },
            onDeleteHistoryItem = { viewModel.deleteSearchFromHistory(it) },
            onSaveSearchQuery = { viewModel.saveSearchToHistory(it) }
        )
    } else {
        HomeScreenContent(
            apps = apps,
            openCounts = openCounts,
            totalCounts = totalCounts,
            installedAppsList = installedAppsList,
            onAddCustom = { name, version ->
                viewModel.addApp(name, null, version, true)
            },
            onAddInstalled = { appInfos ->
                appInfos.forEach { appInfo ->
                    viewModel.addApp(appInfo.name, appInfo.packageName, appInfo.versionName, false)
                }
            },
            onDeleteApp = { viewModel.deleteApp(it) },
            onLoadInstalledApps = { viewModel.loadInstalledApps(context) },
            onNavigateToTracker = { app -> onNavigateToTracker(app, null) },
            onNavigateToSettings = onNavigateToSettings,
            onEnterSearch = { isSearchActive = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    apps: List<TrackedApp>,
    openCounts: Map<String, Int>,
    totalCounts: Map<String, Int>,
    installedAppsList: List<InstalledAppInfo>,
    onAddCustom: (String, String) -> Unit,
    onAddInstalled: (List<InstalledAppInfo>) -> Unit,
    onDeleteApp: (TrackedApp) -> Unit,
    onLoadInstalledApps: () -> Unit,
    onNavigateToTracker: (TrackedApp) -> Unit,
    onNavigateToSettings: () -> Unit,
    onEnterSearch: () -> Unit
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutPopup by rememberSaveable { mutableStateOf(false) }
    var appToDelete by remember { mutableStateOf<TrackedApp?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Issue Tracker",
                            fontWeight = FontWeight.Bold,
                            fontSize = AppFontSizes.title
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(painterResource(R.drawable.manufacturing_48px), contentDescription = "Settings")
                    }
                },
                actions = {
                    // Outlined square search box icon
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onEnterSearch() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        val topPadding = paddingValues.calculateTopPadding()
        val bottomPadding = paddingValues.calculateBottomPadding()
        val density = LocalDensity.current
        val statusBarHeightPx = WindowInsets.statusBars.getTop(density).toFloat()

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding, bottom = bottomPadding + 80.dp)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EmptyFolderIllustration()

                        Text(
                            text = "No applications added yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = AppFontSizes.title
                        )
                        Text(
                            text = "Add an application to start tracking issues and stay organized.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        // Center add button with dotted-looking circle icon
                        Button(
                            onClick = {
                                onLoadInstalledApps()
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .border(
                                            1.5.dp,
                                            MaterialTheme.colorScheme.onPrimaryContainer,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "Add Application",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = topPadding + 8.dp,
                        bottom = bottomPadding + 64.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .fadingEdges(
                            statusBarHeightPx = statusBarHeightPx,
                            density = density,
                            topPadding = topPadding,
                            bottomPadding = bottomPadding,
                            footerHeight = 56.dp
                        )
                ) {
                    items(apps, key = { it.id }) { app ->
                        val openCount = openCounts[app.id] ?: 0
                        val totalCount = totalCounts[app.id] ?: 0
                        AppGridCard(
                            app = app,
                            totalIssues = totalCount,
                            openIssues = openCount,
                            onClick = { onNavigateToTracker(app) },
                            onLongClick = { appToDelete = app }
                        )
                    }
                    item {
                        AddApplicationGridCard(
                            onClick = {
                                onLoadInstalledApps()
                                showAddDialog = true
                            }
                        )
                    }
                }
            }

            // Bottom check footer
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(
                        bottom = bottomPadding + 12.dp,
                        top = 32.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "All data is stored locally on your device.\nYou can export your issues anytime.",
                    fontSize = AppFontSizes.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    IconButton(
                        onClick = { showAboutPopup = !showAboutPopup },
                        modifier = Modifier
                            .background(
                                color = if (showAboutPopup) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.github_mark),
                            contentDescription = "About and source code on GitHub",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (showAboutPopup) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showAboutPopup = false
                        }
                )
                
                BackHandler {
                    showAboutPopup = false
                }
            }

            AnimatedVisibility(
                visible = showAboutPopup,
                enter = fadeIn(animationSpec = tween(durationMillis = 250)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 250)
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 200)) + slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 200)
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = bottomPadding + 68.dp
                    )
            ) {
                AboutPopupContent()
            }
        }
    }

    // Add App/Project Dialog
    if (showAddDialog) {
        AddAppDialog(
            installedApps = installedAppsList,
            onDismiss = { showAddDialog = false },
            onAddCustom = { name, version ->
                onAddCustom(name, version)
                showAddDialog = false
            },
            onAddInstalled = { appInfos ->
                onAddInstalled(appInfos)
                showAddDialog = false
            }
        )
    }

    // Delete App Confirmation Dialog
    if (appToDelete != null) {
        DeleteConfirmationDialog(
            title = "Delete Project",
            message = "Are you sure you want to remove '${appToDelete?.name}'? This will permanently delete all its tracked issues.",
            onConfirm = {
                appToDelete?.let { onDeleteApp(it) }
                appToDelete = null
            },
            onDismiss = { appToDelete = null }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenContentEmptyPreview() {
    SoftTodoTheme {
        HomeScreenContent(
            apps = emptyList(),
            openCounts = emptyMap(),
            totalCounts = emptyMap(),
            installedAppsList = emptyList(),
            onAddCustom = { _, _ -> },
            onAddInstalled = {},
            onDeleteApp = {},
            onLoadInstalledApps = {},
            onNavigateToTracker = {},
            onNavigateToSettings = {},
            onEnterSearch = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenContentWithAppsPreview() {
    SoftTodoTheme {
        HomeScreenContent(
            apps = listOf(
                TrackedApp("1", "App One", "com.example.one", "1.0.0", isCustom = false),
                TrackedApp("2", "App Two Custom", null, "2.1.0", isCustom = true),
                TrackedApp("3", "App Three", "com.example.three", "0.5.1", isCustom = false)
            ),
            openCounts = mapOf("1" to 3, "2" to 0, "3" to 12),
            totalCounts = mapOf("1" to 10, "2" to 2, "3" to 34),
            installedAppsList = emptyList(),
            onAddCustom = { _, _ -> },
            onAddInstalled = {},
            onDeleteApp = {},
            onLoadInstalledApps = {},
            onNavigateToTracker = {},
            onNavigateToSettings = {},
            onEnterSearch = {}
        )
    }
}
