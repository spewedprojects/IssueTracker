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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.ui.theme.AppFontSizes

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
