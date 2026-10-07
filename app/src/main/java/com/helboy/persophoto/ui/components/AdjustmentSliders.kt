package com.helboy.persophoto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helboy.persophoto.ui.theme.StudioCyanPrimary
import com.helboy.persophoto.ui.theme.StudioSurface
import com.helboy.persophoto.ui.theme.StudioSurfaceVariant
import com.helboy.persophoto.ui.theme.TextMuted
import com.helboy.persophoto.ui.theme.TextPrimary
import com.helboy.persophoto.ui.theme.TextSecondary

@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueFormatter: (Float) -> String = { it.toInt().toString() },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioSurface)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueFormatter(value),
                style = MaterialTheme.typography.labelSmall,
                color = StudioCyanPrimary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = StudioCyanPrimary,
                activeTrackColor = StudioCyanPrimary,
                inactiveTrackColor = StudioSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
