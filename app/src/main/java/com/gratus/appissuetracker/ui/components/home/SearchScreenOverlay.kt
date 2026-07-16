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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.data.TrackedApp
import com.gratus.appissuetracker.ui.components.issuetracker.getPriorityColor
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme

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
                val groupedResults = remember(searchResults) {
                    searchResults.groupBy { it.first }
                        .mapValues { entry -> entry.value.map { it.second } }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groupedResults.keys.toList()) { app ->
                        val issues = groupedResults[app] ?: emptyList()
                        GlobalSearchAppCard(
                            app = app,
                            issues = issues,
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
fun GlobalSearchAppCard(
    app: TrackedApp,
    issues: List<IssueItem>,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Header
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
                
                // Show count of matching issues
                Surface(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${issues.size} ${if (issues.size == 1) "match" else "matches"}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = AppFontSizes.pico,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // List of matching issues inside this card
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                issues.forEachIndexed { index, issue ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = issue.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "#${issue.serialNumber}",
                                fontSize = AppFontSizes.nano,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

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
                            val prioColor = getPriorityColor(issue.priority)
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
        }
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
