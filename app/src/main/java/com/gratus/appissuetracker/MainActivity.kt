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

package com.gratus.appissuetracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.gratus.appissuetracker.ui.MainViewModel
import com.gratus.appissuetracker.ui.components.FaintBackground
import com.gratus.appissuetracker.ui.components.ImportConflictDialog
import com.gratus.appissuetracker.ui.screens.HomeScreen
import com.gratus.appissuetracker.ui.screens.IssueTrackerScreen
import com.gratus.appissuetracker.ui.screens.SettingsScreen
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import kotlinx.serialization.Serializable

@Serializable object Home
@Serializable object Settings
@Serializable data class IssueTracker(val appId: String)

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val settingsTheme by viewModel.settingsTheme.collectAsState()
            val colorSchemeType by viewModel.settingsColorScheme.collectAsState()
            val colorfulHueShift by viewModel.colorfulHueShift.collectAsState()
            val colorfulSatScale by viewModel.colorfulSatScale.collectAsState()

            val isDark = when (settingsTheme) {
                "light" -> false
                "dark"  -> true
                else    -> isSystemInDarkTheme()
            }

            SoftTodoTheme(
                themeMode = settingsTheme,
                colorSchemeType = colorSchemeType,
                colorfulHueShift = colorfulHueShift,
                colorfulSatScale = colorfulSatScale
            ) {
                val pendingImports by viewModel.pendingImports.collectAsState()
                val apps by viewModel.apps.collectAsState()
                val installedApps by viewModel.installedApps.collectAsState()

                FaintBackground(
                    colorSchemeType = colorSchemeType,
                    isDark = isDark,
                    colorfulHueShift = colorfulHueShift,
                    colorfulSatScale = colorfulSatScale
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Home,
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) },
                        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
                        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) }
                    ) {
                        composable<Home> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToTracker = { app ->
                                    navController.navigate(IssueTracker(app.id))
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Settings)
                                }
                            )
                        }
                        composable<Settings> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable<IssueTracker> { backStackEntry ->
                            val route: IssueTracker = backStackEntry.toRoute()
                            val app = apps.find { it.id == route.appId }

                            if (app != null) {
                                // Re-load list of apps when leaving tracker (in case open issues count changed)
                                DisposableEffect(Unit) {
                                    onDispose {
                                        viewModel.loadApps()
                                    }
                                }
                                IssueTrackerScreen(
                                    app = app,
                                    colorSchemeType = colorSchemeType,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }

                // Overlay dialog for JSON import conflict customization
                if (pendingImports.isNotEmpty()) {
                    val context = LocalContext.current
                    LaunchedEffect(pendingImports) {
                        if (installedApps.isEmpty()) {
                            viewModel.loadInstalledApps(context)
                        }
                    }
                    val currentTask = pendingImports.first()
                    ImportConflictDialog(
                        task = currentTask,
                        trackedApps = apps,
                        installedApps = installedApps,
                        onDismiss = {
                            viewModel.skipPendingImport(currentTask)
                        },
                        onImport = { targetOption, customName, customVersion, trackedApp, installedApp ->
                            viewModel.executeImport(
                                task = currentTask,
                                targetOption = targetOption,
                                customName = customName,
                                customVersion = customVersion,
                                selectedTrackedApp = trackedApp,
                                selectedInstalledApp = installedApp
                            ) { success ->
                                if (success) {
                                    Toast.makeText(this@MainActivity, "Imported successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@MainActivity, "Import failed.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
