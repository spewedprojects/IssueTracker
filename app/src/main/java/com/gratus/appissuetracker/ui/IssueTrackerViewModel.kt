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
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gratus.appissuetracker.data.IssueItem
import com.gratus.appissuetracker.data.IssueComment
import com.gratus.appissuetracker.data.IssueTrackerRepository
import com.gratus.appissuetracker.data.TrackedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

import com.gratus.appissuetracker.ui.components.issuetracker.CategoryFilter
import com.gratus.appissuetracker.ui.components.issuetracker.IssueSort
import androidx.core.content.edit

enum class IssueFilter { ALL, OPEN, CLOSED }

class IssueTrackerViewModel(application: Application, val app: TrackedApp) : AndroidViewModel(application) {
    private val repository = IssueTrackerRepository(application)
    private val sharedPrefs = application.getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

    private val _issues = MutableStateFlow<List<IssueItem>>(emptyList())
    val issues: StateFlow<List<IssueItem>> = _issues.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(IssueFilter.ALL)
    val filter: StateFlow<IssueFilter> = _filter.asStateFlow()

    private fun getSavedCategoryFilter(): CategoryFilter {
        val savedName = sharedPrefs.getString("issue_category_filter_${app.id}", CategoryFilter.ALL.name)
        return try {
            CategoryFilter.valueOf(savedName ?: CategoryFilter.ALL.name)
        } catch (e: Exception) {
            CategoryFilter.ALL
        }
    }

    private val _categoryFilter = MutableStateFlow(getSavedCategoryFilter())
    val categoryFilter: StateFlow<CategoryFilter> = _categoryFilter.asStateFlow()

    private fun getSavedSortMode(): IssueSort {
        val savedName = sharedPrefs.getString("issue_sort_mode_${app.id}", IssueSort.NEWEST.name)
        return try {
            IssueSort.valueOf(savedName ?: IssueSort.NEWEST.name)
        } catch (e: Exception) {
            IssueSort.NEWEST
        }
    }

    private val _sortMode = MutableStateFlow(getSavedSortMode())
    val sortMode: StateFlow<IssueSort> = _sortMode.asStateFlow()

    init {
        loadIssues()
    }

    private fun loadIssues() {
        viewModelScope.launch {
            val loadedIssues = repository.getIssues(app.id)

            // Check if any existing issues have the default serialNumber 0
            if (loadedIssues.isNotEmpty() && loadedIssues.any { it.serialNumber == 0 }) {
                val migratedIssues = migrateExistingIssues(loadedIssues)
                _issues.value = migratedIssues
                repository.saveIssues(app.id, migratedIssues)
            } else {
                _issues.value = loadedIssues
            }
        }
    }

    fun refresh() {
        loadIssues()
    }

    private fun migrateExistingIssues(oldList: List<IssueItem>): List<IssueItem> {
        // 1. Sort by timestamp so the oldest issue gets #1
        // 2. Map through and assign indices + 1
        return oldList.sortedBy { it.timestamp }.mapIndexed { index, item ->
            if (item.serialNumber == 0) {
                item.copy(serialNumber = index + 1)
            } else {
                item
            }
        }
    }

    private fun getLiveAppVersion(): String {
        return if (!app.isCustom && app.packageName != null) {
            try {
                val pm = getApplication<Application>().packageManager
                pm.getPackageInfo(app.packageName, 0).versionName ?: app.versionName
            } catch (e: Exception) {
                app.versionName
            }
        } else {
            app.versionName
        }
    }

    fun addIssue(title: String, description: String, category: String, priorityLabel: String, customVersionName: String? = null) {
        val currentMaxNumber = _issues.value.maxOfOrNull { it.serialNumber } ?: 0
        
        val liveVersion = if (!customVersionName.isNullOrBlank()) {
            customVersionName.trim()
        } else {
            getLiveAppVersion()
        }

        val now = System.currentTimeMillis()
        val newItem = IssueItem(
            title = title,
            serialNumber = currentMaxNumber + 1,
            description = description,
            category = category,
            priority = IssueItem.getPriorityFromLabel(priorityLabel), // Convert Label to Int
            appVersion = liveVersion,
            timestamp = now,
            lastModified = now
        )
        val updatedList = listOf(newItem) + _issues.value
        _issues.value = updatedList
        saveToDisk(updatedList)

        // If the app's versionName is empty (older import), and a customVersionName is provided,
        // update the app's versionName in the database as well to "seal" it.
        if (app.versionName.isBlank() && !liveVersion.isBlank()) {
            updateAppVersionName(liveVersion)
        }
    }

    fun updateAppVersionName(newVersionName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentApps = repository.getApps().toMutableList()
                val index = currentApps.indexOfFirst { it.id == app.id }
                if (index != -1) {
                    val updatedApp = currentApps[index].copy(versionName = newVersionName)
                    currentApps[index] = updatedApp
                    repository.saveApps(currentApps)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun updateIssue(item: IssueItem) {
        val now = System.currentTimeMillis()
        val updatedItem = item.copy(lastModified = now)
        val updatedList = _issues.value.map { if (it.id == item.id) updatedItem else it }
        _issues.value = updatedList
        saveToDisk(updatedList)
    }

    fun deleteIssue(item: IssueItem) {
        val updatedList = _issues.value.filter { it.id != item.id }
        _issues.value = updatedList
        saveToDisk(updatedList)
    }

    fun toggleStatus(item: IssueItem) {
        val isClosing = !item.isClosed
        val now = System.currentTimeMillis()
        val newClosedTimestamp = if (isClosing) now else null
        val newClosedAppVersion = if (isClosing) getLiveAppVersion() else null
        
        val updatedItem = item.copy(
            isClosed = isClosing,
            closedTimestamp = newClosedTimestamp,
            closedAppVersion = newClosedAppVersion,
            lastModified = now
        )
        val updatedList = _issues.value.map { if (it.id == item.id) updatedItem else it }
        _issues.value = updatedList
        saveToDisk(updatedList)
    }

    fun addComment(item: IssueItem, comment: String) {
        val newComments = item.comments + IssueComment(comment)
        updateIssue(item.copy(comments = newComments))
    }

    fun updateComment(item: IssueItem, commentIndex: Int, newText: String) {
        if (commentIndex < 0 || commentIndex >= item.comments.size) return
        val updatedComments = item.comments.toMutableList()
        updatedComments[commentIndex] = updatedComments[commentIndex].copy(text = newText)
        updateIssue(item.copy(comments = updatedComments))
    }

    fun deleteComment(item: IssueItem, commentIndex: Int) {
        if (commentIndex < 0 || commentIndex >= item.comments.size) return
        val updatedComments = item.comments.toMutableList()
        updatedComments.removeAt(commentIndex)
        updateIssue(item.copy(comments = updatedComments))
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: IssueFilter) {
        _filter.value = filter
    }

    fun setCategoryFilter(filter: CategoryFilter) {
        _categoryFilter.value = filter
        sharedPrefs.edit { putString("issue_category_filter_${app.id}", filter.name) }
    }

    fun setSortMode(sort: IssueSort) {
        _sortMode.value = sort
        sharedPrefs.edit { putString("issue_sort_mode_${app.id}", sort.name) }
    }

    private fun saveToDisk(list: List<IssueItem>) {
        viewModelScope.launch {
            repository.saveIssues(app.id, list)
        }
    }

    fun exportAndShare(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Ensure disk content is loaded and up to date
                val allIssues = repository.getIssues(app.id)
                repository.saveIssues(app.id, allIssues) 

                val currentFilter = _filter.value
                val currentCategory = _categoryFilter.value

                val exportList = allIssues.filter { issue ->
                    val matchesStatus = when (currentFilter) {
                        IssueFilter.ALL -> true
                        IssueFilter.OPEN -> !issue.isClosed
                        IssueFilter.CLOSED -> issue.isClosed
                    }
                    val matchesCategory = when (currentCategory) {
                        CategoryFilter.ALL -> true
                        else -> issue.category.equals(currentCategory.categoryName, ignoreCase = true)
                    }
                    matchesStatus && matchesCategory
                }

                if (exportList.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No issues match active filter to export", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val jsonArray = JSONArray()
                exportList.forEach { jsonArray.put(it.toJson()) }
                val jsonContent = jsonArray.toString(4)

                val cleanAppName = app.name.replace("[^a-zA-Z0-9]".toRegex(), "_")
                val statusPrefix = when (currentFilter) {
                    IssueFilter.ALL -> ""
                    IssueFilter.OPEN -> "open_"
                    IssueFilter.CLOSED -> "closed_"
                }
                val categoryPrefix = when (currentCategory) {
                    CategoryFilter.ALL -> ""
                    else -> "${currentCategory.categoryName!!.lowercase()}_"
                }
                val fullFileName = "${statusPrefix}${categoryPrefix}issues_${cleanAppName}_export_${System.currentTimeMillis()}.json"
                val resolver = context.contentResolver
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

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Exported to Documents/IssueTrackerBackups", Toast.LENGTH_SHORT).show()
                        
                        // Share Intent
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Issues Backup"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
