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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gratus.appissuetracker.ui.theme.AppFontSizes

@Composable
fun AestheticsSettingsCard(
    activeTheme: String,
    activeScheme: String,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onThemeChange(mode) }
                                .padding(vertical = 10.dp),
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

                    Column {
                        Box(
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
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                            "minimal"  -> Icons.Default.Spa
                                            "simple"   -> Icons.Default.BrightnessLow
                                            "colorful" -> Icons.Default.Palette
                                            else       -> Icons.Default.SettingsSuggest
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
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

                                // Palette customizer button for colorful scheme
                                if (schemeKey == "colorful") {
                                    IconButton(
                                        onClick = {
                                            onColorSchemeChange(schemeKey)
                                            customizerExpanded = !customizerExpanded
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Customize colorful palette",
                                            tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onColorSchemeChange(schemeKey) },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.secondary)
                                )
                            }
                        }

                        // Customizer Sliders Panel (only for colorful scheme)
                        if (schemeKey == "colorful" && customizerExpanded) {
                            ColorfulCustomizerPanel(
                                colorfulHueShift = colorfulHueShift,
                                colorfulSatScale = colorfulSatScale,
                                onColorfulHueShiftChange = onColorfulHueShiftChange,
                                onColorfulSatScaleChange = onColorfulSatScaleChange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorfulCustomizerPanel(
    colorfulHueShift: Float,
    colorfulSatScale: Float,
    onColorfulHueShiftChange: (Float) -> Unit,
    onColorfulSatScaleChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // live previews
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Preview colors:", fontSize = AppFontSizes.small, color = MaterialTheme.colorScheme.onSurfaceVariant)
            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary).forEach { swatch ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                )
            }
        }

        // Hue shift slider
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Hue Shift", fontSize = AppFontSizes.small, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = if (colorfulHueShift == 0f) "Default" else String.format(java.util.Locale.US, "%+.0f°", colorfulHueShift),
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

        // Saturation scale slider
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Saturation", fontSize = AppFontSizes.small, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = if (colorfulSatScale == 1f) "Default" else String.format(java.util.Locale.US, "%.2f×", colorfulSatScale),
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

        // Reset button
        TextButton(
            onClick = {
                onColorfulHueShiftChange(0f)
                onColorfulSatScaleChange(1f)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset", fontSize = AppFontSizes.small)
        }
    }
}

@Composable
fun BackupSettingsCard(
    onExportBackups: () -> Unit,
    onImportBackups: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                Text(text = "Saves individual JSON logs for all applications inside Documents/IssueTrackerBackups.", fontSize = AppFontSizes.small, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
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

                Text(text = "Load exported JSON files. Imports apps and restores their tracked issues.", fontSize = AppFontSizes.small, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))

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
