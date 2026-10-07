package com.helboy.persophoto.data

import android.graphics.Color

/**
 * Background mode for biometric photos.
 */
enum class BackgroundColorOption(
    val id: String,
    val nameFa: String,
    val colorInt: Int
) {
    PURE_WHITE("white", "سفید خالص (استاندارد)", Color.WHITE),
    LIGHT_BLUE("light_blue", "آبی کم‌رنگ", Color.rgb(232, 240, 254)),
    LIGHT_GRAY("light_gray", "خاکستری روشن", Color.rgb(244, 244, 245)),
    ORIGINAL("original", "تصویر اصلی (بدون تغییر)", Color.TRANSPARENT);

    companion object {
        val DEFAULT = PURE_WHITE
    }
}

/**
 * Fine-tuning configuration for photo enhancement and biometric framing.
 */
data class PhotoProcessingOptions(
    val standard: BiometricStandard = BiometricStandard.DEFAULT,
    val backgroundOption: BackgroundColorOption = BackgroundColorOption.DEFAULT,
    val isAutoEnhanced: Boolean = true,

    // Lighting and color adjustments (-100 .. +100 range normalized or direct)
    val brightnessAdjustment: Float = 0.0f,  // -50f to +50f
    val contrastAdjustment: Float = 0.0f,    // -50f to +50f
    val warmthAdjustment: Float = 0.0f,      // -50f to +50f
    val sharpnessAdjustment: Float = 25.0f,  // 0f to 50f

    // Manual framing overrides
    val zoomScale: Float = 1.0f,             // 0.8f to 1.4f
    val panOffsetX: Float = 0.0f,            // Normalized offset
    val panOffsetY: Float = 0.0f,            // Normalized offset
    val userRotationDegrees: Float = 0.0f    // Fine rotation angle
)
