package com.helboy.persophoto.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.helboy.persophoto.data.BiometricStandard
import com.helboy.persophoto.data.PrintSheetConfig
import com.helboy.persophoto.data.PrintSheetType
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Generates photographic lab print sheets (6-pack, 12-pack, single) at 300 DPI.
 * Renders cutting guide marks, margins, and crisp photo replicas.
 */
class PrintLayoutGenerator {

    /**
     * Generates a 300 DPI print-ready Bitmap sheet.
     */
    fun generateSheet(
        photo: Bitmap,
        standard: BiometricStandard,
        config: PrintSheetConfig
    ): Bitmap {
        return when (config.sheetType) {
            PrintSheetType.SINGLE -> generateSingleSheet(photo, config)
            PrintSheetType.SHEET_6_PACK -> generateMultiPackSheet(
                photo = photo,
                standard = standard,
                sheetType = PrintSheetType.SHEET_6_PACK,
                config = config
            )
            PrintSheetType.SHEET_12_PACK -> generateMultiPackSheet(
                photo = photo,
                standard = standard,
                sheetType = PrintSheetType.SHEET_12_PACK,
                config = config
            )
            PrintSheetType.SHEET_A4 -> generateMultiPackSheet(
                photo = photo,
                standard = standard,
                sheetType = PrintSheetType.SHEET_A4,
                config = config
            )
        }
    }

    private fun generateSingleSheet(photo: Bitmap, config: PrintSheetConfig): Bitmap {
        if (!config.addWhiteBorderAroundPhotos) return photo

        // Add 2mm white safe border around single photo
        val borderPx = (2f / 25.4f * config.targetDpi).roundToInt()
        val outW = photo.width + borderPx * 2
        val outH = photo.height + borderPx * 2

        val sheet = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(photo, borderPx.toFloat(), borderPx.toFloat(), paint)

        if (config.showCutLines) {
            drawCutMarks(canvas, borderPx.toFloat(), borderPx.toFloat(), photo.width.toFloat(), photo.height.toFloat())
        }

        return sheet
    }

    private fun generateMultiPackSheet(
        photo: Bitmap,
        standard: BiometricStandard,
        sheetType: PrintSheetType,
        config: PrintSheetConfig
    ): Bitmap {
        val sheetW = sheetType.paperWidthPx300Dpi
        val sheetH = sheetType.paperHeightPx300Dpi

        val sheet = Bitmap.createBitmap(sheetW, sheetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        // Clean white photo paper canvas
        canvas.drawColor(Color.WHITE)

        val cols = sheetType.columns
        val rows = sheetType.rows

        // Physical size of one photo in pixels at 300 DPI
        val photoW = (standard.widthMm / 25.4f * config.targetDpi).roundToInt()
        val photoH = (standard.heightMm / 25.4f * config.targetDpi).roundToInt()

        // Calculate spacing and margins
        val totalPhotosW = cols * photoW
        val totalPhotosH = rows * photoH

        // If photos exceed sheet, downscale proportionally with 5% safety margin
        val scale = if (totalPhotosW > sheetW * 0.92f || totalPhotosH > sheetH * 0.92f) {
            val scaleX = (sheetW * 0.88f) / totalPhotosW
            val scaleY = (sheetH * 0.88f) / totalPhotosH
            minOf(scaleX, scaleY)
        } else {
            1.0f
        }

        val scaledPhotoW = (photoW * scale).roundToInt()
        val scaledPhotoH = (photoH * scale).roundToInt()

        val gapX = max(10, ((sheetW - (cols * scaledPhotoW)) / (cols + 1)))
        val gapY = max(10, ((sheetH - (rows * scaledPhotoH)) / (rows + 1)))

        val startX = (sheetW - (cols * scaledPhotoW + (cols - 1) * gapX)) / 2
        val startY = (sheetH - (rows * scaledPhotoH + (rows - 1) * gapY)) / 2

        val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        val srcRect = Rect(0, 0, photo.width, photo.height)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = startX + c * (scaledPhotoW + gapX)
                val y = startY + r * (scaledPhotoH + gapY)
                val dstRect = RectF(
                    x.toFloat(),
                    y.toFloat(),
                    (x + scaledPhotoW).toFloat(),
                    (y + scaledPhotoH).toFloat()
                )

                // Draw photo
                canvas.drawBitmap(photo, srcRect, dstRect, bitmapPaint)

                // Draw cutting guides around each photo
                if (config.showCutLines) {
                    drawCutMarks(canvas, dstRect.left, dstRect.top, dstRect.width(), dstRect.height())
                }
            }
        }

        return sheet
    }

    /**
     * Draws professional corner L-shaped cut marks and subtle dashed lines for clean scissor cutting.
     */
    private fun drawCutMarks(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val markPaint = Paint().apply {
            color = Color.rgb(180, 190, 205)
            strokeWidth = 2.0f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val markLen = 14.0f
        val offset = 3.0f // 1mm outside photo edge

        val left = x - offset
        val top = y - offset
        val right = x + w + offset
        val bottom = y + h + offset

        // Top-Left L
        canvas.drawLine(left, top, left + markLen, top, markPaint)
        canvas.drawLine(left, top, left, top + markLen, markPaint)

        // Top-Right L
        canvas.drawLine(right, top, right - markLen, top, markPaint)
        canvas.drawLine(right, top, right, top + markLen, markPaint)

        // Bottom-Left L
        canvas.drawLine(left, bottom, left + markLen, bottom, markPaint)
        canvas.drawLine(left, bottom, left, bottom - markLen, markPaint)

        // Bottom-Right L
        canvas.drawLine(right, bottom, right - markLen, bottom, markPaint)
        canvas.drawLine(right, bottom, right, bottom - markLen, markPaint)

        // Subtle thin perimeter cut guideline
        val dashPaint = Paint().apply {
            color = Color.rgb(220, 225, 235)
            strokeWidth = 1.0f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
            isAntiAlias = true
        }
        canvas.drawRect(left, top, right, bottom, dashPaint)
    }
}
