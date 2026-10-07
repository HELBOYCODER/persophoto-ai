package com.helboy.persophoto.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helboy.persophoto.data.BackgroundColorOption
import com.helboy.persophoto.data.BiometricStandard
import com.helboy.persophoto.data.PhotoProcessingOptions
import com.helboy.persophoto.ui.components.BiometricGuideOverlay
import com.helboy.persophoto.ui.components.LabeledSlider
import com.helboy.persophoto.ui.components.StandardSelector
import com.helboy.persophoto.ui.theme.StudioCardBorder
import com.helboy.persophoto.ui.theme.StudioCyanPrimary
import com.helboy.persophoto.ui.theme.StudioDarkBg
import com.helboy.persophoto.ui.theme.StudioSurface
import com.helboy.persophoto.ui.theme.StudioSurfaceVariant
import com.helboy.persophoto.ui.theme.StudioTerracotta
import com.helboy.persophoto.ui.theme.TextMuted
import com.helboy.persophoto.ui.theme.TextPrimary
import com.helboy.persophoto.ui.theme.TextSecondary
import com.helboy.persophoto.vision.ProcessedPhotoResult

@Composable
fun EditorScreen(
    currentResult: ProcessedPhotoResult?,
    options: PhotoProcessingOptions,
    isProcessing: Boolean,
    onOptionsChanged: (PhotoProcessingOptions) -> Unit,
    onAutoEnhanceClick: () -> Unit,
    onProceedToPrint: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showBiometricOverlay by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
    ) {
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = options.standard.nameFa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${options.standard.widthMm.toInt()}×${options.standard.heightMm.toInt()} میلی‌متر (300 DPI)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioCyanPrimary,
                    fontSize = 11.sp
                )
            }

            // Toggle Overlay Icon
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (showBiometricOverlay) StudioCyanPrimary else StudioSurfaceVariant)
                    .clickable { showBiometricOverlay = !showBiometricOverlay }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Crop,
                    contentDescription = "راهنمای کادر",
                    tint = if (showBiometricOverlay) StudioDarkBg else TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2. Photo Canvas Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentResult != null) {
                val bitmap = currentResult.finalBitmap

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, StudioCyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "پیش‌نمایش عکس پرسنلی",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(0.92f)
                    )

                    if (showBiometricOverlay) {
                        BiometricGuideOverlay(
                            standard = options.standard,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA0D1117)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = StudioCyanPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "در حال پردازش هوشمند...",
                            color = StudioCyanPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. One-Tap Smart Auto Fix Banner & Compliance Feedback
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Auto Enhance Button
            Button(
                onClick = onAutoEnhanceClick,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCyanPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = StudioDarkBg,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "بهینه‌سازی هوشمند (سفید کردن + نور)",
                    color = StudioDarkBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // 4. Tab Navigation (قالب، پس‌زمینه، نور و رنگ، کادر)
        val tabTitles = listOf("قالب ابعاد", "پس‌زمینه", "نور و رنگ", "کادر و زوم")
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = StudioSurface,
            contentColor = StudioCyanPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = StudioCyanPrimary
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) StudioCyanPrimary else TextSecondary
                        )
                    }
                )
            }
        }

        // 5. Tab Content Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(StudioSurfaceVariant)
                .padding(vertical = 8.dp)
        ) {
            when (selectedTab) {
                // 0: Presets
                0 -> {
                    StandardSelector(
                        selectedStandard = options.standard,
                        onStandardSelected = { newStd ->
                            onOptionsChanged(options.copy(standard = newStd))
                        }
                    )
                }

                // 1: Background Whitening
                1 -> {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(BackgroundColorOption.entries) { bgOpt ->
                            val isSelected = options.backgroundOption == bgOpt
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) StudioSurface else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) StudioCyanPrimary else StudioCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onOptionsChanged(options.copy(backgroundOption = bgOpt))
                                    }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (bgOpt == BackgroundColorOption.ORIGINAL) StudioCardBorder
                                            else Color(bgOpt.colorInt)
                                        )
                                        .border(1.dp, StudioCardBorder, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = bgOpt.nameFa,
                                    fontSize = 11.sp,
                                    color = if (isSelected) StudioCyanPrimary else TextPrimary
                                )
                            }
                        }
                    }
                }

                // 2: Lighting & Color Sliders
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LabeledSlider(
                            label = "روشنایی (Brightness)",
                            value = options.brightnessAdjustment,
                            onValueChange = { onOptionsChanged(options.copy(brightnessAdjustment = it)) },
                            valueRange = -40f..40f
                        )
                        LabeledSlider(
                            label = "کنتراست (Contrast)",
                            value = options.contrastAdjustment,
                            onValueChange = { onOptionsChanged(options.copy(contrastAdjustment = it)) },
                            valueRange = -40f..40f
                        )
                        LabeledSlider(
                            label = "دمای رنگ (گرم / سرد)",
                            value = options.warmthAdjustment,
                            onValueChange = { onOptionsChanged(options.copy(warmthAdjustment = it)) },
                            valueRange = -30f..30f
                        )
                    }
                }

                // 3: Crop, Zoom & Rotate
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LabeledSlider(
                            label = "بزرگنمایی چهره (Zoom)",
                            value = options.zoomScale,
                            onValueChange = { onOptionsChanged(options.copy(zoomScale = it)) },
                            valueRange = 0.85f..1.35f,
                            valueFormatter = { String.format("%.2f×", it) }
                        )
                        LabeledSlider(
                            label = "تراز چرخش (Rotation)",
                            value = options.userRotationDegrees,
                            onValueChange = { onOptionsChanged(options.copy(userRotationDegrees = it)) },
                            valueRange = -10f..10f,
                            valueFormatter = { "${it.toInt()}°" }
                        )
                    }
                }
            }
        }

        // 6. Proceed to Print Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = onProceedToPrint,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCyanPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = StudioDarkBg
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مرحله بعد: طرح‌بندی چاپ ۶ و ۱۲ تایی (عکس و PDF)",
                    color = StudioDarkBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
