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

package com.gratus.appissuetracker.data

import org.json.JSONObject

data class TrackedApp(
    val id: String, // Unique identifier (e.g. package name, or manually generated ID/slug)
    val name: String,
    val packageName: String?, // null if manual custom project
    val versionName: String, // version name (e.g. "1.0.0" or package version)
    val isCustom: Boolean, // true if manually entered, false if user-installed app
    val addedTimestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("packageName", packageName ?: JSONObject.NULL)
        json.put("versionName", versionName)
        json.put("isCustom", isCustom)
        json.put("addedTimestamp", addedTimestamp)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): TrackedApp {
            return TrackedApp(
                id = json.getString("id"),
                name = json.getString("name"),
                packageName = if (json.isNull("packageName")) null else json.getString("packageName"),
                versionName = json.getString("versionName"),
                isCustom = json.getBoolean("isCustom"),
                addedTimestamp = json.optLong("addedTimestamp", System.currentTimeMillis())
            )
        }
    }
}
