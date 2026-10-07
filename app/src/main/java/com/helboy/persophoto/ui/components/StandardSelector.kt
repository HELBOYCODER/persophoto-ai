package com.helboy.persophoto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helboy.persophoto.data.BiometricStandard
import com.helboy.persophoto.ui.theme.StudioCardBorder
import com.helboy.persophoto.ui.theme.StudioCyanPrimary
import com.helboy.persophoto.ui.theme.StudioDarkBg
import com.helboy.persophoto.ui.theme.StudioSurface
import com.helboy.persophoto.ui.theme.StudioSurfaceVariant
import com.helboy.persophoto.ui.theme.TextMuted
import com.helboy.persophoto.ui.theme.TextPrimary
import com.helboy.persophoto.ui.theme.TextSecondary

@Composable
fun StandardSelector(
    selectedStandard: BiometricStandard,
    onStandardSelected: (BiometricStandard) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(BiometricStandard.entries) { standard ->
            val isSelected = standard == selectedStandard

            val bg = if (isSelected) StudioSurfaceVariant else StudioSurface
            val border = if (isSelected) StudioCyanPrimary else StudioCardBorder
            val titleColor = if (isSelected) StudioCyanPrimary else TextPrimary

            Column(
                modifier = Modifier
                    .width(155.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .border(
                        width = if (isSelected) 1.8.dp else 1.dp,
                        color = border,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onStandardSelected(standard) }
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Dimension Pill
                    Text(
                        text = "${standard.widthMm.toInt()}×${standard.heightMm.toInt()} mm",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) StudioDarkBg else TextPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) StudioCyanPrimary else StudioSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = standard.nameFa,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                    maxLines = 1,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = standard.descriptionFa,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
