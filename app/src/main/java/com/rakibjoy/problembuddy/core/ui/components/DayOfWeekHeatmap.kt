package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

private val DayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun DayOfWeekChart(
    dayCounts: IntArray,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val violet = extras.accentVioletSoft
    val track = extras.surfaceRaised
    val maxV = (dayCounts.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            val n = dayCounts.size.coerceAtLeast(1)
            val gap = 6.dp.toPx()
            val totalGap = gap * (n - 1)
            val barW = ((size.width - totalGap) / n).coerceAtLeast(1f)
            val h = size.height
            for (i in 0 until n) {
                val x = i * (barW + gap)
                val v = dayCounts[i]
                val bh = (v.toFloat() / maxV) * h
                // Track
                drawRoundRect(
                    color = track,
                    topLeft = Offset(x, 0f),
                    size = Size(barW, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                )
                drawRoundRect(
                    color = violet,
                    topLeft = Offset(x, h - bh),
                    size = Size(barW, bh),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DayLabels.forEach { l ->
                Text(
                    text = l,
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textTertiary,
                )
            }
        }
    }
}

@Composable
fun HourOfDayChart(
    hourCounts: IntArray,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val violet = extras.accentVioletSoft
    val track = extras.surfaceRaised
    val maxV = (hourCounts.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            val n = hourCounts.size.coerceAtLeast(1)
            val gap = 2.dp.toPx()
            val totalGap = gap * (n - 1)
            val barW = ((size.width - totalGap) / n).coerceAtLeast(1f)
            val h = size.height
            for (i in 0 until n) {
                val x = i * (barW + gap)
                val v = hourCounts[i]
                val bh = (v.toFloat() / maxV) * h
                drawRect(
                    color = track,
                    topLeft = Offset(x, 0f),
                    size = Size(barW, h),
                )
                drawRect(
                    color = violet,
                    topLeft = Offset(x, h - bh),
                    size = Size(barW, bh),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            // Labels at 0, 6, 12, 18 positions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("0", "6", "12", "18", "24").forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.textTertiary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DayHourPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column {
                DayOfWeekChart(dayCounts = intArrayOf(12, 8, 14, 22, 18, 6, 4))
                Spacer(Modifier.height(12.dp))
                HourOfDayChart(hourCounts = IntArray(24) { ((it - 12) * (it - 12)).coerceAtMost(40) })
            }
        }
    }
}
