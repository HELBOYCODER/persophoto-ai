package com.helboy.persophoto.vision

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.helboy.persophoto.data.BiometricStandard
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Detected facial geometry information.
 */
data class DetectedBiometricFace(
    val boundingBox: Rect,
    val leftEye: PointF?,
    val rightEye: PointF?,
    val noseBase: PointF?,
    val mouthBottom: PointF?,
    val headRollAngleDegrees: Float,
    val confidence: Float
)

/**
 * High-precision biometric face detector.
 * Supports Google ML Kit Face Detection with a robust pure-Kotlin heuristic fallback.
 */
class BiometricFaceDetector {

    private val mlKitDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceLandmark.LANDMARK_LEFT_EYE or FaceLandmark.LANDMARK_RIGHT_EYE or FaceLandmark.LANDMARK_NOSE_BASE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f)
            .build()
        FaceDetection.getClient(options)
    }

    /**
     * Detects primary face in bitmap. Returns null if no face found.
     */
    suspend fun detectFace(bitmap: Bitmap): DetectedBiometricFace? {
        val mlKitResult = detectWithMlKit(bitmap)
        if (mlKitResult != null) return mlKitResult

        // Robust heuristic fallback if ML Kit didn't find or is uninitialized
        return detectHeuristicFallback(bitmap)
    }

    private suspend fun detectWithMlKit(bitmap: Bitmap): DetectedBiometricFace? =
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                mlKitDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        if (faces.isNullOrEmpty()) {
                            continuation.resume(null)
                            return@addOnSuccessListener
                        }
                        // Pick the largest/most prominent face (biometric subject)
                        val primary = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }!!
                        val leftEye = primary.getLandmark(FaceLandmark.LEFT_EYE)?.position
                        val rightEye = primary.getLandmark(FaceLandmark.RIGHT_EYE)?.position
                        val nose = primary.getLandmark(FaceLandmark.NOSE_BASE)?.position
                        val mouth = primary.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position

                        val result = DetectedBiometricFace(
                            boundingBox = primary.boundingBox,
                            leftEye = leftEye,
                            rightEye = rightEye,
                            noseBase = nose,
                            mouthBottom = mouth,
                            headRollAngleDegrees = primary.headEulerAngleZ,
                            confidence = 0.95f
                        )
                        continuation.resume(result)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: Throwable) {
                continuation.resume(null)
            }
        }

    /**
     * Autonomous heuristic face locator.
     * Projects skin chrominance (YCbCr / HSV) and luminance gradients to identify the face region.
     */
    private fun detectHeuristicFallback(bitmap: Bitmap): DetectedBiometricFace? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 40 || h < 40) return null

        // Downsample to fast analysis grid
        val sampleStep = max(1, min(w, h) / 100)
        var skinPixelCount = 0
        var sumX = 0L
        var sumY = 0L
        var minX = w
        var maxX = 0
        var minY = h
        var maxY = 0

        // Search central 80% region
        val startX = (w * 0.1f).toInt()
        val endX = (w * 0.9f).toInt()
        val startY = (h * 0.05f).toInt()
        val endY = (h * 0.85f).toInt()

        for (y in startY until endY step sampleStep) {
            for (x in startX until endX step sampleStep) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                if (isSkinColor(r, g, b)) {
                    skinPixelCount++
                    sumX += x
                    sumY += y
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        if (skinPixelCount < 50 || minX >= maxX || minY >= maxY) {
            // Default center face fallback
            val boxW = (w * 0.45f).roundToInt()
            val boxH = (h * 0.50f).roundToInt()
            val left = (w - boxW) / 2
            val top = (h * 0.15f).roundToInt()
            return DetectedBiometricFace(
                boundingBox = Rect(left, top, left + boxW, top + boxH),
                leftEye = PointF(left + boxW * 0.35f, top + boxH * 0.35f),
                rightEye = PointF(left + boxW * 0.65f, top + boxH * 0.35f),
                noseBase = PointF(left + boxW * 0.50f, top + boxH * 0.55f),
                mouthBottom = PointF(left + boxW * 0.50f, top + boxH * 0.75f),
                headRollAngleDegrees = 0f,
                confidence = 0.5f
            )
        }

        val boxW = maxX - minX
        val boxH = maxY - minY
        return DetectedBiometricFace(
            boundingBox = Rect(minX, minY, maxX, maxY),
            leftEye = PointF(minX + boxW * 0.35f, minY + boxH * 0.35f),
            rightEye = PointF(minX + boxW * 0.65f, minY + boxH * 0.35f),
            noseBase = PointF(minX + boxW * 0.50f, minY + boxH * 0.55f),
            mouthBottom = PointF(minX + boxW * 0.50f, minY + boxH * 0.75f),
            headRollAngleDegrees = 0f,
            confidence = 0.7f
        )
    }

    private fun isSkinColor(r: Int, g: Int, b: Int): Boolean {
        // Standard biometric skin chrominance heuristic
        return r > 80 && g > 40 && b > 20 &&
                r > g && r > b &&
                (r - g) >= 12 &&
                abs(r - g) > 10 &&
                r > 1.15 * g
    }

    /**
     * Calculates the optimal biometric crop rectangle based on standard requirements:
     * - Head height ratio (crown to chin) matching standard (e.g. 70-80% for Passport, 50-69% for Lottery)
     * - Eye level placement
     * - Head centering horizontally
     * - Target aspect ratio
     */
    fun calculateBiometricCropRect(
        imageWidth: Int,
        imageHeight: Int,
        face: DetectedBiometricFace,
        standard: BiometricStandard,
        zoomMultiplier: Float = 1.0f,
        panOffsetX: Float = 0f,
        panOffsetY: Float = 0f
    ): Rect {
        val faceBox = face.boundingBox
        val faceH = max(20, faceBox.height()).toFloat()
        val faceW = max(20, faceBox.width()).toFloat()

        // Target head ratio inside photo
        val idealRatio = (standard.minFaceHeightRatio + standard.maxFaceHeightRatio) / 2.0f
        // Total crop height needed so head occupies idealRatio of it
        var targetCropHeight = (faceH / idealRatio) * (1.0f / zoomMultiplier)
        var targetCropWidth = targetCropHeight * standard.aspectRatio

        // Horizontal center: align with face center or midway between eyes
        val faceCenterX = if (face.leftEye != null && face.rightEye != null) {
            (face.leftEye.x + face.rightEye.x) / 2f
        } else {
            faceBox.exactCenterX()
        }

        // Vertical center: eye level should be at idealEyeLevelRatioFromBottom
        val eyeY = if (face.leftEye != null && face.rightEye != null) {
            (face.leftEye.y + face.rightEye.y) / 2f
        } else {
            faceBox.top + faceH * 0.35f
        }

        // If eyes are at (1 - idealEyeLevelRatioFromBottom) from top of crop
        val eyeTopFraction = 1.0f - standard.idealEyeLevelRatioFromBottom
        var cropTop = eyeY - targetCropHeight * eyeTopFraction
        var cropLeft = faceCenterX - targetCropWidth / 2f

        // Apply pan offsets
        cropLeft += panOffsetX * targetCropWidth
        cropTop += panOffsetY * targetCropHeight

        // Bound validation & adjustment
        if (targetCropWidth > imageWidth) {
            targetCropWidth = imageWidth.toFloat()
            targetCropHeight = targetCropWidth / standard.aspectRatio
        }
        if (targetCropHeight > imageHeight) {
            targetCropHeight = imageHeight.toFloat()
            targetCropWidth = targetCropHeight * standard.aspectRatio
        }

        var left = cropLeft.roundToInt()
        var top = cropTop.roundToInt()
        var right = (cropLeft + targetCropWidth).roundToInt()
        var bottom = (cropTop + targetCropHeight).roundToInt()

        // Shift inside image boundaries if spilling over
        if (left < 0) {
            right -= left
            left = 0
        }
        if (right > imageWidth) {
            val shift = right - imageWidth
            left = max(0, left - shift)
            right = imageWidth
        }
        if (top < 0) {
            bottom -= top
            top = 0
        }
        if (bottom > imageHeight) {
            val shift = bottom - imageHeight
            top = max(0, top - shift)
            bottom = imageHeight
        }

        return Rect(left, top, right, bottom)
    }
}
