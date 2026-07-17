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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gratus.appissuetracker.ui.MainViewModel
import com.gratus.appissuetracker.ui.components.home.fadingEdges
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.components.settings.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activeTheme by viewModel.settingsTheme.collectAsState()
    val activeScheme by viewModel.settingsColorScheme.collectAsState()
    val colorfulHueShift by viewModel.colorfulHueShift.collectAsState()
    val colorfulSatScale by viewModel.colorfulSatScale.collectAsState()
    val activeSortMode by viewModel.settingsSortMode.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris != null && uris.isNotEmpty()) {
            viewModel.importAllIssues(context, uris) { success ->
                if (success) {
                    Toast.makeText(context, "Backups imported successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Import failed: Invalid issue file format", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    SettingsScreenContent(
        activeTheme = activeTheme,
        activeScheme = activeScheme,
        colorfulHueShift = colorfulHueShift,
        colorfulSatScale = colorfulSatScale,
        activeSortMode = activeSortMode,
        onThemeChange = { viewModel.setTheme(it) },
        onColorSchemeChange = { viewModel.setColorScheme(it) },
        onColorfulHueShiftChange = { viewModel.setColorfulHueShift(it) },
        onColorfulSatScaleChange = { viewModel.setColorfulSatScale(it) },
        onSortModeChange = { viewModel.setSortMode(it) },
        onExportBackups = { viewModel.exportAllIssues(context) },
        onImportBackups = {
            try {
                importLauncher.launch(arrayOf("application/json", "*/*"))
            } catch (e: Exception) {
                importLauncher.launch(arrayOf("*/*"))
            }
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    activeTheme: String,
    activeScheme: String,
    colorfulHueShift: Float,
    colorfulSatScale: Float,
    activeSortMode: String,
    onThemeChange: (String) -> Unit,
    onColorSchemeChange: (String) -> Unit,
    onColorfulHueShiftChange: (Float) -> Unit,
    onColorfulSatScaleChange: (Float) -> Unit,
    onSortModeChange: (String) -> Unit,
    onExportBackups: () -> Unit,
    onImportBackups: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SortingSettingsCard(
                activeSortMode = activeSortMode,
                onSortModeChange = onSortModeChange
            )

            AestheticsSettingsCard(
                activeTheme = activeTheme,
                activeScheme = activeScheme,
                colorfulHueShift = colorfulHueShift,
                colorfulSatScale = colorfulSatScale,
                onThemeChange = onThemeChange,
                onColorSchemeChange = onColorSchemeChange,
                onColorfulHueShiftChange = onColorfulHueShiftChange,
                onColorfulSatScaleChange = onColorfulSatScaleChange
            )

            BackupSettingsCard(
                onExportBackups = onExportBackups,
                onImportBackups = onImportBackups
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
fun SettingsScreenMinimalPreview() {
    SoftTodoTheme(themeMode = "light", colorSchemeType = "minimal") {
        SettingsScreenContent(
            activeTheme = "light",
            activeScheme = "minimal",
            colorfulHueShift = 0f,
            colorfulSatScale = 1f,
            activeSortMode = "added_date",
            onThemeChange = {},
            onColorSchemeChange = {},
            onColorfulHueShiftChange = {},
            onColorfulSatScaleChange = {},
            onSortModeChange = {},
            onExportBackups = {},
            onImportBackups = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenColorfulDarkPreview() {
    SoftTodoTheme(themeMode = "dark", colorSchemeType = "colorful") {
        SettingsScreenContent(
            activeTheme = "dark",
            activeScheme = "colorful",
            colorfulHueShift = 10f,
            colorfulSatScale = 1.1f,
            activeSortMode = "added_date",
            onThemeChange = {},
            onColorSchemeChange = {},
            onColorfulHueShiftChange = {},
            onColorfulSatScaleChange = {},
            onSortModeChange = {},
            onExportBackups = {},
            onImportBackups = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
fun SettingsScreenSimplePreview() {
    SoftTodoTheme(themeMode = "auto", colorSchemeType = "simple") {
        SettingsScreenContent(
            activeTheme = "auto",
            activeScheme = "simple",
            colorfulHueShift = 0f,
            colorfulSatScale = 1f,
            activeSortMode = "added_date",
            onThemeChange = {},
            onColorSchemeChange = {},
            onColorfulHueShiftChange = {},
            onColorfulSatScaleChange = {},
            onSortModeChange = {},
            onExportBackups = {},
            onImportBackups = {},
            onBack = {}
        )
    }
}
