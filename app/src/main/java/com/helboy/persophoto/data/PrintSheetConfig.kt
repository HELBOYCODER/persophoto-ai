package com.helboy.persophoto.data

import kotlin.math.roundToInt

/**
 * Print Sheet layout mode: Single, 6-Pack, 12-Pack, or A4.
 */
enum class PrintSheetType(
    val id: String,
    val nameFa: String,
    val paperNameFa: String,
    val photoCount: Int,
    val paperWidthMm: Float,
    val paperHeightMm: Float,
    val columns: Int,
    val rows: Int
) {
    SINGLE(
        id = "single",
        nameFa = "تک عکس مستقل",
        paperNameFa = "اندازه استاندارد مشخص شده",
        photoCount = 1,
        paperWidthMm = 35f,
        paperHeightMm = 45f,
        columns = 1,
        rows = 1
    ),

    SHEET_6_PACK(
        id = "sheet_6",
        nameFa = "برگه ۶ تایی (کاغذ ۱۰×۱۵ سانت)",
        paperNameFa = "کاغذ استاندارد لابراتوار ۴×۶ اینچ (10x15 cm)",
        photoCount = 6,
        paperWidthMm = 100f,
        paperHeightMm = 150f,
        columns = 2,
        rows = 3
    ),

    SHEET_12_PACK(
        id = "sheet_12",
        nameFa = "برگه ۱۲ تایی (کاغذ ۱۳×۱۸ سانت)",
        paperNameFa = "کاغذ عکس ۵×۷ اینچ (13x18 cm)",
        photoCount = 12,
        paperWidthMm = 130f,
        paperHeightMm = 180f,
        columns = 3,
        rows = 4
    ),

    SHEET_A4(
        id = "sheet_a4",
        nameFa = "برگه انبوه A4 (چاپ پرینتر خانگی)",
        paperNameFa = "کاغذ استاندارد A4 (21x29.7 cm)",
        photoCount = 16,
        paperWidthMm = 210f,
        paperHeightMm = 297f,
        columns = 4,
        rows = 4
    );

    val paperWidthPx300Dpi: Int
        get() = (paperWidthMm / 25.4f * 300f).roundToInt()

    val paperHeightPx300Dpi: Int
        get() = (paperHeightMm / 25.4f * 300f).roundToInt()

    val paperWidthPoints: Float
        get() = paperWidthMm / 25.4f * 72f

    val paperHeightPoints: Float
        get() = paperHeightMm / 25.4f * 72f

    companion object {
        val DEFAULT = SHEET_6_PACK
    }
}

/**
 * Options for generating print sheets.
 */
data class PrintSheetConfig(
    val sheetType: PrintSheetType = PrintSheetType.DEFAULT,
    val showCutLines: Boolean = true,
    val addWhiteBorderAroundPhotos: Boolean = true,
    val includeHeaderLabel: Boolean = false,
    val targetDpi: Int = 300
)
