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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.data.TrackedApp
import com.gratus.appissuetracker.ui.IssueFilter
import com.gratus.appissuetracker.ui.IssueTrackerViewModel
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.dialogContainerColor
import com.gratus.appissuetracker.ui.components.issuetracker.*
import androidx.compose.ui.tooling.preview.Preview
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme

class IssueTrackerViewModelFactory(
    private val application: android.app.Application,
    private val app: TrackedApp
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IssueTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IssueTrackerViewModel(application, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueTrackerScreen(
    app: TrackedApp,
    onBack: () -> Unit,
    colorSchemeType: String,
    viewModel: IssueTrackerViewModel = viewModel(
        key = app.id,
        factory = IssueTrackerViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application,
            app
        )
    )
) {
    LaunchedEffect(app.id) {
        viewModel.refresh()
    }

    val context = LocalContext.current
    val issues by viewModel.issues.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()

    IssueTrackerScreenContent(
        app = app,
        colorSchemeType = colorSchemeType,
        issues = issues,
        searchQuery = searchQuery,
        currentFilter = currentFilter,
        onBack = onBack,
        onLaunch = app.packageName?.let { pkg ->
            {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "App not found or cannot be launched", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onFilterChange = { viewModel.setFilter(it) },
        onExport = { viewModel.exportAndShare(context) },
        onToggleIssue = { viewModel.toggleStatus(it) },
        onDeleteIssue = { viewModel.deleteIssue(it) },
        onUpdateIssue = { viewModel.updateIssue(it) },
        onAddIssue = { title, desc, cat, prio, version -> viewModel.addIssue(title, desc, cat, prio, version) },
        onAddComment = { issue, comment -> viewModel.addComment(issue, comment) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueTrackerScreenContent(
    app: TrackedApp,
    colorSchemeType: String,
    issues: List<IssueItem>,
    searchQuery: String,
    currentFilter: IssueFilter,
    onBack: () -> Unit,
    onLaunch: (() -> Unit)? = null,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (IssueFilter) -> Unit,
    onExport: () -> Unit,
    onToggleIssue: (IssueItem) -> Unit,
    onDeleteIssue: (IssueItem) -> Unit,
    onUpdateIssue: (IssueItem) -> Unit,
    onAddIssue: (String, String, String, String, String?) -> Unit,
    onAddComment: (IssueItem, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var itemToEditId by rememberSaveable { mutableStateOf<String?>(null) }

    val filteredIssues = issues.filter {
        val matchesFilter = when (currentFilter) {
            IssueFilter.ALL -> true
            IssueFilter.OPEN -> !it.isClosed
            IssueFilter.CLOSED -> it.isClosed
        }
        val matchesSearch = it.title.contains(searchQuery, ignoreCase = true) || 
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.serialNumber.toString() == searchQuery || 
                "#${it.serialNumber}".contains(searchQuery, ignoreCase = true) || 
                it.appVersion?.contains(searchQuery, ignoreCase = true) == true
        matchesFilter && matchesSearch
    }.sortedByDescending { it.timestamp }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(app.name, fontWeight = FontWeight.Bold, fontSize = AppFontSizes.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Only show the launch button if onLaunch is provided
                    onLaunch?.let {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Launch,
                                contentDescription = "Launch App")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Issue")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search issues...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, capitalization = KeyboardCapitalization.Sentences)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onExport) {
                            Text(
                                text = "Export",
                                fontSize = AppFontSizes.small,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            listOf(
                                Pair(IssueFilter.ALL, "All (${issues.size})"),
                                Pair(IssueFilter.OPEN, "Open (${issues.count { !it.isClosed }})"),
                                Pair(IssueFilter.CLOSED, "Closed (${issues.count { it.isClosed }})")
                            ).forEach { (opt, label) ->
                                val active = currentFilter == opt
                                Box(
                                    modifier = Modifier
                                        .clickable { onFilterChange(opt) }
                                        .background(
                                            if (active) MaterialTheme.colorScheme.secondaryContainer
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = AppFontSizes.extraSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) MaterialTheme.colorScheme.onSecondaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (issues.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "No issues", modifier = Modifier.size(82.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Text("No issues tracked yet", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text("Tap the + button to add a new issue", fontSize = AppFontSizes.small, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
            } else if (filteredIssues.isEmpty() && searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No issues match your search", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            } else if (filteredIssues.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No issues match the current filter", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 85.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredIssues, key = { it.id }) { issue ->
                        IssueCard(
                            issue = issue,
                            onToggle = { onToggleIssue(issue) },
                            onDelete = { onDeleteIssue(issue) },
                            onEdit = { itemToEditId = issue.id; showAddDialog = true },
                            onAddComment = { comment -> onAddComment(issue, comment) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        val itemToEdit = issues.find { it.id == itemToEditId }
        IssueAddDialog(
            initialItem = itemToEdit,
            app = app,
            issues = issues,
            onDismiss = {
                showAddDialog = false
                itemToEditId = null
            },
            onSave = { title, desc, cat, prio, version ->
                if (itemToEdit != null) {
                    onUpdateIssue(itemToEdit.copy(
                        title = title,
                        description = desc,
                        category = cat,
                        priority = IssueItem.getPriorityFromLabel(prio)
                    ))
                } else {
                    onAddIssue(title, desc, cat, prio, version)
                }
                showAddDialog = false
                itemToEditId = null
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IssueTrackerScreenContentPreview() {
    SoftTodoTheme {
        IssueTrackerScreenContent(
            app = TrackedApp("1", "Example Tracker App", "com.example.tracker", "1.0.0", isCustom = false),
            colorSchemeType = "minimal",
            issues = listOf(
                IssueItem(
                    id = "1",
                    serialNumber = 1,
                    title = "Crash on login button click",
                    description = "Immediate crash when tapping log in.",
                    category = "Issue",
                    priority = 1,
                    isClosed = false,
                    timestamp = System.currentTimeMillis()
                ),
                IssueItem(
                    id = "2",
                    serialNumber = 2,
                    title = "Implement Settings screen",
                    description = "Allow users to toggle dark mode.",
                    category = "Feature",
                    priority = 2,
                    isClosed = true,
                    timestamp = System.currentTimeMillis() - 3600000L,
                    closedTimestamp = System.currentTimeMillis()
                )
            ),
            searchQuery = "",
            currentFilter = IssueFilter.ALL,
            onBack = {},
            onLaunch = {},
            onSearchQueryChange = {},
            onFilterChange = {},
            onExport = {},
            onToggleIssue = {},
            onDeleteIssue = {},
            onUpdateIssue = {},
            onAddIssue = { _, _, _, _, _ -> },
            onAddComment = { _, _ -> }
        )
    }
}