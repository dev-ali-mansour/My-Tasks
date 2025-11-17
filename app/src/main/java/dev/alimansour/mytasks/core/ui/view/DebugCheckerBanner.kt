package dev.alimansour.mytasks.core.ui.view

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.alimansour.mytasks.core.ui.theme.BurgundyPurple

private const val LTR_ROTATION_DEGREES = -45f
private const val RTL_ROTATION_DEGREES = 45f
private const val PIVOT_OFFSET_FACTOR = 0.3f
private const val BANNER_RECT_HEIGHT_MULTIPLIER = 1.6f
private const val BANNER_RECT_WIDTH_MULTIPLIER = 1.5f
private const val BANNER_CANVAS_SIZE_DP_VALUE = 70
private const val BANNER_FONT_SIZE_SP_VALUE = 10

@Composable
fun DebugCheckerBanner(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) return

    val bannerText = "DEBUG"
    val bannerCanvasSize = BANNER_CANVAS_SIZE_DP_VALUE.dp
    val bannerFontSize = BANNER_FONT_SIZE_SP_VALUE.sp
    val bannerTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Canvas(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .width(bannerCanvasSize)
                    .height(bannerCanvasSize)
                    .zIndex(Float.MAX_VALUE),
        ) {
            val textPaint =
                Paint().apply {
                    isAntiAlias = true
                    color = Color.White.toArgb()
                    textSize = bannerFontSize.toPx()
                    textAlign = Paint.Align.CENTER
                    typeface = bannerTypeface
                }

            val textRotationDegrees =
                if (layoutDirection == LayoutDirection.Ltr) {
                    LTR_ROTATION_DEGREES
                } else {
                    RTL_ROTATION_DEGREES
                }

            val rotationPivotX =
                if (layoutDirection == LayoutDirection.Ltr) {
                    size.width * PIVOT_OFFSET_FACTOR
                } else {
                    size.width * (1f - PIVOT_OFFSET_FACTOR)
                }
            val rotationPivotY = size.height * PIVOT_OFFSET_FACTOR

            val bannerRectHeight = bannerFontSize.toPx() * BANNER_RECT_HEIGHT_MULTIPLIER
            val bannerRectWidth = size.width * BANNER_RECT_WIDTH_MULTIPLIER

            val bannerBgPaint =
                Paint().apply {
                    isAntiAlias = true
                    color = BurgundyPurple.toArgb()
                    style = Paint.Style.FILL
                }

            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(textRotationDegrees, rotationPivotX, rotationPivotY)

            val rectLeft = rotationPivotX - bannerRectWidth / 2f
            val rectTop = rotationPivotY - bannerRectHeight / 2f
            val rectRight = rotationPivotX + bannerRectWidth / 2f
            val rectBottom = rotationPivotY + bannerRectHeight / 2f

            drawContext.canvas.nativeCanvas.drawRect(
                rectLeft,
                rectTop,
                rectRight,
                rectBottom,
                bannerBgPaint,
            )

            drawContext.canvas.nativeCanvas.drawText(
                bannerText,
                rotationPivotX,
                rotationPivotY - (textPaint.descent() + textPaint.ascent()) / 2f,
                textPaint,
            )
            drawContext.canvas.nativeCanvas.restore()
        }
    }
}
