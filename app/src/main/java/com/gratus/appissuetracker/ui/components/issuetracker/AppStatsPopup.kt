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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.data.TrackedApp
import com.gratus.appissuetracker.ui.components.parseStyledDescription
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.theme.dialogContainerColor

private val IssueAccentColor = Color(0xFFE57373)
private val FeatureAccentColor = Color(0xFF81C784)
private val IdeaAccentColor = Color(0xFF64B5F6)

@Composable
fun AppStatsPopup(
    app: TrackedApp,
    issues: List<IssueItem>,
    onUpdateDescription: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppStatsPopupContent(
        app = app,
        issues = issues,
        onUpdateDescription = onUpdateDescription,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

@Composable
fun AppStatsPopupContent(
    app: TrackedApp,
    issues: List<IssueItem>,
    onUpdateDescription: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditingDescription by rememberSaveable { mutableStateOf(false) }
    var editingDescriptionText by rememberSaveable(app.description) { mutableStateOf(app.description) }

    // Synchronize editingDescriptionText if app changes
    LaunchedEffect(app.description) {
        if (!isEditingDescription) {
            editingDescriptionText = app.description
        }
    }

    // Stats calculations for this app
    val totalIssuesCount = remember(issues) { issues.count { it.category.equals("Issue", ignoreCase = true) } }
    val closedIssuesCount = remember(issues) { issues.count { it.category.equals("Issue", ignoreCase = true) && it.isClosed } }

    val totalFeaturesCount = remember(issues) { issues.count { it.category.equals("Feature", ignoreCase = true) } }
    val implementedFeaturesCount = remember(issues) { issues.count { it.category.equals("Feature", ignoreCase = true) && it.isClosed } }

    val totalIdeasCount = remember(issues) { issues.count { it.category.equals("Idea", ignoreCase = true) } }
    val implementedIdeasCount = remember(issues) { issues.count { it.category.equals("Idea", ignoreCase = true) && it.isClosed } }

    val totalItems = issues.size
    val totalResolved = closedIssuesCount + implementedFeaturesCount + implementedIdeasCount
    val completionRate = if (totalItems > 0) ((totalResolved.toFloat() / totalItems) * 100).toInt() else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume click events to prevent background dismissal */ },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.dialogContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: App Name, Subtitle, Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (app.versionName.isNotBlank()) "v${app.versionName}" else "Version not set",
                        fontSize = AppFontSizes.extraSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close popup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Editable Description Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Description & Scope",
                                fontSize = AppFontSizes.small,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (!isEditingDescription) {
                            IconButton(
                                onClick = {
                                    editingDescriptionText = app.description
                                    isEditingDescription = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit description",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    if (isEditingDescription) {
                        OutlinedTextField(
                            value = editingDescriptionText,
                            onValueChange = { editingDescriptionText = it },
                            placeholder = {
                                Text(
                                    "Enter project description or notes...",
                                    fontSize = AppFontSizes.small,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp, max = 160.dp),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    editingDescriptionText = app.description
                                    isEditingDescription = false
                                }
                            ) {
                                Text("Cancel", fontSize = AppFontSizes.extraSmall)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    onUpdateDescription(editingDescriptionText)
                                    isEditingDescription = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Save", fontSize = AppFontSizes.extraSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    editingDescriptionText = app.description
                                    isEditingDescription = true
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            if (app.description.isNotBlank()) {
                                Text(
                                    text = parseStyledDescription(app.description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 19.sp
                                )
                            } else {
                                Text(
                                    text = "Tap to add project description or notes...",
                                    fontSize = AppFontSizes.small,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }

            // Compact Stats Summary
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Resolution summary progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROJECT METRICS",
                        fontSize = AppFontSizes.pico,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (totalItems > 0) "$completionRate% resolved ($totalResolved/$totalItems)" else "0 items",
                        fontSize = AppFontSizes.nano,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                LinearProgressIndicator(
                    progress = { if (totalItems > 0) totalResolved.toFloat() / totalItems else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 3-Column compact stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactCategoryCard(
                        title = "Issues",
                        icon = Icons.Default.BugReport,
                        accentColor = IssueAccentColor,
                        closedCount = closedIssuesCount,
                        totalCount = totalIssuesCount,
                        resolvedLabel = "Closed",
                        modifier = Modifier.weight(1f)
                    )

                    CompactCategoryCard(
                        title = "Features",
                        icon = Icons.Default.AutoAwesome,
                        accentColor = FeatureAccentColor,
                        closedCount = implementedFeaturesCount,
                        totalCount = totalFeaturesCount,
                        resolvedLabel = "Done",
                        modifier = Modifier.weight(1f)
                    )

                    CompactCategoryCard(
                        title = "Ideas",
                        icon = Icons.Default.Lightbulb,
                        accentColor = IdeaAccentColor,
                        closedCount = implementedIdeasCount,
                        totalCount = totalIdeasCount,
                        resolvedLabel = "Done",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactCategoryCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    closedCount: Int,
    totalCount: Int,
    resolvedLabel: String,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) closedCount.toFloat() / totalCount else 0f

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Text(
                text = "$closedCount/$totalCount",
                fontSize = AppFontSizes.medium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = resolvedLabel,
                fontSize = AppFontSizes.pico,
                fontWeight = FontWeight.Medium,
                color = accentColor
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.18f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppStatsPopupPreview() {
    SoftTodoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AppStatsPopupContent(
                app = TrackedApp(
                    id = "1",
                    name = "Example Mobile App",
                    packageName = "com.example.app",
                    versionName = "2.1.0",
                    isCustom = false,
                    description = "Main e-commerce client application for Android.\nContains catalog, checkout, and push notifications."
                ),
                issues = listOf(
                    IssueItem(
                        id = "1",
                        serialNumber = 1,
                        title = "Checkout crash",
                        description = "NPE during stripe payment",
                        category = "Issue",
                        isClosed = true
                    ),
                    IssueItem(
                        id = "2",
                        serialNumber = 2,
                        title = "Dark theme",
                        description = "Add AMOLED theme",
                        category = "Feature",
                        isClosed = true
                    ),
                    IssueItem(
                        id = "3",
                        serialNumber = 3,
                        title = "Biometrics login",
                        description = "Fingerprint check",
                        category = "Idea",
                        isClosed = false
                    )
                ),
                onUpdateDescription = {},
                onDismiss = {}
            )
        }
    }
}
