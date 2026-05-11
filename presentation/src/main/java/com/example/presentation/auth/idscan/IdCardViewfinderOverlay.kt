package com.example.presentation.auth.idscan

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.presentation.constants.IdCardAspectWidthOverHeight
import com.example.presentation.constants.MaxFrameHeightOfAvailable
import com.example.presentation.constants.MaxFrameWidthOfAvailable
import com.example.presentation.constants.OutsideFrameAlpha
import com.example.presentation.constants.ViewfinderCornerDp
import com.example.presentation.constants.ViewfinderOuterMarginDp
import com.example.presentation.constants.ViewfinderStrokeWidthDp


@Composable
internal fun IdCardViewfinderOverlay(
    frameStrokeColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cornerPx = ViewfinderCornerDp.dp.toPx()
        val cornerRadius = CornerRadius(cornerPx, cornerPx)
        val margin = ViewfinderOuterMarginDp.dp.toPx()
        val maxW = size.width - margin * 2f
        val maxH = size.height - margin * 2f

        var frameWidth = maxW * MaxFrameWidthOfAvailable
        var frameHeight = frameWidth / IdCardAspectWidthOverHeight
        if (frameHeight > maxH * MaxFrameHeightOfAvailable) {
            frameHeight = maxH * MaxFrameHeightOfAvailable
            frameWidth = frameHeight * IdCardAspectWidthOverHeight
        }

        val left = (size.width - frameWidth) * 0.5f
        val top = (size.height - frameHeight) * 0.5f
        val roundRect = RoundRect(
            left = left,
            top = top,
            right = left + frameWidth,
            bottom = top + frameHeight,
            cornerRadius = cornerRadius,
        )

        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, this@Canvas.size))
            addRoundRect(roundRect)
        }

        drawPath(path, color = Color.Black.copy(alpha = OutsideFrameAlpha))

        drawRoundRect(
            color = frameStrokeColor,
            topLeft = Offset(left, top),
            size = Size(frameWidth, frameHeight),
            cornerRadius = cornerRadius,
            style = Stroke(width = ViewfinderStrokeWidthDp.dp.toPx()),
        )
    }
}
