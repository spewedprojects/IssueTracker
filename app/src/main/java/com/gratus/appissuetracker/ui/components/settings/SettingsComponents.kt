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

package com.gratus.appissuetracker.ui.components.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import androidx.compose.ui.tooling.preview.Preview
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.R
import com.gratus.appissuetracker.ui.utils.GridCornerShapeProvider

@Composable
fun AestheticsSettingsCard(
    activeTheme: String,
    activeScheme: String,
    colorSchemeType: String,
    colorfulHueShift: Float,
    colorfulSatScale: Float,
    onThemeChange: (String) -> Unit,
    onColorSchemeChange: (String) -> Unit,
    onColorfulHueShiftChange: (Float) -> Unit,
    onColorfulSatScaleChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = when (colorSchemeType) {
            "simple" -> BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            "system" -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            else -> null
        }
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Aesthetics Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Theme selector (Light, Dark, Auto)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeList = listOf(
                        Pair("auto", "System Auto"),
                        Pair("light", "Light"),
                        Pair("dark", "Dark")
                    )

                    themeList.forEach { (mode, label) ->
                        val isSelected = activeTheme == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onThemeChange(mode) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = AppFontSizes.small,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Color Palette Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Color Schemes Palette",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                val schemes = listOf(
                    Triple("minimal", "Clean Minimalism", "Lavender backing with space-blurry spheres, sleek borders, and elegant state indicators."),
                    Triple("simple", "Simple B&W Only", "Black and white base, accents colored strictly around Priority levels."),
                    Triple("colorful", "Pastel Colorful", "Soft pastel layers with faint radial sweeping neon screen background."),
                    Triple("system", "System Monet", "Dynamic native Material You colors synched directly from Android 12+ wallpaper settings.")
                )

                schemes.forEach { (schemeKey, name, desc) ->
                    val isSelected = activeScheme == schemeKey
                    var customizerExpanded by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onColorSchemeChange(schemeKey) }
                            .animateContentSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = when (schemeKey) {
                                        "minimal" -> Icons.Default.Spa
                                        "simple" -> Icons.Default.FilterBAndW
                                        "colorful" -> Icons.Default.Palette
                                        else -> Icons.Default.SettingsSuggest
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp).offset(x = 0.dp, y = 3.dp)
                                )
                                Column {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = AppFontSizes.large,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = AppFontSizes.small,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        lineHeight = AppFontSizes.medium
                                    )
                                }
                            }

                            if (schemeKey == "colorful") {
                                IconButton(
                                    onClick = {
                                        onColorSchemeChange(schemeKey)
                                        customizerExpanded = !customizerExpanded
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    val discoverTuneIcon = ImageVector.vectorResource(R.drawable.discover_tune_18dp)
                                    Icon(
                                        imageVector = discoverTuneIcon,
                                        contentDescription = "Customize colorful palette",
                                        tint = if (isSelected) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)//.rotate(90f) // remove rotation later if you like it otherwise
                                    )
                                }
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { onColorSchemeChange(schemeKey) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.secondary)
                            )
                        }

                        if (schemeKey == "colorful" && isSelected && customizerExpanded) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val previewPrimary = MaterialTheme.colorScheme.primary
                                val previewSecondary = MaterialTheme.colorScheme.secondary
                                val previewTertiary = MaterialTheme.colorScheme.tertiary
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Preview:",
                                        fontSize = AppFontSizes.small,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    listOf(previewPrimary, previewSecondary, previewTertiary).forEach { swatch ->
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(swatch)
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Hue Shift",
                                            fontSize = AppFontSizes.small,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (colorfulHueShift == 0f) "Default"
                                            else "%+.0f°".format(colorfulHueShift),
                                            fontSize = AppFontSizes.small,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Slider(
                                        value = colorfulHueShift,
                                        onValueChange = onColorfulHueShiftChange,
                                        valueRange = -80f..60f,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.secondary,
                                            activeTrackColor = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Saturation",
                                            fontSize = AppFontSizes.small,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (colorfulSatScale == 1f) "Default"
                                            else "%.2f×".format(colorfulSatScale),
                                            fontSize = AppFontSizes.small,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Slider(
                                        value = colorfulSatScale,
                                        onValueChange = onColorfulSatScaleChange,
                                        valueRange = 0.7f..1.3f,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.secondary,
                                            activeTrackColor = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        onColorfulHueShiftChange(0f)
                                        onColorfulSatScaleChange(1f)
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reset to Default",
                                        fontSize = AppFontSizes.small
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackupSettingsCard(
    onExportBackups: () -> Unit,
    onImportBackups: () -> Unit,
    colorSchemeType: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = when (colorSchemeType) {
            "simple" -> BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            "system" -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            else -> null
        }
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Backup & Data Logs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Actions Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Saves individual JSON logs for all applications inside Documents/IssueTrackerBackups.", fontSize = AppFontSizes.small, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = AppFontSizes.extraLarge
                )
                // Export to Device Button
                Button(
                    onClick = { onExportBackups() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_device_btn")
                ) {
                    Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export to Device")
                }

                Text(
                    text = "Load exported JSON files. Imports apps and restores their tracked issues.", fontSize = AppFontSizes.small, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = AppFontSizes.extraLarge
                )

                // Import & Restore Backup Button
                OutlinedButton(
                    onClick = { onImportBackups() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_file_btn")
                ) {
                    Icon(imageVector = Icons.Default.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import & Restore Backup")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AestheticsSettingsCardMinimalPreview() {
    SoftTodoTheme(colorSchemeType = "system", themeMode = "light") {
        Box(modifier = Modifier.padding(16.dp)) {
            AestheticsSettingsCard(
                activeTheme = "light",
                activeScheme = "system",
                colorSchemeType = "system",
                colorfulHueShift = 0f,
                colorfulSatScale = 1f,
                onThemeChange = {},
                onColorSchemeChange = {},
                onColorfulHueShiftChange = {},
                onColorfulSatScaleChange = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0XFF000000)
@Composable
fun AestheticsSettingsCardColorfulPreview() {
    SoftTodoTheme(colorSchemeType = "colorful", themeMode = "dark") {
        Box(modifier = Modifier.padding(16.dp)) {
            AestheticsSettingsCard(
                activeTheme = "dark",
                activeScheme = "colorful",
                colorSchemeType = "colorful",
                colorfulHueShift = 15f,
                colorfulSatScale = 1.2f,
                onThemeChange = {},
                onColorSchemeChange = {},
                onColorfulHueShiftChange = {},
                onColorfulSatScaleChange = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BackupSettingsCardPreview() {
    SoftTodoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BackupSettingsCard(
                onExportBackups = {},
                onImportBackups = {},
                colorSchemeType = "minmal"
            )
        }
    }
}

@Composable
fun SortingSettingsCard(
    activeSortMode: String,
    onSortModeChange: (String) -> Unit,
    colorSchemeType: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = when (colorSchemeType) {
            "simple" -> BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            "system" -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            else -> null
        }
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sorting Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "App List Sorting Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val sortModes = listOf(
                    Pair("added_date", "Date Added"),
                    Pair("alphabetical", "Alphabetical"),
                    Pair("modified", "Recently Modified"),
                    Pair("highest_issues", "Highest Issues"),
                    Pair("lowest_issues", "Lowest Issues"),
                    Pair("highest_open_issues", "Highest Open"),
                    Pair("lowest_open_issues", "Lowest Open")
                )

                val shapeProvider = remember { GridCornerShapeProvider(columns = 2) }

                for (i in sortModes.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (j in 0..1) {
                            if (i + j < sortModes.size) {
                                val (mode, label) = sortModes[i + j]
                                val isSelected = activeSortMode == mode
                                val shape = shapeProvider.shapeFor(i + j, sortModes.size)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(shape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                            shape
                                        )
                                        .clickable { onSortModeChange(mode) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = AppFontSizes.small,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface
                                    )
                                }
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
fun SortingSettingsCardPreview() {
    SoftTodoTheme (colorSchemeType = "simple", themeMode = "light"){
        Box(modifier = Modifier.padding(16.dp)) {
            SortingSettingsCard(
                activeSortMode = "alphabetical",
                onSortModeChange = {},
                colorSchemeType = "simple"
            )
        }
    }
}
