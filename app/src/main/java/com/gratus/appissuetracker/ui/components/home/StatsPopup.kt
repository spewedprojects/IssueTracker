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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.appissuetracker.ui.GlobalStats
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.theme.dialogContainerColor

private val IssueAccentColor = Color(0xFFE57373)
private val FeatureAccentColor = Color(0xFF81C784)
private val IdeaAccentColor = Color(0xFF64B5F6)

@Composable
fun StatsPopup(
    stats: GlobalStats,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    StatsPopupContent(
        stats = stats,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

@Composable
fun StatsPopupContent(
    stats: GlobalStats,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row with Title and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${stats.totalApps} tracked ${if (stats.totalApps == 1) "app" else "apps"} • ${stats.totalAll} total ${if (stats.totalAll == 1) "item" else "items"}",
                            fontSize = AppFontSizes.extraSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close statistics",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Overall Summary Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
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
                        Text(
                            text = "Overall Resolution",
                            fontSize = AppFontSizes.small,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (stats.totalAll > 0) "${stats.completionRate}% resolved" else "0%",
                            fontSize = AppFontSizes.small,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { if (stats.totalAll > 0) stats.closedAll.toFloat() / stats.totalAll else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resolved: ${stats.closedAll}",
                            fontSize = AppFontSizes.micro,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Active: ${stats.openAll}",
                            fontSize = AppFontSizes.micro,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // 1. Issues Tile
            CategoryStatTile(
                title = "Issues",
                icon = Icons.Default.BugReport,
                accentColor = IssueAccentColor,
                createdCount = stats.issuesCreated,
                resolvedCount = stats.issuesClosed,
                resolvedLabel = "Closed",
                openCount = stats.issuesOpen
            )

            // 2. Features Tile
            CategoryStatTile(
                title = "Features",
                icon = Icons.Default.AutoAwesome,
                accentColor = FeatureAccentColor,
                createdCount = stats.featuresCreated,
                resolvedCount = stats.featuresImplemented,
                resolvedLabel = "Implemented",
                openCount = stats.featuresOpen
            )

            // 3. Ideas Tile
            CategoryStatTile(
                title = "Ideas",
                icon = Icons.Default.Lightbulb,
                accentColor = IdeaAccentColor,
                createdCount = stats.ideasCreated,
                resolvedCount = stats.ideasImplemented,
                resolvedLabel = "Implemented",
                openCount = stats.ideasOpen
            )

            if (stats.totalAll == 0) {
                Text(
                    text = "No issues or items tracked yet. Add an application and track its issues, features, and ideas!",
                    fontSize = AppFontSizes.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryStatTile(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    createdCount: Int,
    resolvedCount: Int,
    resolvedLabel: String,
    openCount: Int
) {
    val progress = if (createdCount > 0) resolvedCount.toFloat() / createdCount else 0f
    val percent = (progress * 100).toInt()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category header & percentage pill
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = AppFontSizes.medium,
                        color = accentColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (createdCount > 0) "$percent% $resolvedLabel" else "0% $resolvedLabel",
                        fontSize = AppFontSizes.pico,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            // Stat columns: Created, Resolved, Open
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatColumn(
                    label = "Created",
                    count = createdCount,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
                StatColumn(
                    label = resolvedLabel,
                    count = resolvedCount,
                    textColor = accentColor
                )
                StatColumn(
                    label = "Open",
                    count = openCount,
                    textColor = if (openCount > 0) MaterialTheme.colorScheme.onSurfaceVariant else accentColor
                )
            }

            // Progress bar
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

@Composable
private fun StatColumn(
    label: String,
    count: Int,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = count.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = AppFontSizes.large,
            color = textColor
        )
        Text(
            text = label,
            fontSize = AppFontSizes.nano,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatsPopupPreview() {
    SoftTodoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            StatsPopupContent(
                stats = GlobalStats(
                    totalApps = 3,
                    totalAll = 28,
                    closedAll = 17,
                    openAll = 11,
                    issuesCreated = 14,
                    issuesClosed = 10,
                    issuesOpen = 4,
                    featuresCreated = 8,
                    featuresImplemented = 5,
                    featuresOpen = 3,
                    ideasCreated = 6,
                    ideasImplemented = 2,
                    ideasOpen = 4
                ),
                onDismiss = {}
            )
        }
    }
}
