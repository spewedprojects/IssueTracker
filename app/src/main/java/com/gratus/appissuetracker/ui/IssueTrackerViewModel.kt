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

enum class IssueFilter { ALL, OPEN, CLOSED }

class IssueTrackerViewModel(application: Application, val app: TrackedApp) : AndroidViewModel(application) {
    private val repository = IssueTrackerRepository(application)

    private val _issues = MutableStateFlow<List<IssueItem>>(emptyList())
    val issues: StateFlow<List<IssueItem>> = _issues.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(IssueFilter.ALL)
    val filter: StateFlow<IssueFilter> = _filter.asStateFlow()

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

    fun addIssue(title: String, description: String, category: String, priorityLabel: String) {
        val currentMaxNumber = _issues.value.maxOfOrNull { it.serialNumber } ?: 0
        
        val liveVersion = if (!app.isCustom && app.packageName != null) {
            try {
                val pm = getApplication<Application>().packageManager
                pm.getPackageInfo(app.packageName, 0).versionName ?: app.versionName
            } catch (e: Exception) {
                app.versionName
            }
        } else {
            app.versionName
        }

        val newItem = IssueItem(
            title = title,
            serialNumber = currentMaxNumber + 1,
            description = description,
            category = category,
            priority = IssueItem.getPriorityFromLabel(priorityLabel), // Convert Label to Int
            appVersion = liveVersion
        )
        val updatedList = listOf(newItem) + _issues.value
        _issues.value = updatedList
        saveToDisk(updatedList)
    }

    fun updateIssue(item: IssueItem) {
        val updatedList = _issues.value.map { if (it.id == item.id) item else it }
        _issues.value = updatedList
        saveToDisk(updatedList)
    }

    fun deleteIssue(item: IssueItem) {
        val updatedList = _issues.value.filter { it.id != item.id }
        _issues.value = updatedList
        saveToDisk(updatedList)
    }

    fun toggleStatus(item: IssueItem) {
        val newClosedTimestamp = if (!item.isClosed) System.currentTimeMillis() else null
        updateIssue(item.copy(isClosed = !item.isClosed, closedTimestamp = newClosedTimestamp))
    }

    fun addComment(item: IssueItem, comment: String) {
        val newComments = item.comments + IssueComment(comment)
        updateIssue(item.copy(comments = newComments))
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: IssueFilter) {
        _filter.value = filter
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
                val jsonList = repository.getIssues(app.id)
                repository.saveIssues(app.id, jsonList) 

                val jsonArray = JSONArray()
                jsonList.forEach { jsonArray.put(it.toJson()) }
                val jsonContent = jsonArray.toString(4)

                val cleanAppName = app.name.replace("[^a-zA-Z0-9]".toRegex(), "_")
                val fullFileName = "issues_${cleanAppName}_export_${System.currentTimeMillis()}.json"
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
