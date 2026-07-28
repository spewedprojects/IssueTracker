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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalFocusManager
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
import com.gratus.appissuetracker.ui.components.DeleteConfirmationDialog
import androidx.compose.ui.tooling.preview.Preview
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme

import com.gratus.appissuetracker.ui.components.issuetracker.IssueSort
import com.gratus.appissuetracker.ui.components.issuetracker.IssueSortDropdown
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IssueTrackerScreen(
    app: TrackedApp,
    highlightIssueId: String? = null,
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
    LaunchedEffect(app.id, highlightIssueId) {
        viewModel.refresh()
        if (highlightIssueId != null) {
            viewModel.setFilter(IssueFilter.ALL)
            viewModel.setSearchQuery("") // Clear search query in list so highlighted issue shows up
        }
    }

    val context = LocalContext.current
    val issues by viewModel.issues.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()

    IssueTrackerScreenContent(
        app = app,
        colorSchemeType = colorSchemeType,
        issues = issues,
        searchQuery = searchQuery,
        currentFilter = currentFilter,
        currentCategory = categoryFilter,
        currentSort = sortMode,
        highlightIssueId = highlightIssueId,
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
        onCategoryChange = { viewModel.setCategoryFilter(it) },
        onSortChange = { viewModel.setSortMode(it) },
        onExport = { viewModel.exportAndShare(context) },
        onToggleIssue = { viewModel.toggleStatus(it) },
        onDeleteIssue = { viewModel.deleteIssue(it) },
        onUpdateIssue = { viewModel.updateIssue(it) },
        onAddIssue = { title, desc, cat, prio, version -> viewModel.addIssue(title, desc, cat, prio, version) },
        onAddComment = { issue, comment -> viewModel.addComment(issue, comment) },
        onEditComment = { issue, index, newText -> viewModel.updateComment(issue, index, newText) },
        onDeleteComment = { issue, index -> viewModel.deleteComment(issue, index) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IssueTrackerScreenContent(
    modifier: Modifier = Modifier,
    app: TrackedApp,
    colorSchemeType: String,
    issues: List<IssueItem>,
    searchQuery: String,
    currentFilter: IssueFilter,
    currentCategory: CategoryFilter = CategoryFilter.ALL,
    currentSort: IssueSort = IssueSort.NEWEST,
    highlightIssueId: String? = null,
    onBack: () -> Unit,
    onLaunch: (() -> Unit)? = null,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (IssueFilter) -> Unit,
    onCategoryChange: (CategoryFilter) -> Unit = {},
    onSortChange: (IssueSort) -> Unit = {},
    onExport: () -> Unit,
    onToggleIssue: (IssueItem) -> Unit,
    onDeleteIssue: (IssueItem) -> Unit,
    onUpdateIssue: (IssueItem) -> Unit,
    onAddIssue: (String, String, String, String, String?) -> Unit,
    onAddComment: (IssueItem, String) -> Unit,
    onEditComment: (IssueItem, Int, String) -> Unit = { _, _, _ -> },
    onDeleteComment: (IssueItem, Int) -> Unit = { _, _ -> }
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var itemToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var issueToDelete by remember { mutableStateOf<IssueItem?>(null) }

    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }

    val filteredIssues = issues.filter { issue ->
        val matchesFilter = when (currentFilter) {
            IssueFilter.ALL -> true
            IssueFilter.OPEN -> !issue.isClosed
            IssueFilter.CLOSED -> issue.isClosed
        }
        val matchesCategory = when (currentCategory) {
            CategoryFilter.ALL -> true
            else -> issue.category.equals(currentCategory.categoryName, ignoreCase = true)
        }
        val matchesSearch = issue.title.contains(searchQuery, ignoreCase = true) || 
                issue.description.contains(searchQuery, ignoreCase = true) ||
                issue.serialNumber.toString() == searchQuery || 
                "#${issue.serialNumber}".contains(searchQuery, ignoreCase = true) || 
                issue.appVersion?.contains(searchQuery, ignoreCase = true) == true
        matchesFilter && matchesCategory && matchesSearch
    }.let { list ->
        when (currentSort) {
            IssueSort.HIGHEST_PRIORITY -> list.sortedBy { it.priority }
            IssueSort.LOWEST_PRIORITY -> list.sortedByDescending { it.priority }
            IssueSort.NEWEST -> list.sortedByDescending { it.timestamp }
            IssueSort.OLDEST -> list.sortedBy { it.timestamp }
            IssueSort.MODIFIED -> list.sortedByDescending { it.lastModified }
        }
    }

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
                    Row (modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search issues...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search,
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))
                        var sortDropdownExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { sortDropdownExpanded = true },
                                modifier = Modifier
                                    .background(
                                        color = if (sortDropdownExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Sort Issue List"
                                )
                            }
                            IssueSortDropdown(
                                expanded = sortDropdownExpanded,
                                selectedCategory = currentCategory,
                                onCategorySelected = onCategoryChange,
                                selectedSort = currentSort,
                                onSortSelected = onSortChange,
                                onDismissRequest = { sortDropdownExpanded = false }
                            )
                        }
                    }

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
                val listState = rememberLazyListState()

                // Reset scroll to top when sort mode, filter, or category changes
                LaunchedEffect(currentSort, currentFilter, currentCategory) {
                    listState.scrollToItem(0)
                }

                // Handle issue addition and deep link highlights
                var previousIssueCount by remember { mutableIntStateOf(issues.size) }

                LaunchedEffect(highlightIssueId, filteredIssues, issues.size) {
                    if (highlightIssueId != null) {
                        val index = filteredIssues.indexOfFirst { it.id == highlightIssueId }
                        if (index != -1) {
                            listState.animateScrollToItem(index)
                        }
                    } else if (issues.size > previousIssueCount) {
                        // Scroll to top only if user was already near the top
                        if (listState.firstVisibleItemIndex <= 1) {
                            listState.animateScrollToItem(0)
                        }
                    }
                    previousIssueCount = issues.size
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 85.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredIssues, key = { it.id }) { issue ->
                        IssueCard(
                            issue = issue,
                            highlighted = issue.id == highlightIssueId,
                            onToggle = { onToggleIssue(issue) },
                            onDelete = { issueToDelete = issue },
                            onEdit = { itemToEditId = issue.id; showAddDialog = true },
                            onAddComment = { comment -> onAddComment(issue, comment) },
                            onEditComment = { index, newText -> onEditComment(issue, index, newText) },
                            onDeleteComment = { index -> onDeleteComment(issue, index) }
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

    if (issueToDelete != null) {
        DeleteConfirmationDialog(
            title = "Delete Issue",
            message = "Are you sure you want to permanently delete issue #${issueToDelete?.serialNumber}?",
            onConfirm = {
                issueToDelete?.let { onDeleteIssue(it) }
                issueToDelete = null
            },
            onDismiss = { issueToDelete = null }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IssueTrackerScreenContentPreview() {
    SoftTodoTheme (colorSchemeType = "simple") {
        IssueTrackerScreenContent(
            app = TrackedApp("1", "Example Tracker App", "com.example.tracker", "1.0.0", isCustom = false),
            colorSchemeType = "simple",
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
            highlightIssueId = null,
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