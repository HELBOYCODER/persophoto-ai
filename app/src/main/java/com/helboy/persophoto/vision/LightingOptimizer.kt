package com.helboy.persophoto.vision

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import com.helboy.persophoto.data.PhotoProcessingOptions
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Intelligent Lighting, Exposure and Color Normalization Engine.
 * Automatically analyzes luminance histograms, applies auto-exposure,
 * neutralizes color casts, adjusts contrast, and sharpens for 300 DPI biometric print standard.
 */
class LightingOptimizer {

    /**
     * Optimizes lighting, exposure, contrast, and color balance.
     */
    fun optimize(
        source: Bitmap,
        faceBox: Rect?,
        options: PhotoProcessingOptions
    ): Bitmap {
        val w = source.width
        val h = source.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        // 1. Analyze face region or central subject for exposure statistics
        val sampleBox = faceBox ?: Rect((w * 0.25f).toInt(), (h * 0.20f).toInt(), (w * 0.75f).toInt(), (h * 0.70f).toInt())
        val stats = computeLuminanceAndColorStats(pixels, w, h, sampleBox)

        // 2. Compute Auto Exposure Gain & Gamma
        val idealFaceLuminance = 175.0f // Standard biometric midtone IRE ~70%
        val currentLuminance = stats.meanLuminance.coerceIn(40.0f, 240.0f)

        val autoGamma: Float
        val autoExposureGain: Float
        if (options.isAutoEnhanced) {
            // Adaptive gamma curve calculation
            val lumRatio = idealFaceLuminance / 255.0f
            val currRatio = currentLuminance / 255.0f
            autoGamma = (ln(lumRatio) / ln(currRatio)).coerceIn(0.55f, 1.45f)
            autoExposureGain = (idealFaceLuminance / currentLuminance).coerceIn(0.85f, 1.40f)
        } else {
            autoGamma = 1.0f
            autoExposureGain = 1.0f
        }

        // 3. Auto White Balance gains (Color Cast Neutralization)
        val autoGainR: Float
        val autoGainB: Float
        if (options.isAutoEnhanced && stats.meanGreen > 30.0f) {
            // Balance red and blue relative to green with damping
            val targetR = (stats.meanGreen / max(30.0f, stats.meanRed)).coerceIn(0.88f, 1.15f)
            val targetB = (stats.meanGreen / max(30.0f, stats.meanBlue)).coerceIn(0.88f, 1.15f)
            autoGainR = 1.0f + (targetR - 1.0f) * 0.65f // Soft damping to prevent over-correction
            autoGainB = 1.0f + (targetB - 1.0f) * 0.65f
        } else {
            autoGainR = 1.0f
            autoGainB = 1.0f
        }

        // 4. Incorporate Manual Adjustments
        // Brightness: -50..+50 -> -60..+60 pixel shift
        val manualBrightnessOffset = options.brightnessAdjustment * 1.2f
        // Contrast: -50..+50 -> 0.7 .. 1.4 factor
        val contrastFactor = 1.0f + (options.contrastAdjustment / 100.0f) * 0.7f
        // Warmth: -50..+50 -> shifts R and B
        val warmthFactor = options.warmthAdjustment / 100.0f

        // Precompute LUT (Look-Up Table) for fast 8-bit channel transformation
        val lutR = IntArray(256)
        val lutG = IntArray(256)
        val lutB = IntArray(256)

        for (i in 0..255) {
            val normalized = i / 255.0f

            // Apply gamma curve
            val gammaCorrected = normalized.pow(autoGamma)

            // Red channel
            var r = (gammaCorrected * 255.0f * autoGainR * autoExposureGain)
            r = ((r - 128f) * contrastFactor) + 128f + manualBrightnessOffset + (warmthFactor * 25f)
            lutR[i] = r.roundToInt().coerceIn(0, 255)

            // Green channel
            var g = (gammaCorrected * 255.0f * autoExposureGain)
            g = ((g - 128f) * contrastFactor) + 128f + manualBrightnessOffset
            lutG[i] = g.roundToInt().coerceIn(0, 255)

            // Blue channel
            var b = (gammaCorrected * 255.0f * autoGainB * autoExposureGain)
            b = ((b - 128f) * contrastFactor) + 128f + manualBrightnessOffset - (warmthFactor * 25f)
            lutB[i] = b.roundToInt().coerceIn(0, 255)
        }

        // Apply LUT to all pixels
        for (i in 0 until (w * h)) {
            val p = pixels[i]
            val r = lutR[Color.red(p)]
            val g = lutG[Color.green(p)]
            val b = lutB[Color.blue(p)]
            pixels[i] = Color.rgb(r, g, b)
        }

        output.setPixels(pixels, 0, w, 0, 0, w, h)

        // 5. Sharpening filter if enabled
        val sharpenAmount = if (options.isAutoEnhanced) {
            max(0.15f, options.sharpnessAdjustment / 100.0f)
        } else {
            options.sharpnessAdjustment / 100.0f
        }

        return if (sharpenAmount > 0.05f) {
            applyUnsharpMask(output, sharpenAmount)
        } else {
            output
        }
    }

    private data class LuminanceAndColorStats(
        val meanLuminance: Float,
        val meanRed: Float,
        val meanGreen: Float,
        val meanBlue: Float
    )

    private fun computeLuminanceAndColorStats(
        pixels: IntArray,
        width: Int,
        height: Int,
        box: Rect
    ): LuminanceAndColorStats {
        val left = box.left.coerceIn(0, width - 1)
        val right = box.right.coerceIn(0, width - 1)
        val top = box.top.coerceIn(0, height - 1)
        val bottom = box.bottom.coerceIn(0, height - 1)

        var count = 0L
        var totalLum = 0.0
        var totalR = 0.0
        var totalG = 0.0
        var totalB = 0.0

        val step = max(1, (right - left) / 60)

        for (y in top..bottom step step) {
            for (x in left..right step step) {
                val p = pixels[y * width + x]
                val r = Color.red(p)
                val g = Color.green(p)
                val b = Color.blue(p)

                // Skip pure white/pure black outliers (e.g. background)
                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                if (lum in 20.0..245.0) {
                    count++
                    totalLum += lum
                    totalR += r
                    totalG += g
                    totalB += b
                }
            }
        }

        return if (count > 0) {
            LuminanceAndColorStats(
                meanLuminance = (totalLum / count).toFloat(),
                meanRed = (totalR / count).toFloat(),
                meanGreen = (totalG / count).toFloat(),
                meanBlue = (totalB / count).toFloat()
            )
        } else {
            LuminanceAndColorStats(150.0f, 150.0f, 150.0f, 150.0f)
        }
    }

    /**
     * High-speed 3x3 unsharp mask for photographic edge definition.
     */
    private fun applyUnsharpMask(source: Bitmap, strength: Float): Bitmap {
        val w = source.width
        val h = source.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val srcPixels = IntArray(w * h)
        val dstPixels = IntArray(w * h)
        source.getPixels(srcPixels, 0, w, 0, 0, w, h)

        val alpha = strength.coerceIn(0.0f, 0.8f)

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val idx = y * w + x
                val center = srcPixels[idx]

                // Fast 4-neighbor laplacian
                val top = srcPixels[(y - 1) * w + x]
                val bottom = srcPixels[(y + 1) * w + x]
                val left = srcPixels[y * w + (x - 1)]
                val right = srcPixels[y * w + (x + 1)]

                val cR = Color.red(center)
                val cG = Color.green(center)
                val cB = Color.blue(center)

                val lapR = 4 * cR - (Color.red(top) + Color.red(bottom) + Color.red(left) + Color.red(right))
                val lapG = 4 * cG - (Color.green(top) + Color.green(bottom) + Color.green(left) + Color.green(right))
                val lapB = 4 * cB - (Color.blue(top) + Color.blue(bottom) + Color.blue(left) + Color.blue(right))

                val outR = (cR + alpha * lapR).roundToInt().coerceIn(0, 255)
                val outG = (cG + alpha * lapG).roundToInt().coerceIn(0, 255)
                val outB = (cB + alpha * lapB).roundToInt().coerceIn(0, 255)

                dstPixels[idx] = Color.rgb(outR, outG, outB)
            }
        }

        // Copy borders
        for (x in 0 until w) {
            dstPixels[x] = srcPixels[x]
            dstPixels[(h - 1) * w + x] = srcPixels[(h - 1) * w + x]
        }
        for (y in 0 until h) {
            dstPixels[y * w] = srcPixels[y * w]
            dstPixels[y * w + (w - 1)] = srcPixels[y * w + (w - 1)]
        }

        output.setPixels(dstPixels, 0, w, 0, 0, w, h)
        return output
    }
}
