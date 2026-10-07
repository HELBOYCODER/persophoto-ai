package com.helboy.persophoto.data

import kotlin.math.roundToInt

/**
 * Biometric photo standards specification.
 * Contains exact millimeter dimensions, target 300 DPI resolutions,
 * head height ratio boundaries (crown to chin), eye level guidance, and descriptions.
 */
enum class BiometricStandard(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val descriptionFa: String,
    val widthMm: Float,
    val heightMm: Float,
    val minFaceHeightRatio: Float,
    val maxFaceHeightRatio: Float,
    val idealEyeLevelRatioFromBottom: Float,
    val guidelinesFa: List<String>
) {
    IRAN_3X4(
        id = "iran_3x4",
        nameFa = "۳×۴ سانتی‌متر (ایران)",
        nameEn = "Iran 3x4 cm",
        descriptionFa = "شناسنامه، کارت ملی هوشمند، گواهینامه و مدارک استخدامی",
        widthMm = 30f,
        heightMm = 40f,
        minFaceHeightRatio = 0.70f,
        maxFaceHeightRatio = 0.78f,
        idealEyeLevelRatioFromBottom = 0.60f,
        guidelinesFa = listOf(
            "پس‌زمینه کاملاً سفید و یکدست",
            "صورت تمام‌رخ، مستقیم رو به لنز",
            "بدون عینک آفتابی یا شیشه‌های بازتابنده نور",
            "گردی صورت و پیشانی کاملاً مشخص باشد"
        )
    ),

    ICAO_PASSPORT(
        id = "icao_passport",
        nameFa = "پاسپورت بین‌المللی (ICAO)",
        nameEn = "ICAO Passport 35x45 mm",
        descriptionFa = "گذرنامه بین‌المللی، ترکیه، امارات، گرجستان و استانداردهای ICAO 9303",
        widthMm = 35f,
        heightMm = 45f,
        minFaceHeightRatio = 0.70f,
        maxFaceHeightRatio = 0.80f,
        idealEyeLevelRatioFromBottom = 0.62f,
        guidelinesFa = listOf(
            "طول چهره بین ۳۲ تا ۳۶ میلی‌متر (۷۰ تا ۸۰ درصد کادر)",
            "پس‌زمینه سفید خالص یا خاکستری خنثی",
            "حالت چهره کاملاً طبیعی بدون لبخند باز",
            "فاصله ۲ تا ۴ میلی‌متر بالای سر تا لبه کادر"
        )
    ),

    US_LOTTERY_VISA(
        id = "us_lottery_visa",
        nameFa = "لاتاری و ویزای آمریکا (DV)",
        nameEn = "US Visa & DV Lottery 50x50 mm (2x2 inch)",
        descriptionFa = "ثبت‌نام گرین‌کارت لاتاری DV، ویزای توریستی و تحصیلی آمریکا",
        widthMm = 50.8f,
        heightMm = 50.8f,
        minFaceHeightRatio = 0.50f,
        maxFaceHeightRatio = 0.69f,
        idealEyeLevelRatioFromBottom = 0.59f,
        guidelinesFa = listOf(
            "کادر مربع با رزولوشن ۶۰۰×۶۰۰ تا ۱۲۰۰×۱۲۰۰ پیکسل",
            "طول سر بین ۵۰ تا ۶۹ درصد ارتفاع کل عکس (۱ تا ۱.۳۷۵ اینچ)",
            "ارتفاع چشم‌ها بین ۵۶ تا ۶۹ درصد از پایین عکس",
            "عینک کاملاً ممنوع است (حتی طبی)",
            "پس‌زمینه سفید ساده و بدون کوچکترین سایه"
        )
    ),

    SCHENGEN_VISA(
        id = "schengen_visa",
        nameFa = "ویزای شنگن (اروپا)",
        nameEn = "Schengen Visa 35x45 mm",
        descriptionFa = "ویزای توریستی و کاری ۲۷ کشور حوزه شنگن اروپا",
        widthMm = 35f,
        heightMm = 45f,
        minFaceHeightRatio = 0.72f,
        maxFaceHeightRatio = 0.80f,
        idealEyeLevelRatioFromBottom = 0.62f,
        guidelinesFa = listOf(
            "پوشش ۷۰ تا ۸۰ درصدی کادر توسط چهره (۳۲ تا ۳۶ میلی‌متر)",
            "پس‌زمینه سفید روشن یا خاکستری یکنواخت",
            "شاخه‌های مو روی چشم یا پیشانی را نپوشاند",
            "نوردهی متعادل بدون بازتاب فلاش روی گونه یا بینی"
        )
    ),

    CANADA_VISA(
        id = "canada_visa",
        nameFa = "ویزای کانادا (۵×۷ سانت)",
        nameEn = "Canada Visa 50x70 mm",
        descriptionFa = "ویزای توریستی، تحصیلی و اقامت دائم کانادا (IRCC)",
        widthMm = 50f,
        heightMm = 70f,
        minFaceHeightRatio = 0.60f,
        maxFaceHeightRatio = 0.72f,
        idealEyeLevelRatioFromBottom = 0.60f,
        guidelinesFa = listOf(
            "ابعاد فیزیکی دقیق ۵۰ در ۷۰ میلی‌متر",
            "طول چهره از چانه تا بالای سر بین ۳۱ تا ۳۶ میلی‌متر",
            "پس‌زمینه سفید خالص یا خاکستری بسیار روشن",
            "شاخه‌های چاپی شفاف با حداقل ۶۰۰ دی‌پی‌آی"
        )
    ),

    STANDARD_4X6(
        id = "standard_4x6",
        nameFa = "عکس پرسنلی ۴×۶ سانتی‌متر",
        nameEn = "Standard 4x6 cm ID",
        descriptionFa = "عکس پرونده‌های قضایی، مدارک مهندسی و اداری خاص",
        widthMm = 40f,
        heightMm = 60f,
        minFaceHeightRatio = 0.65f,
        maxFaceHeightRatio = 0.75f,
        idealEyeLevelRatioFromBottom = 0.60f,
        guidelinesFa = listOf(
            "ابعاد ۴۰ در ۶۰ میلی‌متر (نسبت ۲:۳)",
            "زمینه سفید یا خاکستری روشن",
            "تقارن کامل دو طرف شانه و صورت"
        )
    );

    val aspectRatio: Float
        get() = widthMm / heightMm

    /** Target pixel width for 300 DPI lab printing */
    val targetWidthPx300Dpi: Int
        get() = (widthMm / 25.4f * 300f).roundToInt()

    /** Target pixel height for 300 DPI lab printing */
    val targetHeightPx300Dpi: Int
        get() = (heightMm / 25.4f * 300f).roundToInt()

    companion object {
        val DEFAULT = IRAN_3X4
        fun fromId(id: String): BiometricStandard =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
}
