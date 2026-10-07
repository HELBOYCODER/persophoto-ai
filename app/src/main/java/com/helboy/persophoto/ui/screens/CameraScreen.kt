package com.helboy.persophoto.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.helboy.persophoto.data.BiometricStandard
import com.helboy.persophoto.ui.components.BiometricGuideOverlay
import com.helboy.persophoto.ui.theme.StudioCyanPrimary
import com.helboy.persophoto.ui.theme.StudioDarkBg
import com.helboy.persophoto.ui.theme.StudioTerracotta
import com.helboy.persophoto.ui.theme.TextPrimary
import java.nio.ByteBuffer

@Composable
fun CameraScreen(
    standard: BiometricStandard,
    onPhotoCaptured: (Bitmap) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Default to back camera for high-resolution studio photo, togglable to front
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    // Clean, robust camera binding reacting whenever lensFacing changes
    LaunchedEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val selector = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

                if (!cameraProvider.hasCamera(selector)) {
                    Toast.makeText(context, "دوربین موردنظر در این دستگاه پشتیبانی نمی‌شود", Toast.LENGTH_SHORT).show()
                    return@addListener
                }

                cameraProvider.unbindAll()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()
                imageCapture = capture

                val boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    capture
                )
                camera = boundCamera
                isTorchOn = false
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "خطا در اتصال به دوربین: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(StudioDarkBg)) {
        // 1. CameraX Preview Layer
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Biometric Guide Overlay (Head Oval & Eye line)
        BiometricGuideOverlay(
            standard = standard,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Top Navigation & Camera Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = Color.White
                    )
                }

                // Standard Title & Camera Lens Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xDD161B22))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${standard.nameFa} • ${if (lensFacing == CameraSelector.LENS_FACING_BACK) "دوربین پشت" else "دوربین جلو"}",
                        color = StudioCyanPrimary,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Flash / Torch Toggle Button (Available on back camera)
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        IconButton(
                            onClick = {
                                val cam = camera ?: return@IconButton
                                val nextState = !isTorchOn
                                try {
                                    cam.cameraControl.enableTorch(nextState)
                                    isTorchOn = nextState
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isTorchOn) StudioCyanPrimary else Color(0x99000000))
                        ) {
                            Icon(
                                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "فلاش",
                                tint = if (isTorchOn) StudioDarkBg else Color.White
                            )
                        }
                    }

                    // Flip Camera Button (Front <-> Back)
                    IconButton(
                        onClick = {
                            val newFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                            lensFacing = newFacing
                            Toast.makeText(
                                context,
                                if (newFacing == CameraSelector.LENS_FACING_BACK) "تغییر به دوربین پشت" else "تغییر به دوربین جلو",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "چرخش دوربین",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Guide Hint Banner
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xAA0D1117))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "سر را داخل بیضی قرار دهید و مستقیم به لنز نگاه کنید",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
        }

        // 4. Bottom Controls (Shutter Button)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Shutter Ring & Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(4.dp, StudioCyanPrimary, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(if (isCapturing) StudioCyanPrimary.copy(alpha = 0.5f) else Color.White)
                    .clickable(enabled = !isCapturing) {
                        val capture = imageCapture
                        if (capture == null) {
                            Toast.makeText(context, "دوربین در حال آماده‌سازی است...", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }
                        isCapturing = true
                        val currentLens = lensFacing
                        capture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = imageProxyToBitmap(image, currentLens == CameraSelector.LENS_FACING_FRONT)
                                    image.close()
                                    isCapturing = false
                                    onPhotoCaptured(bitmap)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                    isCapturing = false
                                    Toast.makeText(context, "خطا در ثبت عکس: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = StudioDarkBg,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy, isFrontCamera: Boolean): Bitmap {
    val plane = image.planes[0]
    val buffer: ByteBuffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val matrix = Matrix()
    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
    if (isFrontCamera) {
        // Mirror front camera horizontally
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
