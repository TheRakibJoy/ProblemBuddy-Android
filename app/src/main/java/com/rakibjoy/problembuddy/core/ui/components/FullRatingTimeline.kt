package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.feature.profile.ContestResult
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val TierBoundaries = listOf(
    1200 to Tier.PUPIL,
    1400 to Tier.SPECIALIST,
    1600 to Tier.EXPERT,
    1900 to Tier.CANDIDATE_MASTER,
    2100 to Tier.MASTER,
    2300 to Tier.INTL_MASTER,
    2400 to Tier.GRANDMASTER,
    2600 to Tier.INTL_GRANDMASTER,
    3000 to Tier.LEGENDARY,
)

@Composable
fun FullRatingTimeline(
    contests: List<ContestResult>,
    onTap: (ContestResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val violet = extras.accentVioletSoft
    val sorted = contests.sortedBy { it.timeSeconds }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .pointerInput(sorted) {
                    if (sorted.isEmpty()) return@pointerInput
                    detectTapsSimple { offset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val minT = sorted.first().timeSeconds.toFloat()
                        val maxT = sorted.last().timeSeconds.toFloat()
                        val range = (maxT - minT).takeIf { it > 0f } ?: 1f
                        var best = sorted.first()
                        var bestDx = Float.MAX_VALUE
                        sorted.forEach { c ->
                            val x = ((c.timeSeconds - minT) / range) * w
                            val dx = abs(x - offset.x)
                            if (dx < bestDx) {
                                bestDx = dx
                                best = c
                            }
                        }
                        onTap(best)
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (sorted.isEmpty()) return@Canvas
                val w = size.width
                val h = size.height

                val ratings = sorted.map { it.newRating }
                val dataMin = ratings.min()
                val dataMax = ratings.max()
                val pad = 100
                val yMin = (dataMin - pad).coerceAtMost(1100).toFloat()
                val yMax = (dataMax + pad).coerceAtLeast(1500).toFloat()
                val yRange = (yMax - yMin).takeIf { it > 0f } ?: 1f
                val minT = sorted.first().timeSeconds.toFloat()
                val maxT = sorted.last().timeSeconds.toFloat()
                val tRange = (maxT - minT).takeIf { it > 0f } ?: 1f

                fun xOf(tSec: Long) = ((tSec - minT) / tRange) * w
                fun yOf(rating: Int) = h - ((rating - yMin) / yRange) * h

                // Tier boundary dashed lines
                val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                TierBoundaries.forEach { (r, tier) ->
                    if (r.toFloat() in yMin..yMax) {
                        val y = yOf(r)
                        drawLine(
                            color = tier.palette().strong.copy(alpha = 0.35f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 0.7.dp.toPx(),
                            pathEffect = dash,
                        )
                    }
                }

                // Build line path
                val linePath = Path()
                val fillPath = Path()
                sorted.forEachIndexed { i, c ->
                    val x = xOf(c.timeSeconds)
                    val y = yOf(c.newRating)
                    if (i == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(xOf(sorted.last().timeSeconds), h)
                fillPath.close()

                val currentTier = Tier.forMaxRating(sorted.last().newRating)
                val tierColor = currentTier.palette().strong

                drawPath(
                    path = fillPath,
                    color = tierColor,
                    alpha = 0.15f,
                )
                drawPath(
                    path = linePath,
                    color = violet,
                    alpha = 0.9f,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
                // Latest dot
                val last = sorted.last()
                drawCircle(
                    color = violet,
                    radius = 3.5.dp.toPx(),
                    center = Offset(xOf(last.timeSeconds), yOf(last.newRating)),
                )
            }
        }
        if (sorted.isNotEmpty()) {
            val fmt = DateTimeFormatter.ofPattern("MMM yy")
            val minLabel = Instant.ofEpochSecond(sorted.first().timeSeconds)
                .atZone(ZoneId.systemDefault()).format(fmt)
            val maxLabel = Instant.ofEpochSecond(sorted.last().timeSeconds)
                .atZone(ZoneId.systemDefault()).format(fmt)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = minLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textTertiary,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                Text(
                    text = maxLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textTertiary,
                )
            }
        }
    }
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTapsSimple(
    onTap: (Offset) -> Unit,
) {
    detectTapGestures(onTap = onTap)
}

@Preview(showBackground = true)
@Composable
private fun FullRatingTimelinePreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            val now = Instant.now().epochSecond
            val data = listOf(
                ContestResult(1, "R1", 500, 1000, 1150, now - 86400L * 300, "Div. 3"),
                ContestResult(2, "R2", 400, 1150, 1310, now - 86400L * 240, "Div. 2"),
                ContestResult(3, "R3", 800, 1310, 1250, now - 86400L * 180, "Div. 2"),
                ContestResult(4, "R4", 300, 1250, 1480, now - 86400L * 120, "Div. 2"),
                ContestResult(5, "R5", 250, 1480, 1620, now - 86400L * 60, "Div. 2"),
                ContestResult(6, "R6", 180, 1620, 1720, now - 86400L * 10, "Div. 2"),
            )
            FullRatingTimeline(contests = data, onTap = {})
        }
    }
}
