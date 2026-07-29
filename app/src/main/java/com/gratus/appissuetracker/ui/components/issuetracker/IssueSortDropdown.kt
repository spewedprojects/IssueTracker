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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import com.gratus.appissuetracker.ui.theme.dialogContainerColor

enum class CategoryFilter(val label: String, val categoryName: String?) {
    ALL("All", null),
    ISSUE("Issues", "Issue"),
    FEATURE("Features", "Feature"),
    IDEA("Ideas", "Idea")
}

enum class IssueSort(val label: String) {
    HIGHEST_PRIORITY("Highest Priority"),
    LOWEST_PRIORITY("Lowest Priority"),
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    MODIFIED("Recently Modified")
}

@Composable
fun getCategoryFilterColor(option: CategoryFilter): Color {
    return when (option) {
        CategoryFilter.ALL -> MaterialTheme.colorScheme.primary
        CategoryFilter.ISSUE -> Color(0xFFE57373)
        CategoryFilter.FEATURE -> Color(0xFF81C784)
        CategoryFilter.IDEA -> Color(0xFF64B5F6)
    }
}

@Composable
fun IssueSortDropdownItems(
    selectedCategory: CategoryFilter,
    onCategorySelected: (CategoryFilter) -> Unit,
    selectedSort: IssueSort,
    onSortSelected: (IssueSort) -> Unit,
    categoryCounts: Map<CategoryFilter, Int> = emptyMap()
) {
    // Section Header 1: Category Filter
    Text(
        text = "CATEGORY",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )

    // 2x2 Grid of Category Chips
    val categoryOptions = CategoryFilter.entries
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in categoryOptions.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (j in 0..1) {
                    if (i + j < categoryOptions.size) {
                        val option = categoryOptions[i + j]
                        val isSelected = option == selectedCategory
                        val badgeColor = getCategoryFilterColor(option)
                        val count = categoryCounts[option]
                        val labelWithCount = if (count != null) "${option.label} ($count)" else option.label

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onCategorySelected(option)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = badgeColor.copy(alpha = if (isSelected) 0.25f else 0.08f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) badgeColor else badgeColor.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = labelWithCount,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) badgeColor else badgeColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
    )

    // Section Header 2: Sort By
    Text(
        text = "SORT BY",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )

    IssueSort.entries.forEach { option ->
        val isSelected = option == selectedSort
        DropdownMenuItem(
            text = {
                Text(
                    text = option.label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = AppFontSizes.medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            },
            trailingIcon = {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            onClick = {
                onSortSelected(option)
            },
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun IssueSortDropdownContent(
    modifier: Modifier = Modifier,
    selectedCategory: CategoryFilter = CategoryFilter.ALL,
    onCategorySelected: (CategoryFilter) -> Unit = {},
    selectedSort: IssueSort = IssueSort.NEWEST,
    onSortSelected: (IssueSort) -> Unit = {},
    categoryCounts: Map<CategoryFilter, Int> = emptyMap()
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .border
                (
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.dialogContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp)
        ) {
            IssueSortDropdownItems(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
                selectedSort = selectedSort,
                onSortSelected = onSortSelected,
                categoryCounts = categoryCounts
            )
        }
    }
}

@Composable
fun IssueSortDropdown(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    selectedCategory: CategoryFilter = CategoryFilter.ALL,
    onCategorySelected: (CategoryFilter) -> Unit = {},
    selectedSort: IssueSort,
    onSortSelected: (IssueSort) -> Unit,
    onDismissRequest: () -> Unit,
    categoryCounts: Map<CategoryFilter, Int> = emptyMap()
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.dialogContainerColor
    ) {
        IssueSortDropdownItems(
            selectedCategory = selectedCategory,
            onCategorySelected = {
                onCategorySelected(it)
                //onDismissRequest()
            },
            selectedSort = selectedSort,
            onSortSelected = {
                onSortSelected(it)
                onDismissRequest()
            },
            categoryCounts = categoryCounts
        )
    }
}

@Preview(showBackground = true, name = "Navigable Interactive Dropdown")
@Composable
fun IssueSortDropdownNavigablePreview() {
    var selectedCategory by remember { mutableStateOf(CategoryFilter.ALL) }
    var selectedSort by remember { mutableStateOf(IssueSort.NEWEST) }
    val mockCategoryCounts = remember {
        mapOf(
            CategoryFilter.ALL to 12,
            CategoryFilter.ISSUE to 5,
            CategoryFilter.FEATURE to 4,
            CategoryFilter.IDEA to 3
        )
    }

    SoftTodoTheme (colorSchemeType = "colorful") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            IssueSortDropdownContent(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                selectedSort = selectedSort,
                onSortSelected = { selectedSort = it },
                categoryCounts = mockCategoryCounts
            )
        }
    }
}
