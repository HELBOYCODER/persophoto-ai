package com.helboy.persophoto.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.helboy.persophoto.data.PrintSheetConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * High-quality image & printable PDF exporter.
 * Emits 300 DPI JPEGs/PNGs and physical 1:1 scale printable PDF documents.
 */
class PdfDocumentExporter(private val context: Context) {

    /**
     * Saves sheet bitmap to internal/external cache and returns a shareable FileProvider Uri.
     */
    suspend fun saveImageForShare(
        bitmap: Bitmap,
        filenamePrefix: String = "persophoto"
    ): Pair<File, Uri> = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(dir, "${filenamePrefix}_${System.currentTimeMillis()}.jpg")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        Pair(file, uri)
    }

    /**
     * Saves sheet bitmap directly to device public gallery (Pictures/PersoPhoto).
     */
    suspend fun saveImageToGallery(
        bitmap: Bitmap,
        title: String = "PersoPhoto_Print_300DPI"
    ): Uri? = withContext(Dispatchers.IO) {
        val filename = "${title}_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PersoPhoto")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext null

        try {
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            null
        }
    }

    /**
     * Generates a 1:1 physical scale printable PDF document matching the sheet's dimensions.
     * When printed from any phone or PC, prints at exact millimeter scale.
     */
    suspend fun exportToPrintablePdf(
        sheetBitmap: Bitmap,
        config: PrintSheetConfig,
        filenamePrefix: String = "persophoto_sheet"
    ): Pair<File, Uri> = withContext(Dispatchers.IO) {
        val sheetType = config.sheetType
        val pageW = sheetType.paperWidthPoints.toInt()
        val pageH = sheetType.paperHeightPoints.toInt()

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

        // Draw 300 DPI high-resolution bitmap onto exact physical PDF bounds
        val srcRect = Rect(0, 0, sheetBitmap.width, sheetBitmap.height)
        val dstRect = RectF(0f, 0f, pageW.toFloat(), pageH.toFloat())
        canvas.drawBitmap(sheetBitmap, srcRect, dstRect, paint)

        pdfDocument.finishPage(page)

        val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(dir, "${filenamePrefix}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        Pair(file, uri)
    }
}
