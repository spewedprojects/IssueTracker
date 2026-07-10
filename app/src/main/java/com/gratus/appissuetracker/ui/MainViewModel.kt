/*
 * MustDO
 * Copyright (C) 2026 spewedprojects <rkharat98@live.com>
 *
 * This file is part of MustDo Application.
 *
 * MustDo is free software: you can redistribute it and/or modify
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

package com.gratus.appissuetracker.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.data.IssueTrackerRepository
import com.gratus.appissuetracker.data.TrackedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.UUID

sealed interface Screen {
    object Home : Screen
    object Settings : Screen
    data class IssueTracker(val app: TrackedApp) : Screen
}

data class InstalledAppInfo(
    val name: String,
    val packageName: String,
    val versionName: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IssueTrackerRepository(application)
    private val sharedPrefs = application.getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

    // Navigation State
    private val _activeScreen = MutableStateFlow<Screen>(Screen.Home)
    val activeScreen: StateFlow<Screen> = _activeScreen.asStateFlow()

    // Settings States
    private val _settingsTheme = MutableStateFlow(sharedPrefs.getString("theme", "auto") ?: "auto")
    val settingsTheme: StateFlow<String> = _settingsTheme.asStateFlow()

    private val _settingsColorScheme = MutableStateFlow(sharedPrefs.getString("color_scheme", "minimal") ?: "minimal")
    val settingsColorScheme: StateFlow<String> = _settingsColorScheme.asStateFlow()

    private val _colorfulHueShift = MutableStateFlow(sharedPrefs.getFloat("colorful_hue_shift", 0f))
    val colorfulHueShift: StateFlow<Float> = _colorfulHueShift.asStateFlow()

    private val _colorfulSatScale = MutableStateFlow(sharedPrefs.getFloat("colorful_sat_scale", 1f))
    val colorfulSatScale: StateFlow<Float> = _colorfulSatScale.asStateFlow()

    // Tracked Apps List
    private val _apps = MutableStateFlow<List<TrackedApp>>(emptyList())
    val apps: StateFlow<List<TrackedApp>> = _apps.asStateFlow()

    // Open Issues Count for each app
    private val _openIssuesCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val openIssuesCounts: StateFlow<Map<String, Int>> = _openIssuesCounts.asStateFlow()

    // Total Issues Count for each app
    private val _totalIssuesCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val totalIssuesCounts: StateFlow<Map<String, Int>> = _totalIssuesCounts.asStateFlow()

    // User Installed Apps (cached for add-app dialog)
    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    // Global Search State
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _globalSearchResults = MutableStateFlow<List<Pair<TrackedApp, IssueItem>>>(emptyList())
    val globalSearchResults: StateFlow<List<Pair<TrackedApp, IssueItem>>> = _globalSearchResults.asStateFlow()

    init {
        loadApps()
    }

    fun setActiveScreen(screen: Screen) {
        _activeScreen.value = screen
    }

    fun setTheme(theme: String) {
        _settingsTheme.value = theme
        sharedPrefs.edit().putString("theme", theme).apply()
    }

    fun setColorScheme(scheme: String) {
        _settingsColorScheme.value = scheme
        sharedPrefs.edit().putString("color_scheme", scheme).apply()
    }

    fun setColorfulHueShift(value: Float) {
        _colorfulHueShift.value = value
        sharedPrefs.edit().putFloat("colorful_hue_shift", value).apply()
    }

    fun setColorfulSatScale(value: Float) {
        _colorfulSatScale.value = value
        sharedPrefs.edit().putFloat("colorful_sat_scale", value).apply()
    }

    fun loadApps() {
        viewModelScope.launch {
            val loadedApps = repository.getApps()
            _apps.value = loadedApps
            
            // Calculate open and total issues counts in background
            withContext(Dispatchers.IO) {
                val openCounts = mutableMapOf<String, Int>()
                val totalCounts = mutableMapOf<String, Int>()
                loadedApps.forEach { app ->
                    val issues = repository.getIssues(app.id)
                    openCounts[app.id] = issues.count { !it.isClosed }
                    totalCounts[app.id] = issues.size
                }
                _openIssuesCounts.value = openCounts
                _totalIssuesCounts.value = totalCounts
            }
        }
    }

    fun addApp(name: String, packageName: String?, versionName: String, isCustom: Boolean) {
        viewModelScope.launch {
            val id = packageName ?: UUID.randomUUID().toString()
            val newApp = TrackedApp(
                id = id,
                name = name,
                packageName = packageName,
                versionName = versionName,
                isCustom = isCustom,
                addedTimestamp = System.currentTimeMillis()
            )
            val updatedApps = listOf(newApp) + _apps.value
            _apps.value = updatedApps
            repository.saveApps(updatedApps)
            loadApps() // Reload to trigger recalculating issue counts
        }
    }

    fun deleteApp(app: TrackedApp) {
        viewModelScope.launch {
            repository.deleteApp(app.id)
            loadApps() // Reload to trigger recalculating issue counts
        }
    }

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val appList = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
                    .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 } // User apps only
                    .map { appInfo ->
                        val name = appInfo.loadLabel(pm).toString()
                        val packageName = appInfo.packageName
                        val versionName = try {
                            pm.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
                        } catch (e: Exception) {
                            "1.0.0"
                        }
                        InstalledAppInfo(name, packageName, versionName)
                    }
                    .sortedBy { it.name }
                _installedApps.value = appList
            } catch (e: Exception) {
                _installedApps.value = emptyList()
            }
        }
    }

    fun setGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
        if (query.isBlank()) {
            _globalSearchResults.value = emptyList()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val currentApps = _apps.value
                val results = mutableListOf<Pair<TrackedApp, IssueItem>>()
                for (app in currentApps) {
                    val issuesList = repository.getIssues(app.id)
                    val filtered = issuesList.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.serialNumber.toString() == query ||
                        "#${it.serialNumber}".contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.priority.contains(query, ignoreCase = true)
                    }
                    filtered.forEach { results.add(Pair(app, it)) }
                }
                _globalSearchResults.value = results.sortedByDescending { it.second.timestamp }
            }
        }
    }

    // Export issues of all apps in separate files
    fun exportAllIssues(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentApps = repository.getApps()
                if (currentApps.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No apps/issues to export", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                var successCount = 0
                val resolver = context.contentResolver

                for (app in currentApps) {
                    val issuesList = repository.getIssues(app.id)
                    if (issuesList.isEmpty()) continue

                    val jsonArray = JSONArray()
                    issuesList.forEach { jsonArray.put(it.toJson()) }
                    val jsonContent = jsonArray.toString(4)

                    val timestamp = System.currentTimeMillis()
                    val cleanAppName = app.name.replace("[^a-zA-Z0-9]".toRegex(), "_")
                    val fullFileName = "issues_${cleanAppName}_export_${timestamp}.json"

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/IssueTrackerBackups")
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                    }

                    val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(jsonContent.toByteArray())
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            resolver.update(uri, contentValues, null, null)
                        }
                        successCount++
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exported $successCount JSON backups to Documents/IssueTrackerBackups", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Import issues from multiselected files
    fun importAllIssues(context: Context, uris: List<Uri>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var successCount = 0
                val currentApps = repository.getApps().toMutableList()

                for (uri in uris) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val jsonStr = String(inputStream.readBytes(), Charsets.UTF_8)
                        val jsonArray = JSONArray(jsonStr)
                        val issues = mutableListOf<IssueItem>()
                        for (i in 0 until jsonArray.length()) {
                            issues.add(IssueItem.fromJson(jsonArray.getJSONObject(i)))
                        }

                        if (issues.isNotEmpty()) {
                            // Extract app name from filename
                            val fileName = getFileName(context, uri) ?: "Imported_App_${System.currentTimeMillis()}"
                            var appName = "Imported App"
                            if (fileName.startsWith("issues_")) {
                                val temp = fileName.removePrefix("issues_")
                                val exportIndex = temp.indexOf("_export_")
                                if (exportIndex != -1) {
                                    appName = temp.substring(0, exportIndex).replace("_", " ")
                                } else {
                                    appName = temp.removeSuffix(".json").replace("_", " ")
                                }
                            } else {
                                appName = fileName.removeSuffix(".json").replace("_", " ")
                            }

                            // Create app entry
                            val appId = UUID.randomUUID().toString()
                            val newApp = TrackedApp(
                                id = appId,
                                name = appName,
                                packageName = null,
                                versionName = "1.0.0",
                                isCustom = true,
                                addedTimestamp = System.currentTimeMillis()
                            )

                            currentApps.add(0, newApp)
                            repository.saveIssues(appId, issues)
                            successCount++
                        }
                    }
                }

                if (successCount > 0) {
                    repository.saveApps(currentApps)
                    _apps.value = currentApps
                    withContext(Dispatchers.Main) {
                        onComplete(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
