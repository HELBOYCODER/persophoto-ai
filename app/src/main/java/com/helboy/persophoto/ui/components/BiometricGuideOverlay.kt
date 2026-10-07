package com.helboy.persophoto.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.helboy.persophoto.data.BiometricStandard
import com.helboy.persophoto.ui.theme.BiometricGuideCyan
import com.helboy.persophoto.ui.theme.StudioCyanPrimary

/**
 * Biometric alignment HUD overlay.
 * Renders head oval, eye line, chin line, and aspect-ratio photo frame.
 */
@Composable
fun BiometricGuideOverlay(
    standard: BiometricStandard,
    modifier: Modifier = Modifier,
    showDetailedLines: Boolean = true
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        val aspect = standard.aspectRatio
        val maxBoxW = canvasW * 0.82f
        val maxBoxH = canvasH * 0.72f

        val boxW: Float
        val boxH: Float
        if (maxBoxW / maxBoxH > aspect) {
            boxH = maxBoxH
            boxW = boxH * aspect
        } else {
            boxW = maxBoxW
            boxH = boxW / aspect
        }

        val boxLeft = (canvasW - boxW) / 2f
        val boxTop = (canvasH - boxH) / 2f
        val boxRight = boxLeft + boxW
        val boxBottom = boxTop + boxH

        // 1. Dim outside area (Vignette)
        val outerPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(canvasW, 0f)
            lineTo(canvasW, canvasH)
            lineTo(0f, canvasH)
            close()
        }
        val innerPath = Path().apply {
            moveTo(boxLeft, boxTop)
            lineTo(boxRight, boxTop)
            lineTo(boxRight, boxBottom)
            lineTo(boxLeft, boxBottom)
            close()
        }
        // Draw dark overlay around the photo box
        drawRect(
            color = Color(0x99000000),
            size = Size(canvasW, canvasH)
        )
        // Clear inner box
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxW, boxH),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // 2. Photo Boundary Frame
        drawRect(
            color = StudioCyanPrimary.copy(alpha = 0.9f),
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxW, boxH),
            style = Stroke(width = 2.5f)
        )

        // 3. Corner Brackets (Cyber aesthetic)
        val bracketLen = minOf(boxW, boxH) * 0.12f
        val bracketStroke = Stroke(width = 5f, cap = StrokeCap.Round)
        val bracketColor = StudioCyanPrimary

        // Top-Left
        drawLine(bracketColor, Offset(boxLeft - 2, boxTop), Offset(boxLeft + bracketLen, boxTop), strokeWidth = 5f)
        drawLine(bracketColor, Offset(boxLeft, boxTop - 2), Offset(boxLeft, boxTop + bracketLen), strokeWidth = 5f)
        // Top-Right
        drawLine(bracketColor, Offset(boxRight + 2, boxTop), Offset(boxRight - bracketLen, boxTop), strokeWidth = 5f)
        drawLine(bracketColor, Offset(boxRight, boxTop - 2), Offset(boxRight, boxTop + bracketLen), strokeWidth = 5f)
        // Bottom-Left
        drawLine(bracketColor, Offset(boxLeft - 2, boxBottom), Offset(boxLeft + bracketLen, boxBottom), strokeWidth = 5f)
        drawLine(bracketColor, Offset(boxLeft, boxBottom + 2), Offset(boxLeft, boxBottom - bracketLen), strokeWidth = 5f)
        // Bottom-Right
        drawLine(bracketColor, Offset(boxRight + 2, boxBottom), Offset(boxRight - bracketLen, boxBottom), strokeWidth = 5f)
        drawLine(bracketColor, Offset(boxRight, boxBottom + 2), Offset(boxRight, boxBottom - bracketLen), strokeWidth = 5f)

        if (showDetailedLines) {
            // 4. Biometric Head Oval
            // Oval height matches ideal ratio (e.g. 72% of photo height)
            val idealRatio = (standard.minFaceHeightRatio + standard.maxFaceHeightRatio) / 2f
            val ovalH = boxH * idealRatio
            val ovalW = ovalH * 0.72f // Average human head aspect ratio
            val ovalCx = boxLeft + boxW / 2f
            val ovalTop = boxTop + boxH * 0.10f // ~10% margin above crown
            val ovalCy = ovalTop + ovalH / 2f

            val dashedStroke = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            drawOval(
                color = BiometricGuideCyan.copy(alpha = 0.85f),
                topLeft = Offset(ovalCx - ovalW / 2f, ovalTop),
                size = Size(ovalW, ovalH),
                style = dashedStroke
            )

            // 5. Eye-Level Horizon Line
            val eyeY = boxBottom - (boxH * standard.idealEyeLevelRatioFromBottom)
            val fineDashedStroke = Stroke(
                width = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            drawLine(
                color = BiometricGuideCyan.copy(alpha = 0.75f),
                start = Offset(boxLeft + boxW * 0.15f, eyeY),
                end = Offset(boxRight - boxW * 0.15f, eyeY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            // 6. Chin Marker Line
            val chinY = ovalTop + ovalH
            drawLine(
                color = BiometricGuideCyan.copy(alpha = 0.5f),
                start = Offset(ovalCx - ovalW * 0.35f, chinY),
                end = Offset(ovalCx + ovalW * 0.35f, chinY),
                strokeWidth = 2f
            )
        }
    }
}
