package com.helboy.persophoto.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.helboy.persophoto.data.StudioColorPreset
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
import java.util.Locale

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
    var selectedTab by remember { mutableIntStateOf(2) } // Default to Lighting & Color tab
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                .padding(horizontal = 16.dp, vertical = 4.dp),
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
                        modifier = Modifier.fillMaxSize(0.94f)
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
                        .background(Color(0x990D1117)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = StudioCyanPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "در حال پردازش رنگ و نور استودیویی...",
                            color = StudioCyanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Quick Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAutoEnhanceClick,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCyanPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = StudioDarkBg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "بهینه‌سازی هوشمند (تک‌کلیک)",
                    color = StudioDarkBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = {
                    onOptionsChanged(
                        PhotoProcessingOptions.fromPreset(
                            StudioColorPreset.BALANCED_STUDIO,
                            options.standard
                        )
                    )
                },
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StudioCardBorder)
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "ریست",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 4. Tab Navigation (قالب، پس‌زمینه، نور و رنگ، کادر)
        val tabTitles = listOf("قالب ابعاد", "پس‌زمینه", "نور و رنگ استودیویی", "کادر و زوم")
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

        // 5. Tab Content Panel (Spacious & Scrollable)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .background(StudioSurfaceVariant)
                .padding(vertical = 6.dp)
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
                                        .size(36.dp)
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

                // 2: Lighting & Color Sliders (GPUImage-Grade)
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick Studio Presets Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            items(StudioColorPreset.entries) { preset ->
                                val isPresetActive = options.activePreset == preset
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isPresetActive) StudioCyanPrimary else StudioSurface)
                                        .border(
                                            1.dp,
                                            if (isPresetActive) StudioCyanPrimary else StudioCardBorder,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            val newOptions = PhotoProcessingOptions.fromPreset(preset, options.standard)
                                                .copy(backgroundOption = options.backgroundOption)
                                            onOptionsChanged(newOptions)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = preset.nameFa,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPresetActive) StudioDarkBg else TextPrimary
                                    )
                                }
                            }
                        }

                        // 1. Photographic Exposure (EV stops)
                        LabeledSlider(
                            label = "نوردهی عکاسی (Exposure EV)",
                            value = options.exposureEv,
                            onValueChange = {
                                onOptionsChanged(options.copy(exposureEv = it, activePreset = StudioColorPreset.CUSTOM))
                            },
                            valueRange = -1.5f..1.5f,
                            valueFormatter = {
                                String.format(Locale.US, "%+.2f EV", it)
                            }
                        )

                        // 2. Linear Contrast
                        LabeledSlider(
                            label = "کنتراست (Contrast)",
                            value = options.contrast,
                            onValueChange = {
                                onOptionsChanged(options.copy(contrast = it, activePreset = StudioColorPreset.CUSTOM))
                            },
                            valueRange = 0.6f..1.6f,
                            valueFormatter = {
                                String.format(Locale.US, "%.2f×", it)
                            }
                        )

                        // 3. GPUImage Shadow Lift (Lifting dark neck shadows)
                        LabeledSlider(
                            label = "روشن‌سازی سایه‌ها (Shadow Lift)",
                            value = options.shadowLift,
                            onValueChange = {
                                onOptionsChanged(options.copy(shadowLift = it, activePreset = StudioColorPreset.CUSTOM))
                            },
                            valueRange = 0.0f..1.0f,
                            valueFormatter = {
                                String.format(Locale.US, "+%d%%", (it * 100).toInt())
                            }
                        )

                        // 4. Highlight Recovery (Control blown-out flash)
                        LabeledSlider(
                            label = "کنترل هایلایت‌ها (Highlight Recovery)",
                            value = options.highlightRecovery,
                            onValueChange = {
                                onOptionsChanged(options.copy(highlightRecovery = it, activePreset = StudioColorPreset.CUSTOM))
                            },
                            valueRange = 0.3f..1.0f,
                            valueFormatter = {
                                String.format(Locale.US, "%d%%", (it * 100).toInt())
                            }
                        )

                        // 5. YIQ Color Temperature (Cool vs Warm)
                        LabeledSlider(
                            label = "دمای رنگ (سرد / گرم)",
                            value = options.temperature,
                            onValueChange = {
                                onOptionsChanged(options.copy(temperature = it, activePreset = StudioColorPreset.CUSTOM))
                            },
                            valueRange = -0.5f..0.5f,
                            valueFormatter = {
                                when {
                                    it < -0.05f -> String.format(Locale.US, "سرد (%d%%)", (-it * 200).toInt())
                                    it > 0.05f -> String.format(Locale.US, "گرم (+%d%%)", (it * 200).toInt())
                                    else -> "طبیعی (0)"
                                }
                            }
                        )

                        // 6. Saturation
                        LabeledSlider(
                            label = "شادابی و غلظت رنگ (Saturation)",
                            value = options.saturation,
                            onValueChange = {
                                onOptionsChanged(options.copy(saturation = it, activePreset = StudioColorPreset.CUSTOM))
                            },
                            valueRange = 0.5f..1.6f,
                            valueFormatter = {
                                String.format(Locale.US, "%.2f×", it)
                            }
                        )

                        // 7. 300 DPI Photographic Sharpening (Unsharp Mask)
                        LabeledSlider(
                            label = "وضوح و شارپنس مو و چشم (Sharpness)",
                            value = options.sharpness,
                            onValueChange = {
                                onOptionsChanged(options.copy(sharpness = it, activePreset = StudioColorPreset.CUSTOM))
                            },
                            valueRange = 0.0f..0.8f,
                            valueFormatter = {
                                String.format(Locale.US, "%d%%", (it * 100).toInt())
                            }
                        )
                    }
                }

                // 3: Crop, Zoom & Rotate
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LabeledSlider(
                            label = "بزرگنمایی چهره (Zoom)",
                            value = options.zoomScale,
                            onValueChange = { onOptionsChanged(options.copy(zoomScale = it)) },
                            valueRange = 0.85f..1.35f,
                            valueFormatter = { String.format(Locale.US, "%.2f×", it) }
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
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Button(
                onClick = onProceedToPrint,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCyanPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
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
                    fontSize = 13.sp
                )
            }
        }
    }
}
