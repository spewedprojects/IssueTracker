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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.appissuetracker.data.IssueComment
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.ui.components.parseStyledDescription
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.utils.DateTimeUtils
import kotlinx.coroutines.delay
import java.util.Calendar

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
