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

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.fadingEdges(
    statusBarHeightPx: Float,
    density: Density,
    topPadding: Dp,
    bottomPadding: Dp,
    footerHeight: Dp = 56.dp
): Modifier = this
    .graphicsLayer(alpha = 0.99f)
    .drawWithContent {
        drawContent()
        
        val H = size.height
        if (H <= 0f) return@drawWithContent
        
        val topPaddingPx = with(density) { topPadding.toPx() }
        
        val bottomPaddingPx = with(density) { bottomPadding.toPx() }
        val footerHeightPx = with(density) { footerHeight.toPx() }
        
        // Define top fade range:
        // y1 (transparent) = topPaddingPx - 16.dp
        // y2 (opaque) = topPaddingPx + 32.dp
        val y1 = (topPaddingPx - with(density) { 54.dp.toPx() }).coerceAtLeast(0f)
        val y2 = topPaddingPx + with(density) { 32.dp.toPx() }
        
        // Define bottom fade range:
        // y3 (opaque) = H - bottomPaddingPx - footerHeightPx - 24.dp
        // y4 (transparent) = H - bottomPaddingPx
        val y4 = H - bottomPaddingPx
        val y3 = H - bottomPaddingPx - footerHeightPx - with(density) { 54.dp.toPx() }
        
        // Safeguard to prevent crossover
        val mid = H / 2f
        val clampedY2 = y2.coerceAtMost(mid)
        val clampedY3 = y3.coerceAtLeast(mid)
        
        // Build the gradient brush stops
        val stops = arrayOf(
            0.0f to Color.Transparent,
            (y1 / H).coerceIn(0f, 1f) to Color.Transparent,
            (clampedY2 / H).coerceIn(0f, 1f) to Color.Black,
            (clampedY3 / H).coerceIn(0f, 1f) to Color.Black,
            (y4 / H).coerceIn(0f, 1f) to Color.Transparent,
            1.0f to Color.Transparent
        )
        
        drawRect(
            brush = Brush.verticalGradient(colorStops = stops),
            blendMode = BlendMode.DstIn
        )
    }
