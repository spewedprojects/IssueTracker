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

data class InstalledAppInfo(
    val name: String,
    val packageName: String,
    val versionName: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IssueTrackerRepository(application)
    private val sharedPrefs = application.getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

    // Settings States
    private val _settingsTheme = MutableStateFlow(sharedPrefs.getString("theme", "auto") ?: "auto")
    val settingsTheme: StateFlow<String> = _settingsTheme.asStateFlow()

    private val _settingsColorScheme = MutableStateFlow(sharedPrefs.getString("color_scheme", "minimal") ?: "minimal")
    val settingsColorScheme: StateFlow<String> = _settingsColorScheme.asStateFlow()

    private val _settingsSortMode = MutableStateFlow(sharedPrefs.getString("sort_mode", "added_date") ?: "added_date")
    val settingsSortMode: StateFlow<String> = _settingsSortMode.asStateFlow()

    private val _colorfulHueShift = MutableStateFlow(sharedPrefs.getFloat("colorful_hue_shift", 0f))
    val colorfulHueShift: StateFlow<Float> = _colorfulHueShift.asStateFlow()

    private val _colorfulSatScale = MutableStateFlow(sharedPrefs.getFloat("colorful_sat_scale", 1f))
    val colorfulSatScale: StateFlow<Float> = _colorfulSatScale.asStateFlow()

    // Open Issues Count for each app
    private val _openIssuesCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val openIssuesCounts: StateFlow<Map<String, Int>> = _openIssuesCounts.asStateFlow()

    // Total Issues Count for each app
    private val _totalIssuesCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val totalIssuesCounts: StateFlow<Map<String, Int>> = _totalIssuesCounts.asStateFlow()

    // Tracked Apps List
    private val _apps = MutableStateFlow<List<TrackedApp>>(emptyList())
    // Combine raw apps flow with sorting settings and issue counts to sort dynamically
    val apps: StateFlow<List<TrackedApp>> = combine(
        _apps,
        _settingsSortMode,
        _openIssuesCounts,
        _totalIssuesCounts
    ) { appsList, sortMode, openCounts, totalCounts ->
        when (sortMode) {
            "highest_issues" -> appsList.sortedByDescending { totalCounts[it.id] ?: 0 }
            "lowest_issues" -> appsList.sortedBy { totalCounts[it.id] ?: 0 }
            "highest_open_issues" -> appsList.sortedByDescending { openCounts[it.id] ?: 0 }
            "lowest_open_issues" -> appsList.sortedBy { openCounts[it.id] ?: 0 }
            "alphabetical" -> appsList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            "added_date" -> appsList.sortedByDescending { it.addedTimestamp }
            else -> appsList.sortedByDescending { it.addedTimestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search history States
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // User Installed Apps (cached for add-app dialog)
    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    // Global Search State
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _globalSearchResults = MutableStateFlow<List<Pair<TrackedApp, IssueItem>>>(emptyList())
    val globalSearchResults: StateFlow<List<Pair<TrackedApp, IssueItem>>> = _globalSearchResults.asStateFlow()

    // Pending JSON imports queue
    data class PendingImportTask(
        val uri: Uri,
        val fileName: String,
        val issues: List<IssueItem>
    )

    private val _pendingImports = MutableStateFlow<List<PendingImportTask>>(emptyList())
    val pendingImports: StateFlow<List<PendingImportTask>> = _pendingImports.asStateFlow()

    init {
        loadApps()
        loadSearchHistory()
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

    fun setSortMode(mode: String) {
        _settingsSortMode.value = mode
        sharedPrefs.edit().putString("sort_mode", mode).apply()
    }

    private fun loadSearchHistory() {
        val historyJson = sharedPrefs.getString("search_history", "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(historyJson)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            _searchHistory.value = list
        } catch (e: Exception) {
            _searchHistory.value = emptyList()
        }
    }

    fun saveSearchToHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        
        val current = _searchHistory.value.toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)
        val updated = current.take(5)
        _searchHistory.value = updated
        
        try {
            val jsonArray = JSONArray()
            updated.forEach { jsonArray.put(it) }
            sharedPrefs.edit().putString("search_history", jsonArray.toString()).apply()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun deleteSearchFromHistory(query: String) {
        val updated = _searchHistory.value.filter { it != query }
        _searchHistory.value = updated
        
        try {
            val jsonArray = JSONArray()
            updated.forEach { jsonArray.put(it) }
            sharedPrefs.edit().putString("search_history", jsonArray.toString()).apply()
        } catch (e: Exception) {
            // Ignore
        }
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
            val trimmedName = name.trim()
            val exists = if (packageName != null) {
                _apps.value.any { it.packageName == packageName }
            } else {
                _apps.value.any { it.isCustom && it.name.trim().equals(trimmedName, ignoreCase = true) }
            }
            if (exists) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "App \"$trimmedName\" is already tracked!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val id = packageName ?: UUID.randomUUID().toString()
            val newApp = TrackedApp(
                id = id,
                name = trimmedName,
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
                                IssueItem.getPriorityLabel(it.priority).contains(query, ignoreCase = true)
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
                val tasks = mutableListOf<PendingImportTask>()
                for (uri in uris) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val jsonStr = String(inputStream.readBytes(), Charsets.UTF_8)
                        
                        var appName: String? = null
                        var packageName: String? = null
                        var versionName: String? = null
                        val issues = mutableListOf<IssueItem>()
                        
                        try {
                            val jsonObject = org.json.JSONObject(jsonStr)
                            appName = if (jsonObject.has("appName")) jsonObject.getString("appName") else null
                            packageName = if (jsonObject.has("packageName")) jsonObject.getString("packageName") else null
                            versionName = if (jsonObject.has("versionName")) jsonObject.getString("versionName") else null
                            val array = jsonObject.optJSONArray("issues")
                            if (array != null) {
                                for (i in 0 until array.length()) {
                                    issues.add(IssueItem.fromJson(array.getJSONObject(i)))
                                }
                            }
                        } catch (e: Exception) {
                            // Try as flat array
                            val jsonArray = JSONArray(jsonStr)
                            for (i in 0 until jsonArray.length()) {
                                issues.add(IssueItem.fromJson(jsonArray.getJSONObject(i)))
                            }
                        }

                        if (issues.isNotEmpty()) {
                            val fileName = getFileName(context, uri) ?: "imported_backup.json"
                            tasks.add(PendingImportTask(uri, fileName, issues))
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    if (tasks.isNotEmpty()) {
                        _pendingImports.value = _pendingImports.value + tasks
                        onComplete(true)
                    } else {
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

    fun skipPendingImport(task: PendingImportTask) {
        _pendingImports.value = _pendingImports.value.filter { it.uri != task.uri }
    }

    fun executeImport(
        task: PendingImportTask,
        targetOption: Int, // 0 = New Custom, 1 = Existing Tracked, 2 = Installed App
        customName: String,
        customVersion: String,
        selectedTrackedApp: TrackedApp?,
        selectedInstalledApp: InstalledAppInfo?,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentApps = repository.getApps().toMutableList()
                var targetAppId = ""
                
                when (targetOption) {
                    0 -> {
                        // Create New Custom App
                        targetAppId = UUID.randomUUID().toString()
                        val newApp = TrackedApp(
                            id = targetAppId,
                            name = customName.trim(),
                            packageName = null,
                            versionName = customVersion.trim(),
                            isCustom = true,
                            addedTimestamp = System.currentTimeMillis()
                        )
                        currentApps.add(0, newApp)
                        repository.saveApps(currentApps)
                        
                        // Save imported issues directly
                        repository.saveIssues(targetAppId, task.issues)
                    }
                    1 -> {
                        // Merge with existing tracked app
                        if (selectedTrackedApp == null) return@launch
                        targetAppId = selectedTrackedApp.id
                        val existingIssues = repository.getIssues(targetAppId)
                        val mergedIssues = mergeIssues(existingIssues, task.issues)
                        repository.saveIssues(targetAppId, mergedIssues)
                    }
                    2 -> {
                        // Merge with installed app
                        if (selectedInstalledApp == null) return@launch
                        
                        // Check if this installed app is already tracked
                        val existingTracked = currentApps.find { it.id == selectedInstalledApp.packageName }
                        if (existingTracked != null) {
                            targetAppId = existingTracked.id
                            val existingIssues = repository.getIssues(targetAppId)
                            val mergedIssues = mergeIssues(existingIssues, task.issues)
                            repository.saveIssues(targetAppId, mergedIssues)
                        } else {
                            targetAppId = selectedInstalledApp.packageName
                            val newApp = TrackedApp(
                                id = targetAppId,
                                name = selectedInstalledApp.name,
                                packageName = selectedInstalledApp.packageName,
                                versionName = selectedInstalledApp.versionName,
                                isCustom = false,
                                addedTimestamp = System.currentTimeMillis()
                            )
                            currentApps.add(0, newApp)
                            repository.saveApps(currentApps)
                            repository.saveIssues(targetAppId, task.issues)
                        }
                    }
                }
                
                // Remove task from queue and refresh lists
                withContext(Dispatchers.Main) {
                    _pendingImports.value = _pendingImports.value.filter { it.uri != task.uri }
                    _apps.value = currentApps
                    loadApps() // Reload to refresh counts
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    private fun mergeIssues(existing: List<IssueItem>, imported: List<IssueItem>): List<IssueItem> {
        val mergedList = existing.toMutableList()
        
        // Find issues in imported that do not exist by ID
        val newImported = imported.filter { imp -> existing.none { ext -> ext.id == imp.id } }
                                  .sortedBy { it.timestamp }
        
        var nextSerialNumber = (existing.map { it.serialNumber }.maxOrNull() ?: 0) + 1
        
        for (issue in newImported) {
            val serialConflict = mergedList.any { it.serialNumber == issue.serialNumber }
            val resolvedIssue = if (serialConflict) {
                val updated = issue.copy(serialNumber = nextSerialNumber)
                nextSerialNumber++
                updated
            } else {
                if (issue.serialNumber >= nextSerialNumber) {
                    nextSerialNumber = issue.serialNumber + 1
                }
                issue
            }
            mergedList.add(resolvedIssue)
        }
        
        return mergedList
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
