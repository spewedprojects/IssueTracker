package com.gratus.appissuetracker.ui.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A utility class that provides [RoundedCornerShape]s for items in a grid layout,
 * allowing for specific corner rounding based on the item's position (e.g., larger
 * radii for the outer corners of the grid).
 *
 * @property columns The number of columns in the grid.
 * @property bigRadius The radius to apply to the outer corners of the grid.
 * @property smallRadius The radius to apply to all other corners.
 */
@Immutable
class GridCornerShapeProvider(
    private val columns: Int,
    private val bigRadius: Dp = 10.dp,
    private val smallRadius: Dp = 5.dp
) {
    /**
     * Calculates the appropriate [RoundedCornerShape] for an item at the given [index].
     *
     * @param index The zero-based index of the item in the grid.
     * @param totalCount The total number of items in the grid.
     * @return A [RoundedCornerShape] with radii configured based on the item's position.
     */
    fun shapeFor(index: Int, totalCount: Int): RoundedCornerShape {
        if (totalCount <= 0 || columns <= 0) return RoundedCornerShape(smallRadius)

        val row = index / columns
        val col = index % columns
        val lastRow = (totalCount - 1) / columns
        
        // Correctly determine if this item is at the end of its row (even if it's the last row)
        val isLastInRow = if (row == lastRow) {
            val itemsInLastRow = if (totalCount % columns == 0) columns else totalCount % columns
            col == itemsInLastRow - 1
        } else {
            col == columns - 1
        }

        val isFirstRow = row == 0
        val isLastRow = row == lastRow
        val isFirstCol = col == 0
        val isLastCol = isLastInRow

        val topStart = if (isFirstRow && isFirstCol) bigRadius else smallRadius
        val topEnd = if (isFirstRow && isLastCol) bigRadius else smallRadius
        val bottomStart = if (isLastRow && isFirstCol) bigRadius else smallRadius
        val bottomEnd = if (isLastRow && isLastCol) bigRadius else smallRadius

        return RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomEnd = bottomEnd,
            bottomStart = bottomStart
        )
    }
}
