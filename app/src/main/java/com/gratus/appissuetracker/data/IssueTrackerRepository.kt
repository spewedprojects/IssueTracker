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

package com.gratus.appissuetracker.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

class IssueTrackerRepository(private val context: Context) {
    private val appsFileName = "apps.json"
    
    private val appsFile: File
        get() = File(context.filesDir, appsFileName)

    private fun getIssuesFile(appId: String): File {
        // Sanitize the appId to ensure it is a safe file name (alphanumeric and underscores)
        val safeId = appId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        return File(context.filesDir, "issues_$safeId.json")
    }

    suspend fun getApps(): List<TrackedApp> = withContext(Dispatchers.IO) {
        if (!appsFile.exists()) return@withContext emptyList()
        try {
            val jsonString = appsFile.readText()
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<TrackedApp>()
            for (i in 0 until jsonArray.length()) {
                list.add(TrackedApp.fromJson(jsonArray.getJSONObject(i)))
            }
            // Sort so most recently added apps are first
            list.distinctBy { it.id }.sortedByDescending { it.addedTimestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveApps(apps: List<TrackedApp>) = withContext(Dispatchers.IO) {
        val jsonArray = JSONArray()
        apps.forEach { jsonArray.put(it.toJson()) }
        // Pretty print with 4 spaces indent
        appsFile.writeText(jsonArray.toString(4))
    }

    suspend fun getIssues(appId: String): List<IssueItem> = withContext(Dispatchers.IO) {
        val file = getIssuesFile(appId)
        if (!file.exists()) return@withContext emptyList()
        try {
            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<IssueItem>()
            for (i in 0 until jsonArray.length()) {
                list.add(IssueItem.fromJson(jsonArray.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveIssues(appId: String, issues: List<IssueItem>) = withContext(Dispatchers.IO) {
        val file = getIssuesFile(appId)
        val jsonArray = JSONArray()
        issues.forEach { jsonArray.put(it.toJson()) }
        // Pretty print with 4 spaces indent
        file.writeText(jsonArray.toString(4))
    }

    suspend fun deleteApp(appId: String) = withContext(Dispatchers.IO) {
        val currentApps = getApps()
        val updatedApps = currentApps.filter { it.id != appId }
        saveApps(updatedApps)
        
        val file = getIssuesFile(appId)
        if (file.exists()) {
            file.delete()
        }
    }
}
