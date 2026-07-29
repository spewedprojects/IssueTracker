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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import com.gratus.appissuetracker.ui.theme.dialogContainerColor

//@Composable
//fun DeleteConfirmationDialog(
//    title: String,
//    message: String,
//    onConfirm: () -> Unit,
//    onDismiss: () -> Unit
//) {
//    DeleteConfirmationDialog(
//        title = title,
//        message = AnnotatedString(message),
//        onConfirm = onConfirm,
//        onDismiss = onDismiss
//    )
//}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: AnnotatedString,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DeleteConfirmationDialogContent(
            title = title,
            message = message,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun DeleteConfirmationDialogContent(
    title: String,
    message: AnnotatedString,
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
            Text("$title?", style = MaterialTheme.typography.titleMedium, fontSize = 22.sp, fontStyle = FontStyle.Normal)
            Text(message)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(onClick = onConfirm) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteConfirmationDialogPreview() {
    val annotatedMessage = buildAnnotatedString {
        append("Are you sure you want to delete ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("issue #12 ('Fix crash on launch')")
        }
        append("? This action cannot be undone.")
    }

    SoftTodoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DeleteConfirmationDialogContent(
                title = "Delete Issue",
                message = annotatedMessage,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
