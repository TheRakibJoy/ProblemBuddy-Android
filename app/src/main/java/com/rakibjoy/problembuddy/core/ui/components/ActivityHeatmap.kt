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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * GitHub-style contribution heatmap: 26 weeks × 7 days, violet-saturation ramped
 * by number of AC submissions per day.
 *
 * Caller passes a map of epoch-day → AC count.
 */
@Composable
fun ActivityHeatmap(
    solvedByDayEpoch: Map<Long, Int>,
    modifier: Modifier = Modifier,
    weeksBack: Int = 26,
) {
    val extras = MaterialTheme.appExtras

    // Colour buckets for the cells. Aligned with the redesign CSS (--surface3 / violet ramp).
    val empty = extras.surfaceRaised
    val l1 = Color(0xFF1E1530)
    val l2 = Color(0xFF4C1D95)
    val l3 = Color(0xFF7C3AED)
    val l4 = Color(0xFFA78BFA)

    val today = LocalDate.now()
    // Align rightmost column to the week containing "today", so the grid ends on today's column.
    val endOfLastCol = today.plusDays((DayOfWeek.SUNDAY.value - today.dayOfWeek.value).toLong())
    val gridStart = endOfLastCol.minusWeeks((weeksBack - 1).toLong()).with(DayOfWeek.MONDAY)
    // Matrix: weeks × 7 rows
    val cells: List<List<Long?>> = (0 until weeksBack).map { w ->
        (0 until 7).map { d ->
            val date = gridStart.plusDays((w * 7 + d).toLong())
            if (date.isAfter(today)) null else date.toEpochDay()
        }
    }

    fun bucketColor(count: Int): Color = when {
        count <= 0 -> empty
        count <= 2 -> l1
        count <= 4 -> l2
        count <= 9 -> l3
        else -> l4
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Cell grid
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp * 7 + 3.dp * 6),
        ) {
            val gap = 3.dp.toPx()
            val cellW = (size.width - (weeksBack - 1) * gap) / weeksBack
            val cellH = (size.height - 6 * gap) / 7
            val cellSize = minOf(cellW, cellH)
            val radius = 2.dp.toPx()
            for (w in 0 until weeksBack) {
                for (d in 0 until 7) {
                    val dayEpoch = cells[w][d] ?: continue
                    val count = solvedByDayEpoch[dayEpoch] ?: 0
                    val x = w * (cellSize + gap)
                    val y = d * (cellSize + gap)
                    drawRoundRect(
                        color = bucketColor(count),
                        topLeft = androidx.compose.ui.geometry.Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                        style = Fill,
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.sm))
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "26 weeks",
                style = MaterialTheme.typography.labelSmall,
                color = extras.textTertiary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "less",
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textTertiary,
                )
                Spacer(Modifier.width(4.dp))
                listOf(empty, l1, l2, l3, l4).forEach { c ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .size(9.dp)
                            .background(c, RoundedCornerShape(2.dp)),
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "more",
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textTertiary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ActivityHeatmapPreview() {
    ProblemBuddyTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            val today = LocalDate.now().toEpochDay()
            val fixture = mapOf(
                today to 5,
                today - 1 to 3,
                today - 2 to 1,
                today - 5 to 10,
                today - 14 to 4,
                today - 32 to 2,
            )
            ActivityHeatmap(solvedByDayEpoch = fixture)
        }
    }
}
