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

package com.gratus.appissuetracker.ui.components.home

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.appissuetracker.data.TrackedApp
import com.gratus.appissuetracker.ui.theme.AppFontSizes
import com.gratus.appissuetracker.ui.theme.SoftTodoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridCard(
    app: TrackedApp,
    totalIssues: Int,
    openIssues: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .aspectRatio(0.95f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon Container
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (app.isCustom) {
                    GridLetterAvatar(name = app.name, modifier = Modifier.fillMaxSize())
                } else {
                    GridAppLauncherIcon(packageName = app.id, modifier = Modifier.fillMaxSize())
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Text(
                text = app.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Stats line
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$totalIssues issues",
                    fontSize = AppFontSizes.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = " • ",
                    fontSize = AppFontSizes.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "$openIssues open",
                    fontSize = AppFontSizes.micro,
                    fontWeight = FontWeight.Bold,
                    color = if (openIssues > 0) Color(0xFFE57373) else Color(0xFF4DB6AC)
                )
            }
        }
    }
}

@Composable
fun AddApplicationGridCard(onClick: () -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.95f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
            .clickable { onClick() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = outlineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                ),
                cornerRadius = CornerRadius(20.dp.toPx())
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Add Application",
                fontWeight = FontWeight.Bold,
                fontSize = AppFontSizes.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GridLetterAvatar(name: String, modifier: Modifier = Modifier) {
    val char = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFB74D), Color(0xFF4DB6AC), Color(0xFFBA68C8)
    )
    val bgColor = remember(name) {
        colors[name.hashCode().let { if (it < 0) -it else it } % colors.size]
    }
    Box(
        modifier = modifier
            .background(bgColor.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
            .border(1.dp, bgColor.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            fontWeight = FontWeight.Bold,
            fontSize = AppFontSizes.headline,
            color = bgColor
        )
    }
}

@Composable
fun LetterAvatar(name: String, modifier: Modifier = Modifier) {
    val char = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFB74D), Color(0xFF4DB6AC), Color(0xFFBA68C8)
    )
    val bgColor = remember(name) {
        colors[name.hashCode().let { if (it < 0) -it else it } % colors.size]
    }
    Box(
        modifier = modifier
            .background(bgColor.copy(alpha = 0.2f), shape = CircleShape)
            .border(1.5.dp, bgColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            fontWeight = FontWeight.Bold,
            fontSize = AppFontSizes.extraLarge,
            color = bgColor
        )
    }
}

object AppIconCache {
    private val cache = java.util.concurrent.ConcurrentHashMap<String, androidx.compose.ui.graphics.ImageBitmap>()

    fun get(packageName: String): androidx.compose.ui.graphics.ImageBitmap? {
        return cache[packageName]
    }

    fun put(packageName: String, bitmap: androidx.compose.ui.graphics.ImageBitmap) {
        cache[packageName] = bitmap
    }
}

@Composable
fun GridAppLauncherIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var imageBitmap by remember(packageName) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(AppIconCache.get(packageName)) }

    if (imageBitmap == null) {
        LaunchedEffect(packageName) {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val pm = context.packageManager
                    val iconDrawable = pm.getApplicationIcon(packageName)
                    val width = iconDrawable.intrinsicWidth.coerceAtLeast(1).coerceAtMost(400)
                    val height = iconDrawable.intrinsicHeight.coerceAtLeast(1).coerceAtMost(400)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = AndroidCanvas(bitmap)
                    iconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    iconDrawable.draw(canvas)
                    bitmap.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                AppIconCache.put(packageName, bitmap)
                imageBitmap = bitmap
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = null,
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                .padding(12.dp)
        )
    } else {
        GridLetterAvatar(name = packageName, modifier = modifier)
    }
}

@Composable
fun AppLauncherIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var imageBitmap by remember(packageName) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(AppIconCache.get(packageName)) }

    if (imageBitmap == null) {
        LaunchedEffect(packageName) {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val pm = context.packageManager
                    val iconDrawable = pm.getApplicationIcon(packageName)
                    val width = iconDrawable.intrinsicWidth.coerceAtLeast(1).coerceAtMost(256)
                    val height = iconDrawable.intrinsicHeight.coerceAtLeast(1).coerceAtMost(256)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = AndroidCanvas(bitmap)
                    iconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    iconDrawable.draw(canvas)
                    bitmap.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                AppIconCache.put(packageName, bitmap)
                imageBitmap = bitmap
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = null,
            modifier = modifier.clip(CircleShape)
        )
    } else {
        LetterAvatar(name = packageName, modifier = modifier)
    }
}

@Composable
fun EmptyFolderIllustration(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primaryContainer
    val outlineColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw folder background
            val path = Path().apply {
                moveTo(10.dp.toPx(), 20.dp.toPx())
                lineTo(45.dp.toPx(), 20.dp.toPx())
                lineTo(55.dp.toPx(), 32.dp.toPx())
                lineTo(110.dp.toPx(), 32.dp.toPx())
                lineTo(110.dp.toPx(), 100.dp.toPx())
                lineTo(10.dp.toPx(), 100.dp.toPx())
                close()
            }
            drawPath(path, color = primaryColor.copy(alpha = 0.3f))
            drawPath(path, color = outlineColor.copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))

            // Draw a sheet of paper peeking out
            val paperPath = Path().apply {
                moveTo(25.dp.toPx(), 10.dp.toPx())
                lineTo(95.dp.toPx(), 10.dp.toPx())
                lineTo(95.dp.toPx(), 60.dp.toPx())
                lineTo(25.dp.toPx(), 60.dp.toPx())
                close()
            }
            drawPath(paperPath, color = Color.White)
            drawPath(paperPath, color = outlineColor.copy(alpha = 0.4f), style = Stroke(width = 1.5.dp.toPx()))
            
            // Eyes
            drawCircle(color = outlineColor, radius = 2.5.dp.toPx(), center = Offset(45.dp.toPx(), 65.dp.toPx()))
            drawCircle(color = outlineColor, radius = 2.5.dp.toPx(), center = Offset(75.dp.toPx(), 65.dp.toPx()))
            // Sad mouth (arc)
            drawArc(
                color = outlineColor,
                startAngle = 00f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(52.dp.toPx(), 72.dp.toPx()),
                size = Size(16.dp.toPx(), 10.dp.toPx()),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppGridCardPreview() {
    SoftTodoTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AppGridCard(
                    app = TrackedApp("1", "RocketApp", "com.example.rocket", "1.2.3", isCustom = false),
                    totalIssues = 23,
                    openIssues = 5,
                    onClick = {},
                    onLongClick = {}
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AppGridCard(
                    app = TrackedApp("2", "ChatFlow", null, "0.1.0", isCustom = true),
                    totalIssues = 18,
                    openIssues = 0,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }
    }
}
