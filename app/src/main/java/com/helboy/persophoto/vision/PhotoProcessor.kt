package com.helboy.persophoto.vision

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import com.helboy.persophoto.data.BackgroundColorOption
import com.helboy.persophoto.data.BiometricStandard
import com.helboy.persophoto.data.PhotoProcessingOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * Result of the full processing pipeline.
 */
data class ProcessedPhotoResult(
    val finalBitmap: Bitmap,
    val faceDetected: Boolean,
    val faceBoundingBox: Rect?,
    val actualFaceHeightRatio: Float,
    val isBiometricCompliant: Boolean,
    val complianceFeedbackFa: List<String>
)

/**
 * Main coordinator pipeline for PersoPhoto AI.
 */
class PhotoProcessor(
    private val faceDetector: BiometricFaceDetector = BiometricFaceDetector(),
    private val backgroundSegmenter: BackgroundSegmenter = BackgroundSegmenter(),
    private val lightingOptimizer: LightingOptimizer = LightingOptimizer()
) {

    /**
     * Executes end-to-end processing:
     * Face detection -> Biometric alignment & crop -> Background whitening -> Lighting & color optimization -> 300 DPI rendering.
     */
    suspend fun processPhoto(
        sourceBitmap: Bitmap,
        options: PhotoProcessingOptions
    ): ProcessedPhotoResult = withContext(Dispatchers.Default) {
        val standard = options.standard

        // 1. Detect Face
        val face = faceDetector.detectFace(sourceBitmap)
        val faceDetected = face != null

        // 2. Calculate optimal biometric crop
        val cropRect = if (face != null) {
            faceDetector.calculateBiometricCropRect(
                imageWidth = sourceBitmap.width,
                imageHeight = sourceBitmap.height,
                face = face,
                standard = standard,
                zoomMultiplier = options.zoomScale,
                panOffsetX = options.panOffsetX,
                panOffsetY = options.panOffsetY
            )
        } else {
            // Center crop with standard aspect ratio if no face detected
            calculateFallbackCenterCrop(sourceBitmap.width, sourceBitmap.height, standard.aspectRatio)
        }

        // 3. Crop source bitmap with user rotation
        val croppedBitmap = cropAndRotate(
            source = sourceBitmap,
            cropRect = cropRect,
            rotationDegrees = options.userRotationDegrees - (face?.headRollAngleDegrees ?: 0f) * 0.5f
        )

        // 4. Background Whitening / Replacement
        val segmentedBitmap = if (options.backgroundOption != BackgroundColorOption.ORIGINAL) {
            backgroundSegmenter.replaceBackground(
                bitmap = croppedBitmap,
                targetColor = options.backgroundOption.colorInt
            )
        } else {
            croppedBitmap
        }

        // 5. Lighting, Auto-exposure, Color-cast neutralization & Sharpening
        val faceInCropped = if (face != null) {
            // Translate face box relative to crop
            Rect(
                max(0, face.boundingBox.left - cropRect.left),
                max(0, face.boundingBox.top - cropRect.top),
                face.boundingBox.right - cropRect.left,
                face.boundingBox.bottom - cropRect.top
            )
        } else null

        val colorOptimizedBitmap = lightingOptimizer.optimize(
            source = segmentedBitmap,
            faceBox = faceInCropped,
            options = options
        )

        // 6. Scale to Target 300 DPI Lab Print Resolution
        val targetW = standard.targetWidthPx300Dpi
        val targetH = standard.targetHeightPx300Dpi
        val finalBitmap = Bitmap.createScaledBitmap(colorOptimizedBitmap, targetW, targetH, true)

        // 7. Verify Biometric Compliance
        val faceHeightRatio = if (face != null && cropRect.height() > 0) {
            face.boundingBox.height().toFloat() / cropRect.height().toFloat()
        } else {
            0.72f
        }

        val isRatioOk = faceHeightRatio in standard.minFaceHeightRatio..standard.maxFaceHeightRatio
        val feedbackList = mutableListOf<String>()

        if (faceDetected) {
            if (isRatioOk) {
                feedbackList.add("اندازه و تناسب چهره کاملاً استاندارد است (${(faceHeightRatio * 100).toInt()}٪)")
            } else if (faceHeightRatio < standard.minFaceHeightRatio) {
                feedbackList.add("چهره کمی دور است (پیشنهاد: کمی زوم کنید)")
            } else {
                feedbackList.add("چهره کمی نزدیک است (پیشنهاد: زوم را کاهش دهید)")
            }

            if (options.backgroundOption == BackgroundColorOption.PURE_WHITE) {
                feedbackList.add("پس‌زمینه سفید خالص است ✓")
            }
            if (options.isAutoEnhanced) {
                feedbackList.add("نور و کنتراست بهینه‌سازی شد ✓")
            }
        } else {
            feedbackList.add("چهره‌ای شناسایی نشد؛ کادربندی از مرکز تصویر انجام شد")
        }

        ProcessedPhotoResult(
            finalBitmap = finalBitmap,
            faceDetected = faceDetected,
            faceBoundingBox = faceInCropped,
            actualFaceHeightRatio = faceHeightRatio,
            isBiometricCompliant = faceDetected && isRatioOk,
            complianceFeedbackFa = feedbackList
        )
    }

    private fun calculateFallbackCenterCrop(srcW: Int, srcH: Int, targetAspect: Float): Rect {
        val srcAspect = srcW.toFloat() / srcH.toFloat()
        return if (srcAspect > targetAspect) {
            // Source is wider, crop sides
            val cropW = (srcH * targetAspect).toInt()
            val left = (srcW - cropW) / 2
            Rect(left, 0, left + cropW, srcH)
        } else {
            // Source is taller, crop top/bottom
            val cropH = (srcW / targetAspect).toInt()
            val top = (srcH - cropH) / 3 // Slightly bias towards upper body
            Rect(0, top, srcW, top + cropH)
        }
    }

    private fun cropAndRotate(
        source: Bitmap,
        cropRect: Rect,
        rotationDegrees: Float
    ): Bitmap {
        val clampedRect = Rect(
            cropRect.left.coerceIn(0, source.width - 1),
            cropRect.top.coerceIn(0, source.height - 1),
            cropRect.right.coerceIn(1, source.width),
            cropRect.bottom.coerceIn(1, source.height)
        )
        val w = max(1, clampedRect.width())
        val h = max(1, clampedRect.height())

        val cropped = Bitmap.createBitmap(source, clampedRect.left, clampedRect.top, w, h)
        if (Math.abs(rotationDegrees) < 0.3f) {
            return cropped
        }

        val matrix = Matrix().apply {
            postRotate(rotationDegrees, w / 2f, h / 2f)
        }
        return Bitmap.createBitmap(cropped, 0, 0, w, h, matrix, true)
    }
}
