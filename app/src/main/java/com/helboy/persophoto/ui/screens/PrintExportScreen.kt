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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helboy.persophoto.data.BiometricStandard
import com.helboy.persophoto.data.PrintSheetConfig
import com.helboy.persophoto.data.PrintSheetType
import com.helboy.persophoto.ui.theme.StudioCardBorder
import com.helboy.persophoto.ui.theme.StudioCyanPrimary
import com.helboy.persophoto.ui.theme.StudioDarkBg
import com.helboy.persophoto.ui.theme.StudioSurface
import com.helboy.persophoto.ui.theme.StudioSurfaceVariant
import com.helboy.persophoto.ui.theme.StudioTerracotta
import com.helboy.persophoto.ui.theme.TextMuted
import com.helboy.persophoto.ui.theme.TextPrimary
import com.helboy.persophoto.ui.theme.TextSecondary

@Composable
fun PrintExportScreen(
    standard: BiometricStandard,
    sheetBitmap: Bitmap?,
    sheetConfig: PrintSheetConfig,
    isExporting: Boolean,
    onSheetConfigChanged: (PrintSheetConfig) -> Unit,
    onExportImageClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "طرح‌بندی و چاپ عکس",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "خروجی ۶ تایی و ۱۲ تایی (تصویر 300DPI و PDF)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioCyanPrimary,
                    fontSize = 11.sp
                )
            }
        }

        // 2. Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sheet Canvas Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioSurface)
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(14.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (sheetBitmap != null) {
                    Image(
                        bitmap = sheetBitmap.asImageBitmap(),
                        contentDescription = "پیش‌نمایش برگه چاپی",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (isExporting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xBB0D1117)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = StudioCyanPrimary)
                    }
                }
            }

            // Sheet Type Selector (Single, 6-Pack, 12-Pack, A4)
            Text(
                text = "انتخاب نحوه چیدمان برگه چاپ",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    PrintSheetType.SHEET_6_PACK,
                    PrintSheetType.SHEET_12_PACK,
                    PrintSheetType.SINGLE
                ).forEach { type ->
                    val isSelected = sheetConfig.sheetType == type
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSheetConfigChanged(sheetConfig.copy(sheetType = type))
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) StudioSurfaceVariant else StudioSurface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) StudioCyanPrimary else StudioCardBorder
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (type) {
                                    PrintSheetType.SHEET_6_PACK -> "۶ تایی"
                                    PrintSheetType.SHEET_12_PACK -> "۱۲ تایی"
                                    PrintSheetType.SINGLE -> "تک عکس"
                                    else -> "A4"
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) StudioCyanPrimary else TextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (type) {
                                    PrintSheetType.SHEET_6_PACK -> "۱۰×۱۵ سانت"
                                    PrintSheetType.SHEET_12_PACK -> "۱۳×۱۸ سانت"
                                    PrintSheetType.SINGLE -> "استاندارد"
                                    else -> "A4"
                                },
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Cut Guidelines Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioSurface)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "نمایش خطوط راهنمای برش (قیچی)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "براکت‌های گوشه و خطوط نقطه‌چین برای برش دقیق",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = sheetConfig.showCutLines,
                    onCheckedChange = {
                        onSheetConfigChanged(sheetConfig.copy(showCutLines = it))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioCyanPrimary,
                        checkedTrackColor = StudioSurfaceVariant
                    )
                )
            }

            // Sheet Print Specs Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "مشخصات فنی خروجی چاپ",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• ابعاد کاغذ: ${sheetConfig.sheetType.paperNameFa}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• تعداد عکس در هر برگه: ${sheetConfig.sheetType.photoCount} قطعه",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• رزولوشن خروجی: ۳۰۰ دی‌پی‌آی (کیفیت استاندارد چاپ عکس)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• مقیاس فایل PDF: ۱:۱ (بدون نیاز به تغییر سایز در پرینتر)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 3. Export Buttons Dock
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // High-Res Image Export
            Button(
                onClick = onExportImageClick,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCyanPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = StudioDarkBg
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ذخیره عکس چاپی (Ultra 300 DPI JPG)",
                    color = StudioDarkBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Printable PDF Export
                Button(
                    onClick = onExportPdfClick,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioTerracotta),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "خروجی PDF چاپی",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Share Button
                OutlinedButton(
                    onClick = onShareClick,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StudioCyanPrimary)
                    ),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = StudioCyanPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اشتراک‌گذاری",
                        color = StudioCyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
