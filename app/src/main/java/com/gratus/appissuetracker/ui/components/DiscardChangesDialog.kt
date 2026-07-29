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
 *
 */

package com.gratus.appissuetracker.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.theme.dialogContainerColor

@Composable
fun DiscardChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DiscardChangesDialogContent(
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun DiscardChangesDialogContent(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.dialogContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Discard Changes?", style = MaterialTheme.typography.titleMedium, fontSize = 22.sp, fontStyle = FontStyle.Normal)
            Text("You have unsaved changes. Are you sure you want to discard them?")
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) { Text("Keep Editing") }
                TextButton(onClick = onConfirm) { Text("Discard", color = MaterialTheme.colorScheme.error) }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0x000)
@Composable
fun DiscardChangesDialogPreview() {
    SoftTodoTheme (themeMode = "dark", colorSchemeType = "colorful") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DiscardChangesDialogContent(
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
