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

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class IssueComment(
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("text", text)
        json.put("timestamp", timestamp)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): IssueComment {
            return IssueComment(
                text = json.getString("text"),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

data class IssueItem(
    val id: String = UUID.randomUUID().toString(),
    val serialNumber: Int,
    val title: String,
    val description: String,
    val category: String,
    val priority: Int = 2, // Added Priority field (1: High, 2: Normal, 3: Low)
    val isClosed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val closedTimestamp: Long? = null,
    val comments: List<IssueComment> = emptyList(),
    val appVersion: String? = null
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("serialNumber", serialNumber)
        json.put("title", title)
        json.put("description", description)
        json.put("category", category)
        json.put("priority", priority) // Serialize priority
        json.put("isClosed", isClosed)
        json.put("timestamp", timestamp)
        json.put("closedTimestamp", closedTimestamp)
        json.put("appVersion", appVersion)
        
        val commentsArray = JSONArray()
        comments.forEach { commentsArray.put(it.toJson()) }
        json.put("comments", commentsArray)
        
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): IssueItem {
            val commentsArray = json.optJSONArray("comments")
            val commentsList = mutableListOf<IssueComment>()
            if (commentsArray != null) {
                for (i in 0 until commentsArray.length()) {
                    val obj = commentsArray.optJSONObject(i)
                    if (obj != null) {
                        commentsList.add(IssueComment.fromJson(obj))
                    } else {
                        val str = commentsArray.optString(i)
                        if (str.isNotEmpty()) {
                            commentsList.add(IssueComment(text = str, timestamp = json.optLong("timestamp", System.currentTimeMillis())))
                        }
                    }
                }
            }

            // Handle legacy data or String values if they exist in saved JSON
            val priorityObj = json.opt("priority")
            val priorityInt = when (priorityObj) {
                is Int -> priorityObj
                is String -> getPriorityFromLabel(priorityObj)
                else -> 2
            }
            
            return IssueItem(
                id = json.getString("id"),
                serialNumber = json.optInt("serialNumber", 0),
                title = json.getString("title"),
                description = json.getString("description"),
                category = json.getString("category"),
                priority = priorityInt, // Use the parsed Int
                isClosed = json.getBoolean("isClosed"),
                timestamp = json.getLong("timestamp"),
                closedTimestamp = if (json.has("closedTimestamp") && !json.isNull("closedTimestamp")) json.getLong("closedTimestamp") else null,
                comments = commentsList,
                appVersion = if (json.has("appVersion") && !json.isNull("appVersion")) json.getString("appVersion") else null
            )
        }
        fun getPriorityLabel(priority: Int): String = when (priority) {
            1 -> "High"
            2 -> "Normal"
            3 -> "Low"
            else -> "Normal"
        }

        fun getPriorityFromLabel(label: String): Int = when (label) {
            "High" -> 1
            "Normal" -> 2
            "Low" -> 3
            else -> 2
        }
    }
}
