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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gratus.appissuetracker.data.IssueComment
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.data.TrackedApp
import com.gratus.appissuetracker.ui.IssueFilter
import com.gratus.appissuetracker.ui.IssueTrackerViewModel
import com.gratus.appissuetracker.ui.components.parseStyledDescription
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.dialogContainerColor
import com.gratus.appissuetracker.ui.utils.DateTimeUtils
import kotlinx.coroutines.delay
import java.util.Calendar
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
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
        appName = app.name,
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
        onAddIssue = { title, desc, cat, prio -> viewModel.addIssue(title, desc, cat, prio) },
        onAddComment = { issue, comment -> viewModel.addComment(issue, comment) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueTrackerScreenContent(
    appName: String,
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
    onAddIssue: (String, String, String, String) -> Unit,
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
                title = { Text(appName, fontWeight = FontWeight.Bold, fontSize = AppFontSizes.title) },
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
            onDismiss = {
                showAddDialog = false
                itemToEditId = null
            },
            onSave = { title, desc, cat, prio ->
                if (itemToEdit != null) {
                    onUpdateIssue(itemToEdit.copy(
                        title = title,
                        description = desc,
                        category = cat,
                        priority = IssueItem.getPriorityFromLabel(prio) // Use 'prio' instead of 'priority'
                    ))
                } else {
                    onAddIssue(title, desc, cat, prio)
                }
                showAddDialog = false
                itemToEditId = null
            }
        )
    }
}

@Composable
fun IssueCard(
    issue: IssueItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAddComment: (String) -> Unit,
    initialExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (issue.isClosed) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                    ) {
                        Icon(
                            imageVector = if (issue.isClosed) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Status",
                            tint = if (issue.isClosed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(if (issue.isClosed) 18.dp else 24.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "#${issue.serialNumber}",
                        fontSize = AppFontSizes.extraSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (issue.isClosed) 0.5f else 0.8f)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = issue.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (issue.isClosed) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (issue.isClosed) { MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) } else { MaterialTheme.colorScheme.onSurface },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            CategoryBadge(category = issue.category)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val cal = Calendar.getInstance().apply { timeInMillis = issue.timestamp }
                        Text(
                            text = DateTimeUtils.formatShortDate(cal.time),
                            fontSize = AppFontSizes.extraSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        if (issue.isClosed && issue.closedTimestamp != null) {
                            Text(
                                text = "•",
                                fontSize = AppFontSizes.extraSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            val closedCal = Calendar.getInstance().apply { timeInMillis = issue.closedTimestamp }
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Closed ")
                                    }
                                    append(DateTimeUtils.formatShortDate(closedCal.time))
                                },
                                fontSize = AppFontSizes.extraSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (issue.description.isNotBlank()) {
                        Text(
                            text = parseStyledDescription(issue.description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (issue.isClosed) 0.5f else 0.8f),
                            maxLines = if (expanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = !expanded && issue.comments.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Comment,
                                modifier = Modifier.size(20.dp),
                                contentDescription = "Comments",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${issue.comments.size})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {

                    PriorityBadge(priority = issue.priority)
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        modifier = Modifier
                            .rotate(rotation)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )


                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "v: ${issue.appVersion ?: "Unknown"}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                
                var commentsExpanded by remember { mutableStateOf(true) }
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { commentsExpanded = !commentsExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comments",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${issue.comments.size}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (commentsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand/Collapse comments",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (commentsExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 42.dp)
                                .animateContentSize(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                issue.comments.forEachIndexed { index, comment ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = parseStyledDescription(comment.text),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val cCal = Calendar.getInstance().apply { timeInMillis = comment.timestamp }
                                        val iCal = Calendar.getInstance().apply { timeInMillis = issue.timestamp }
                                        val isSameDay = DateTimeUtils.isSameDay(cCal, iCal)
                                        Text(
                                            text = if (isSameDay) DateTimeUtils.formatAlarmTime(context, comment.timestamp) else "${DateTimeUtils.formatShortDate(cCal.time)}, ${DateTimeUtils.formatAlarmTime(context, comment.timestamp)}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    if (index < issue.comments.size - 1) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                }

                                // Add comment input row
                                var isAddingComment by remember { mutableStateOf(false) }
                                var newCommentText by remember { mutableStateOf("") }
                                val commentFocusRequester = remember { FocusRequester() }
                                
                                if (isAddingComment) {
                                    val keyboardController = LocalSoftwareKeyboardController.current
                                    LaunchedEffect(Unit) {
                                        delay(100)
                                        commentFocusRequester.requestFocus()
                                        keyboardController?.show()
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newCommentText,
                                            onValueChange = { newCommentText = it },
                                            placeholder = { Text("Enter comment...", fontSize = 14.sp) },
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .focusRequester(commentFocusRequester),
                                            shape = RoundedCornerShape(8.dp),
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = ImeAction.Done,
                                                capitalization = KeyboardCapitalization.Sentences
                                            ),
                                            keyboardActions = KeyboardActions(onDone = {
                                                if (newCommentText.isNotBlank()) {
                                                    onAddComment(newCommentText.trim())
                                                    newCommentText = ""
                                                    isAddingComment = false
                                                }
                                            })
                                        )
                                        IconButton(
                                            onClick = {
                                                if (newCommentText.isNotBlank()) {
                                                    onAddComment(newCommentText.trim())
                                                    newCommentText = ""
                                                }
                                                isAddingComment = false
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Save",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                newCommentText = ""
                                                isAddingComment = false
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cancel",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isAddingComment = true }
                                            .padding(vertical = 10.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add comment",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Add comment",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onEdit) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}

@Composable
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Issue" -> Color(0xFFE57373)
        "Feature" -> Color(0xFF81C784)
        "Idea" -> Color(0xFF64B5F6)
        else -> MaterialTheme.colorScheme.secondary
    }
}

@Composable
fun CategoryBadge(category: String) {
    val color = getCategoryColor(category)
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, color)
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
            fontSize = AppFontSizes.micro,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun getPriorityColor(priority: Int): Color {
    return when (priority) {
        1 -> Color(0xFFE57373)      // Red
        2 -> Color(0xFFFFB74D)    // Orange
        3 -> Color(0xFF4DB6AC)       // Teal/Mint
        else -> MaterialTheme.colorScheme.secondary
    }
}

// 3. Keep a String overload for the Dialog chips (optional but helpful)
@Composable
fun getPriorityColor(priority: String): Color {
    return getPriorityColor(IssueItem.getPriorityFromLabel(priority))
}

@Composable
fun PriorityBadge(priority: Int) {
    val color = getPriorityColor(priority)
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.5.dp, color)
    ) {
        Text(
            text = priority.toString(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
            fontSize = AppFontSizes.micro,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueAddDialog(
    initialItem: IssueItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String, priority: String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        IssueAddDialogContent(
            initialItem = initialItem,
            onDismiss = onDismiss,
            onSave = onSave
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueAddDialogContent(
    initialItem: IssueItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String, priority: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(initialItem?.title ?: "") }
    var description by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(initialItem?.description ?: "")) }
    var category by rememberSaveable { mutableStateOf(initialItem?.category ?: "Issue") }
    // Convert the Int priority to its String label for the UI state
    var priority by rememberSaveable { mutableStateOf(initialItem?.let { IssueItem.getPriorityLabel(it.priority) } ?: "Normal") }
    
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
                            // Fix: Offset by 2 (for "**") and maintain selection length
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
                            description = TextFieldValue(newText, androidx.compose.ui.text.TextRange(selection.start + formatted.length))
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
                        description = TextFieldValue(newText, androidx.compose.ui.text.TextRange(selection.start + 2))
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
                        onClick = { onSave(title, description.text, category, priority) },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
}

private val previewIssues = listOf(
    IssueItem(
        id = "1342",
        serialNumber = 3,
        title = "Fix crash on login screen",
        description = "NullPointerException when tapping login button rapidly.\n- Reproducible on Android 12\n- Need to check login flow thread locks.",
        category = "Issue",
        isClosed = false,
        timestamp = System.currentTimeMillis() - 86400000,
        comments = listOf(IssueComment("Assigned to dev team"), IssueComment("Adding test logs..."))
    )
)

@Preview(showBackground = true)
@Composable
fun IssueCardCollapsedPreview() {
    SoftTodoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            IssueCard(
                issue = IssueItem(
                    id = "1",
                    serialNumber = 12,
                    title = "Nullpointer on launch",
                    description = "Happens when user is not logged in and starts the app.",
                    category = "Issue",
                    priority = 1,
                    isClosed = false,
                    timestamp = System.currentTimeMillis() - 86400000L,
                    comments = listOf(
                        IssueComment("Confirmed on Google Pixel", System.currentTimeMillis() - 43200000L)
                    )
                ),
                onToggle = {},
                onDelete = {},
                onEdit = {},
                onAddComment = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IssueCardExpandedPreview() {
    SoftTodoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            IssueCard(
                issue = IssueItem(
                    id = "2",
                    serialNumber = 3,
                    title = "Add Google Login",
                    description = "Request from marketing to add Google OAuth options.",
                    category = "Feature",
                    priority = 2,
                    isClosed = false,
                    timestamp = System.currentTimeMillis() - 172800000L,
                    comments = listOf(
                        IssueComment("Requires API Console credentials", System.currentTimeMillis() - 86400000L),
                        IssueComment("Assigned to backend team", System.currentTimeMillis() - 43200000L)
                    )
                ),
                onToggle = {},
                onDelete = {},
                onEdit = {},
                onAddComment = {},
                initialExpanded = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IssueTrackerScreenContentPreview() {
    SoftTodoTheme {
        IssueTrackerScreenContent(
            appName = "Example Tracker App",
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
            onAddIssue = { _, _, _, _ -> },
            onAddComment = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Add/Edit Issue Dialog", backgroundColor = 0X00000)
@Composable
fun IssueAddDialogPreview() {
    SoftTodoTheme(colorSchemeType = "minimal", themeMode = "light") {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            IssueAddDialogContent(
                initialItem = previewIssues[0],
                onDismiss = {},
                onSave = { _, _, _, _ -> }
            )
        }
    }
}