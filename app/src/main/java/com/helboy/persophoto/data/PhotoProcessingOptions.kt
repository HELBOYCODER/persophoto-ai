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
 * Quick studio color & lighting presets.
 */
enum class StudioColorPreset(
    val id: String,
    val nameFa: String,
    val descriptionFa: String
) {
    BALANCED_STUDIO("balanced", "🌟 نور استاندارد پرسنلی", "تنظیم متوازن کنتراست و نور برای انواع چهره"),
    SHADOW_LIFT("shadow_lift", "☀️ رفع تیرگی و سایه", "افزایش روشنایی گردن و زیر چشم بدون سوزاندن نور"),
    CLEAN_COOL("clean_cool", "❄️ سفید و شفاف", "حذف زردی چهره و شفافیت بالای خطوط چشم و مو"),
    WARM_NATURAL("warm_natural", "🔆 پرتره گرم و طبیعی", "رنگ چهره گرم و زنده با حفظ استانداردهای اداری"),
    CUSTOM("custom", "🎨 تنظیم دستی", "تنظیم دقیق تمام متغیرها توسط کاربر")
}

/**
 * Professional GPUImage-grade photo adjustment configuration.
 * Calibrated with real photographic units: EV exposure stops, S-curve contrast,
 * GPUImage shadow lift, highlight recovery, YIQ temperature & tint, and unsharp mask.
 */
data class PhotoProcessingOptions(
    val standard: BiometricStandard = BiometricStandard.DEFAULT,
    val backgroundOption: BackgroundColorOption = BackgroundColorOption.DEFAULT,
    val isAutoEnhanced: Boolean = true,
    val activePreset: StudioColorPreset = StudioColorPreset.BALANCED_STUDIO,

    // Professional Photographic Grading Parameters
    val exposureEv: Float = 0.0f,          // -2.0f to +2.0f EV stops (normal: 0.0)
    val contrast: Float = 1.0f,            // 0.5f to 1.8f (normal: 1.0)
    val shadowLift: Float = 0.0f,          // 0.0f to 1.0f (lifts dark shadows under neck)
    val highlightRecovery: Float = 1.0f,   // 0.0f to 1.0f (1.0 = normal, 0.0 = max recovery)
    val temperature: Float = 0.0f,         // -1.0f (cool blue) to +1.0f (warm golden)
    val tint: Float = 0.0f,                // -1.0f (green) to +1.0f (magenta)
    val saturation: Float = 1.0f,          // 0.0f (B&W) to 2.0f (vibrant)
    val sharpness: Float = 0.25f,          // 0.0f to 1.0f (unsharp mask for 300 DPI print)

    // Framing & Composition
    val zoomScale: Float = 1.0f,           // 0.85f to 1.35f
    val panOffsetX: Float = 0.0f,          // Normalized offset
    val panOffsetY: Float = 0.0f,          // Normalized offset
    val userRotationDegrees: Float = 0.0f  // Fine rotation angle (-10 to +10 deg)
) {
    companion object {
        fun fromPreset(preset: StudioColorPreset, standard: BiometricStandard = BiometricStandard.DEFAULT): PhotoProcessingOptions {
            return when (preset) {
                StudioColorPreset.BALANCED_STUDIO -> PhotoProcessingOptions(
                    standard = standard,
                    activePreset = preset,
                    exposureEv = 0.20f,
                    contrast = 1.10f,
                    shadowLift = 0.35f,
                    highlightRecovery = 0.90f,
                    temperature = 0.0f,
                    tint = 0.0f,
                    saturation = 1.02f,
                    sharpness = 0.30f
                )
                StudioColorPreset.SHADOW_LIFT -> PhotoProcessingOptions(
                    standard = standard,
                    activePreset = preset,
                    exposureEv = 0.55f,
                    contrast = 1.05f,
                    shadowLift = 0.70f,
                    highlightRecovery = 0.85f,
                    temperature = 0.0f,
                    tint = 0.0f,
                    saturation = 1.00f,
                    sharpness = 0.25f
                )
                StudioColorPreset.CLEAN_COOL -> PhotoProcessingOptions(
                    standard = standard,
                    activePreset = preset,
                    exposureEv = 0.30f,
                    contrast = 1.15f,
                    shadowLift = 0.40f,
                    highlightRecovery = 0.95f,
                    temperature = -0.15f,
                    tint = 0.0f,
                    saturation = 0.98f,
                    sharpness = 0.40f
                )
                StudioColorPreset.WARM_NATURAL -> PhotoProcessingOptions(
                    standard = standard,
                    activePreset = preset,
                    exposureEv = 0.15f,
                    contrast = 1.08f,
                    shadowLift = 0.30f,
                    highlightRecovery = 0.90f,
                    temperature = 0.20f,
                    tint = 0.05f,
                    saturation = 1.08f,
                    sharpness = 0.25f
                )
                StudioColorPreset.CUSTOM -> PhotoProcessingOptions(
                    standard = standard,
                    activePreset = preset
                )
            }
        }
    }
}
