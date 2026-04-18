package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

/**
 * Dense stat card for the 3-up stat row on Home/Profile. 10dp corner, hairline
 * border, 2.5dp accent bar on the left, value in [accent], label uppercase.
 * Optional [deltaText] below the label, green or red per [deltaIsPositive].
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    accent: Color = MaterialTheme.colorScheme.primary,
    deltaText: String? = null,
    deltaIsPositive: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = AppShapes.small // 10dp

    Row(
        modifier = modifier
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .height(IntrinsicSize.Min)
            .fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight()
                .background(accent),
        )
        Column(
            modifier = Modifier
                .padding(start = 6.dp, top = 10.dp, end = 10.dp, bottom = 8.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.72).sp, // -0.04em @ 18sp
                ),
                color = accent,
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.54.sp, // 0.06em @ 9sp
                ),
                color = extras.textTertiary,
            )
            if (deltaText != null) {
                Spacer(Modifier.height(1.dp))
                Text(
                    text = deltaText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (deltaIsPositive) extras.deltaPositive else extras.deltaNegative,
                )
            }
        }
    }
}

/**
 * Back-compat overload keyed by the original `label` + `value` named args.
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    StatCard(
        value = value,
        label = label,
        accent = accent,
        deltaText = null,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun StatCardPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                StatCard(value = "3847", label = "Rating", accent = MaterialTheme.colorScheme.primary, deltaText = "+42", modifier = Modifier.width(100.dp))
                StatCard(value = "1247", label = "Solved", accent = MaterialTheme.appExtras.accentCyanSoft, modifier = Modifier.width(100.dp))
                StatCard(value = "14", label = "Streak", accent = Color(0xFFF59E0B), deltaText = "days", modifier = Modifier.width(100.dp))
            }
        }
    }
}
