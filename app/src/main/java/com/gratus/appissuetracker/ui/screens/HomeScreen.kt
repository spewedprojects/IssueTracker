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

import android.content.Intent
import android.widget.Space
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.gratus.appissuetracker.R
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.data.TrackedApp
import com.gratus.appissuetracker.ui.InstalledAppInfo
import com.gratus.appissuetracker.ui.MainViewModel
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.theme.dialogContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTracker: (TrackedApp) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val apps by viewModel.apps.collectAsState()
    val openCounts by viewModel.openIssuesCounts.collectAsState()
    val totalCounts by viewModel.totalIssuesCounts.collectAsState()
    val searchQuery by viewModel.globalSearchQuery.collectAsState()
    val searchResults by viewModel.globalSearchResults.collectAsState()
    val installedAppsList by viewModel.installedApps.collectAsState()

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
            onSearchQueryChange = { viewModel.setGlobalSearchQuery(it) },
            onClearSearch = { viewModel.setGlobalSearchQuery("") },
            onExitSearch = {
                isSearchActive = false
                viewModel.setGlobalSearchQuery("")
            },
            onNavigateToTracker = onNavigateToTracker
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
            onNavigateToTracker = onNavigateToTracker,
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
    var appToDelete by remember { mutableStateOf<TrackedApp?>(null) }
    val context = LocalContext.current

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main body
            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
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
                                        .border(1.5.dp, MaterialTheme.colorScheme.onPrimaryContainer, shape = CircleShape),
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
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
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
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 8.dp, start = 16.dp, end = 16.dp),
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
                Spacer(modifier = Modifier.weight(1f)) // pushes next item to the end
                IconButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/spewedprojects/IssueTracker".toUri()
                        )
                        context.startActivity(intent)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.github_mark),
                        contentDescription = "Source code on GitHub",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp) // Adjusted to a more standard size
                    )
                }
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
        AlertDialog(
            onDismissRequest = { appToDelete = null },
            title = { Text("Delete Tracker") },
            text = { Text("Are you sure you want to remove '${appToDelete?.name}'? This will permanently delete all its tracked issues.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        appToDelete?.let { onDeleteApp(it) }
                        appToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.dialogContainerColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridCard(
    app: TrackedApp,
    totalIssues: Int,
    openIssues: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.95f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon Container
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (app.isCustom) {
                    GridLetterAvatar(name = app.name, modifier = Modifier.fillMaxSize())
                } else {
                    GridAppLauncherIcon(packageName = app.id, modifier = Modifier.fillMaxSize())
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Text(
                text = app.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Stats line
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$totalIssues issues",
                    fontSize = AppFontSizes.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = " • ",
                    fontSize = AppFontSizes.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "$openIssues open",
                    fontSize = AppFontSizes.micro,
                    fontWeight = FontWeight.Bold,
                    color = if (openIssues > 0) Color(0xFFE57373) else Color(0xFF4DB6AC)
                )
            }
        }
    }
}

@Composable
fun AddApplicationGridCard(onClick: () -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.95f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
            .clickable { onClick() }
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = outlineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                ),
                cornerRadius = CornerRadius(20.dp.toPx())
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Add Application",
                fontWeight = FontWeight.Bold,
                fontSize = AppFontSizes.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GridLetterAvatar(name: String, modifier: Modifier = Modifier) {
    val char = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFB74D), Color(0xFF4DB6AC), Color(0xFFBA68C8)
    )
    val bgColor = remember(name) {
        colors[name.hashCode().let { if (it < 0) -it else it } % colors.size]
    }
    Box(
        modifier = modifier
            .background(bgColor.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
            .border(1.dp, bgColor.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            fontWeight = FontWeight.Bold,
            fontSize = AppFontSizes.headline,
            color = bgColor
        )
    }
}

object AppIconCache {
    private val cache = java.util.concurrent.ConcurrentHashMap<String, androidx.compose.ui.graphics.ImageBitmap>()

    fun get(packageName: String): androidx.compose.ui.graphics.ImageBitmap? {
        return cache[packageName]
    }

    fun put(packageName: String, bitmap: androidx.compose.ui.graphics.ImageBitmap) {
        cache[packageName] = bitmap
    }
}

@Composable
fun GridAppLauncherIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var imageBitmap by remember(packageName) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(AppIconCache.get(packageName)) }

    if (imageBitmap == null) {
        LaunchedEffect(packageName) {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val pm = context.packageManager
                    val iconDrawable = pm.getApplicationIcon(packageName)
                    val width = iconDrawable.intrinsicWidth.coerceAtLeast(1).coerceAtMost(400)
                    val height = iconDrawable.intrinsicHeight.coerceAtLeast(1).coerceAtMost(400)
                    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    iconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    iconDrawable.draw(canvas)
                    bitmap.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                AppIconCache.put(packageName, bitmap)
                imageBitmap = bitmap
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = null,
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                .padding(12.dp)
        )
    } else {
        GridLetterAvatar(name = packageName, modifier = modifier)
    }
}

@Composable
fun EmptyFolderIllustration(modifier: Modifier = Modifier) {

    val primaryColor = MaterialTheme.colorScheme.primaryContainer // soft purple/indigo
    val outlineColor = MaterialTheme.colorScheme.primary // Indigo accent
    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            
            // Draw folder background
            val path = Path().apply {
                moveTo(10.dp.toPx(), 20.dp.toPx())
                lineTo(45.dp.toPx(), 20.dp.toPx())
                lineTo(55.dp.toPx(), 32.dp.toPx())
                lineTo(110.dp.toPx(), 32.dp.toPx())
                lineTo(110.dp.toPx(), 100.dp.toPx())
                lineTo(10.dp.toPx(), 100.dp.toPx())
                close()
            }
            drawPath(path, color = primaryColor.copy(alpha = 0.3f))
            drawPath(path, color = outlineColor.copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))

            // Draw a sheet of paper peeking out
            val paperPath = Path().apply {
                moveTo(25.dp.toPx(), 10.dp.toPx())
                lineTo(95.dp.toPx(), 10.dp.toPx())
                lineTo(95.dp.toPx(), 60.dp.toPx())
                lineTo(25.dp.toPx(), 60.dp.toPx())
                close()
            }
            drawPath(paperPath, color = Color.White)
            drawPath(paperPath, color = outlineColor.copy(alpha = 0.4f), style = Stroke(width = 1.5.dp.toPx()))
            
            // Eyes
            drawCircle(color = outlineColor, radius = 2.5.dp.toPx(), center = Offset(45.dp.toPx(), 65.dp.toPx()))
            drawCircle(color = outlineColor, radius = 2.5.dp.toPx(), center = Offset(75.dp.toPx(), 65.dp.toPx()))
            // Sad mouth (arc)
            drawArc(
                color = outlineColor,
                startAngle = 00f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(52.dp.toPx(), 72.dp.toPx()),
                size = Size(16.dp.toPx(), 10.dp.toPx()),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenOverlay(
    searchQuery: String,
    searchResults: List<Pair<TrackedApp, IssueItem>>,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onExitSearch: () -> Unit,
    onNavigateToTracker: (TrackedApp) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitSearch) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit search")
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search all issues...") },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .padding(end = 8.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            // Results list
            if (searchQuery.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Type to search all issues...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (searchResults.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No matching issues found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { (app, issue) ->
                        GlobalSearchIssueCard(
                            app = app,
                            issue = issue,
                            onClick = {
                                onNavigateToTracker(app)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchIssueCard(
    app: TrackedApp,
    issue: IssueItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Parent App name badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        AppLauncherIcon(packageName = app.id, modifier = Modifier.size(24.dp))
                        Text(
                            text = app.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = AppFontSizes.extraSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Issue serial #
                Text(
                    text = "#${issue.serialNumber}",
                    fontSize = AppFontSizes.nano,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Issue Title
            Text(
                text = issue.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Category & Priority badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category
                val catColor = when (issue.category) {
                    "Issue" -> Color(0xFFE57373)
                    "Feature" -> Color(0xFF81C784)
                    "Idea" -> Color(0xFF64B5F6)
                    else -> MaterialTheme.colorScheme.secondary
                }
                Surface(
                    color = catColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, catColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Category: " + issue.category,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp),
                        fontSize = AppFontSizes.pico,
                        fontWeight = FontWeight.Bold,
                        color = catColor
                    )
                }

                // Priority
                val prioColor = getPriorityColor(issue.priority) // Use your existing helper
                Surface(
                    color = prioColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, prioColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Priority: " + IssueItem.getPriorityLabel(issue.priority),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp),
                        fontSize = AppFontSizes.pico,
                        fontWeight = FontWeight.Bold,
                        color = prioColor
                    )
                }
            }
        }
    }
}

@Composable
fun AppLauncherIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var imageBitmap by remember(packageName) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(AppIconCache.get(packageName)) }

    if (imageBitmap == null) {
        LaunchedEffect(packageName) {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val pm = context.packageManager
                    val iconDrawable = pm.getApplicationIcon(packageName)
                    val width = iconDrawable.intrinsicWidth.coerceAtLeast(1).coerceAtMost(256)
                    val height = iconDrawable.intrinsicHeight.coerceAtLeast(1).coerceAtMost(256)
                    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    iconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    iconDrawable.draw(canvas)
                    bitmap.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                AppIconCache.put(packageName, bitmap)
                imageBitmap = bitmap
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = null,
            modifier = modifier.clip(CircleShape)
        )
    } else {
        LetterAvatar(name = packageName, modifier = modifier)
    }
}

@Composable
fun LetterAvatar(name: String, modifier: Modifier = Modifier) {
    val char = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFB74D), Color(0xFF4DB6AC), Color(0xFFBA68C8)
    )
    val bgColor = remember(name) {
        colors[name.hashCode().let { if (it < 0) -it else it } % colors.size]
    }
    Box(
        modifier = modifier
            .background(bgColor.copy(alpha = 0.2f), shape = CircleShape)
            .border(1.5.dp, bgColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            fontWeight = FontWeight.Bold,
            fontSize = AppFontSizes.extraLarge,
            color = bgColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppDialog(
    installedApps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onAddCustom: (String, String) -> Unit,
    onAddInstalled: (List<InstalledAppInfo>) -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        AddAppDialogContent(
            installedApps = installedApps,
            onDismiss = onDismiss,
            onAddCustom = onAddCustom,
            onAddInstalled = onAddInstalled
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
    modifier: Modifier = Modifier
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
        Card(
            modifier = Modifier
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

@Preview(showBackground = true)
@Composable
fun HomeScreenContentSearchPreview() {
    SoftTodoTheme {
        SearchScreenOverlay(
            searchQuery = "crash",
            searchResults = listOf(
                Pair(
                    TrackedApp("1", "App One", "com.example.one", "1.0.0", isCustom = false),
                    IssueItem(
                        id = "101",
                        serialNumber = 1,
                        title = "App crashes on startup",
                        description = "Crashes on startup with NullPointerException",
                        category = "Issue",
                        priority = 1,
                        isClosed = false,
                        timestamp = 1234567890L
                    )
                ),
                Pair(
                    TrackedApp("1", "App One", "com.example.one", "1.0.0", isCustom = false),
                    IssueItem(
                        id = "102",
                        serialNumber = 2,
                        title = "Request to support dark mode",
                        description = "Add dark mode toggle to settings",
                        category = "Feature",
                        priority = 2,
                        isClosed = false,
                        timestamp = 1234567890L
                    )
                )
            ),
            onSearchQueryChange = {},
            onClearSearch = {},
            onExitSearch = {},
            onNavigateToTracker = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppGridCardPreview() {
    SoftTodoTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AppGridCard(
                    app = TrackedApp("1", "RocketApp", "com.example.rocket", "1.2.3", isCustom = false),
                    totalIssues = 23,
                    openIssues = 5,
                    onClick = {},
                    onLongClick = {}
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AppGridCard(
                    app = TrackedApp("2", "ChatFlow", null, "0.1.0", isCustom = true),
                    totalIssues = 18,
                    openIssues = 0,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Add App Dialog - Custom Tab", backgroundColor = 0X000)
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

@Preview(showBackground = true, name = "Add App Dialog - Installed Tab", backgroundColor = 0X000)
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
