package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.feature.profile.TierMonthPoint

@Composable
fun TierStackedArea(
    points: List<TierMonthPoint>,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val months = points.map { it.yearMonth }.distinct().sorted()
    val presentTiers = points.map { it.tier }.distinct()
    // Stack top-down: LEGENDARY on top, NEWBIE at bottom in drawing order
    // means we draw NEWBIE first (largest area covering from bottom),
    // then higher tiers on top. Equivalent to cumulative sum from NEWBIE up.
    val tierOrderBottomUp = Tier.entries.toList() // NEWBIE..LEGENDARY

    // Build map[yearMonth][tier] = cumulativeCount
    val byMonth: Map<String, Map<Tier, Int>> = points.groupBy { it.yearMonth }
        .mapValues { (_, list) -> list.associate { it.tier to it.cumulativeCount } }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            if (months.size < 2) return@Canvas
            val w = size.width
            val h = size.height

            // Per month, compute per-tier count (cumulative, already cumulative per tier)
            // Stack = sum over all tiers at that month.
            val stackedTotals = months.map { m ->
                val mp = byMonth[m].orEmpty()
                tierOrderBottomUp.sumOf { mp[it] ?: 0 }
            }
            val maxTotal = (stackedTotals.max()).coerceAtLeast(1)

            // For stacked area: for each month, compute bottomY -> topY per tier
            // Pre-compute layer Y offsets per month.
            val stepX = if (months.size > 1) w / (months.size - 1) else 0f

            // Running cumulative from bottom per month index
            val bottomCumulative = IntArray(months.size) { 0 }
            tierOrderBottomUp.forEach { tier ->
                // Skip fully-absent tiers to keep draw count low but still draw if present elsewhere
                val monthValues = months.mapIndexed { i, m ->
                    val v = byMonth[m]?.get(tier) ?: 0
                    i to v
                }
                val anyNonZero = monthValues.any { it.second > 0 }
                if (!anyNonZero) return@forEach

                val path = Path()
                // Top edge: bottom + value
                months.forEachIndexed { i, _ ->
                    val v = monthValues[i].second
                    val top = bottomCumulative[i] + v
                    val x = i * stepX
                    val y = h - (top.toFloat() / maxTotal) * h
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                // Bottom edge reverse
                for (i in months.indices.reversed()) {
                    val x = i * stepX
                    val y = h - (bottomCumulative[i].toFloat() / maxTotal) * h
                    path.lineTo(x, y)
                }
                path.close()
                drawPath(
                    path = path,
                    color = tier.palette().strong,
                    alpha = 0.9f,
                )
                // Update cumulative
                months.forEachIndexed { i, _ ->
                    bottomCumulative[i] = bottomCumulative[i] + monthValues[i].second
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            presentTiers.forEach { tier ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(tier.palette().strong, CircleShape),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = tier.label.take(3),
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.textSecondary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TierStackedAreaPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            val months = listOf("2024-01", "2024-02", "2024-03", "2024-04", "2024-05", "2024-06")
            val data = months.flatMapIndexed { i, m ->
                listOf(
                    TierMonthPoint(m, Tier.NEWBIE, 20 + i * 3),
                    TierMonthPoint(m, Tier.PUPIL, 10 + i * 2),
                    TierMonthPoint(m, Tier.SPECIALIST, 4 + i),
                    TierMonthPoint(m, Tier.EXPERT, i),
                )
            }
            TierStackedArea(points = data)
        }
    }
}
