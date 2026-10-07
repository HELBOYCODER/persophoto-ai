package com.helboy.persophoto.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.helboy.persophoto.data.BiometricStandard
import com.helboy.persophoto.data.PhotoProcessingOptions
import com.helboy.persophoto.data.PrintSheetConfig
import com.helboy.persophoto.export.PdfDocumentExporter
import com.helboy.persophoto.export.PrintLayoutGenerator
import com.helboy.persophoto.ui.screens.CameraScreen
import com.helboy.persophoto.ui.screens.EditorScreen
import com.helboy.persophoto.ui.screens.HomeScreen
import com.helboy.persophoto.ui.screens.PrintExportScreen
import com.helboy.persophoto.ui.theme.PersoPhotoTheme
import com.helboy.persophoto.ui.theme.StudioDarkBg
import com.helboy.persophoto.vision.PhotoProcessor
import com.helboy.persophoto.vision.ProcessedPhotoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppScreen {
    HOME,
    CAMERA,
    EDITOR,
    PRINT_EXPORT
}

class MainActivity : ComponentActivity() {

    private val photoProcessor by lazy { PhotoProcessor() }
    private val printLayoutGenerator by lazy { PrintLayoutGenerator() }
    private val pdfExporter by lazy { PdfDocumentExporter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersoPhotoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioDarkBg
                ) {
                    PersoPhotoMainApp()
                }
            }
        }
    }

    @Composable
    private fun PersoPhotoMainApp() {
        val scope = rememberCoroutineScope()

        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
        var options by remember { mutableStateOf(PhotoProcessingOptions()) }
        var sheetConfig by remember { mutableStateOf(PrintSheetConfig()) }

        var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var processedResult by remember { mutableStateOf<ProcessedPhotoResult?>(null) }
        var sheetBitmap by remember { mutableStateOf<Bitmap?>(null) }

        var isProcessing by remember { mutableStateOf(false) }
        var isExporting by remember { mutableStateOf(false) }

        // Pipeline trigger function
        fun triggerProcessing(newOptions: PhotoProcessingOptions, src: Bitmap?) {
            val bitmap = src ?: sourceBitmap ?: return
            scope.launch {
                isProcessing = true
                try {
                    val result = photoProcessor.processPhoto(bitmap, newOptions)
                    processedResult = result

                    // Update sheet bitmap
                    val sheet = printLayoutGenerator.generateSheet(
                        photo = result.finalBitmap,
                        standard = newOptions.standard,
                        config = sheetConfig
                    )
                    sheetBitmap = sheet
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "خطا در پردازش تصویر: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isProcessing = false
                }
            }
        }

        fun updateSheet(newConfig: PrintSheetConfig) {
            sheetConfig = newConfig
            val result = processedResult ?: return
            scope.launch(Dispatchers.Default) {
                val sheet = printLayoutGenerator.generateSheet(
                    photo = result.finalBitmap,
                    standard = options.standard,
                    config = newConfig
                )
                sheetBitmap = sheet
            }
        }

        // Gallery Launcher
        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch(Dispatchers.IO) {
                    try {
                        contentResolver.openInputStream(uri)?.use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            if (bmp != null) {
                                sourceBitmap = bmp
                                withContext(Dispatchers.Main) {
                                    currentScreen = AppScreen.EDITOR
                                    triggerProcessing(options, bmp)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // Camera Permission Launcher
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                currentScreen = AppScreen.CAMERA
            } else {
                Toast.makeText(this, "دسترسی دوربین برای عکاسی الزامی است", Toast.LENGTH_SHORT).show()
            }
        }

        when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    onTakePhotoClick = {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) {
                            currentScreen = AppScreen.CAMERA
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onPickGalleryClick = {
                        galleryLauncher.launch("image/*")
                    },
                    onSelectStandard = { std ->
                        options = options.copy(standard = std)
                    }
                )
            }

            AppScreen.CAMERA -> {
                CameraScreen(
                    standard = options.standard,
                    onPhotoCaptured = { bmp ->
                        sourceBitmap = bmp
                        currentScreen = AppScreen.EDITOR
                        triggerProcessing(options, bmp)
                    },
                    onCloseClick = {
                        currentScreen = AppScreen.HOME
                    }
                )
            }

            AppScreen.EDITOR -> {
                EditorScreen(
                    currentResult = processedResult,
                    options = options,
                    isProcessing = isProcessing,
                    onOptionsChanged = { newOpts ->
                        val stdChanged = newOpts.standard != options.standard
                        options = newOpts
                        triggerProcessing(newOpts, null)
                    },
                    onAutoEnhanceClick = {
                        val enhanced = options.copy(
                            isAutoEnhanced = true,
                            backgroundOption = com.helboy.persophoto.data.BackgroundColorOption.PURE_WHITE,
                            brightnessAdjustment = 0f,
                            contrastAdjustment = 0f,
                            warmthAdjustment = 0f,
                            sharpnessAdjustment = 25f,
                            zoomScale = 1.0f
                        )
                        options = enhanced
                        triggerProcessing(enhanced, null)
                    },
                    onProceedToPrint = {
                        currentScreen = AppScreen.PRINT_EXPORT
                        updateSheet(sheetConfig)
                    },
                    onBackClick = {
                        currentScreen = AppScreen.HOME
                    }
                )
            }

            AppScreen.PRINT_EXPORT -> {
                PrintExportScreen(
                    standard = options.standard,
                    sheetBitmap = sheetBitmap,
                    sheetConfig = sheetConfig,
                    isExporting = isExporting,
                    onSheetConfigChanged = { newConfig ->
                        updateSheet(newConfig)
                    },
                    onExportImageClick = {
                        val sheet = sheetBitmap ?: return@PrintExportScreen
                        scope.launch {
                            isExporting = true
                            val uri = pdfExporter.saveImageToGallery(sheet, "PersoPhoto_${options.standard.id}")
                            isExporting = false
                            if (uri != null) {
                                Toast.makeText(this@MainActivity, "تصویر با کیفیت ۳۰۰ DPI در گالری ذخیره شد ✓", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this@MainActivity, "خطا در ذخیره تصویر", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onExportPdfClick = {
                        val sheet = sheetBitmap ?: return@PrintExportScreen
                        scope.launch {
                            isExporting = true
                            val (file, uri) = pdfExporter.exportToPrintablePdf(sheet, sheetConfig)
                            isExporting = false
                            Toast.makeText(this@MainActivity, "فایل PDF چاپی ایجاد شد: ${file.name}", Toast.LENGTH_LONG).show()

                            // Open PDF intent
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                startActivity(intent)
                            } catch (e: Exception) {
                                // PDF viewer not available
                            }
                        }
                    },
                    onShareClick = {
                        val sheet = sheetBitmap ?: return@PrintExportScreen
                        scope.launch {
                            isExporting = true
                            val (file, uri) = pdfExporter.saveImageForShare(sheet)
                            isExporting = false
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری عکس چاپی"))
                        }
                    },
                    onBackClick = {
                        currentScreen = AppScreen.EDITOR
                    }
                )
            }
        }
    }
}
