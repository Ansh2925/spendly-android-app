package com.example.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object HandDrawnIcons {
    val Food: ImageVector
        get() = ImageVector.Builder(
            name = "Food",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Burger top bun (sketchy)
                moveTo(4f, 13f)
                quadTo(12f, 12f, 20f, 13f)
                quadTo(22f, 7f, 12f, 6f)
                quadTo(2f, 7f, 4f, 13f)
                close()
                // Meat
                moveTo(3f, 15f)
                quadTo(12f, 16f, 21f, 15f)
                lineTo(21f, 17f)
                quadTo(12f, 18f, 3f, 17f)
                close()
                // Bottom bun
                moveTo(4f, 19f)
                quadTo(12f, 20f, 20f, 19f)
                quadTo(19f, 22f, 12f, 22f)
                quadTo(5f, 22f, 4f, 19f)
                close()
            }
        }.build()

    val Travel: ImageVector
        get() = ImageVector.Builder(
            name = "Travel",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 16f)
                lineTo(5f, 9f)
                lineTo(15f, 9f)
                lineTo(19f, 13f)
                lineTo(21f, 16f)
                lineTo(3f, 16f)
                close()
                
                // wheels
                moveTo(6f, 16f)
                arcTo(2f, 2f, 0f, false, false, 10f, 16f)
                moveTo(14f, 16f)
                arcTo(2f, 2f, 0f, false, false, 18f, 16f)
            }
        }.build()

    val Coffee: ImageVector
        get() = ImageVector.Builder(
            name = "Coffee",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 7f)
                quadTo(12f, 6f, 19f, 7f)
                quadTo(20f, 14f, 18f, 18f)
                quadTo(12f, 21f, 6f, 18f)
                quadTo(4f, 14f, 5f, 7f)
                close()
                
                // Handle
                moveTo(19f, 9f)
                quadTo(23f, 9f, 23f, 12f)
                quadTo(23f, 15f, 18f, 15f)
                
                // Steam
                moveTo(10f, 3f)
                quadTo(11f, 1f, 10f, 0f)
                moveTo(14f, 4f)
                quadTo(15f, 2f, 14f, 1f)
            }
        }.build()

    val Shopping: ImageVector
        get() = ImageVector.Builder(
            name = "Shopping",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 8f)
                lineTo(20f, 8f)
                lineTo(18f, 21f)
                lineTo(6f, 21f)
                lineTo(4f, 8f)
                close()
                
                // Handle
                moveTo(8f, 8f)
                quadTo(8f, 3f, 12f, 3f)
                quadTo(16f, 3f, 16f, 8f)
            }
        }.build()

    val Generic: ImageVector
        get() = ImageVector.Builder(
            name = "Generic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Sketchy star
                moveTo(12f, 2f)
                lineTo(15f, 9f)
                lineTo(22f, 9f)
                lineTo(16f, 14f)
                lineTo(18f, 21f)
                lineTo(12f, 17f)
                lineTo(6f, 21f)
                lineTo(8f, 14f)
                lineTo(2f, 9f)
                lineTo(9f, 9f)
                close()
            }
        }.build()
}
