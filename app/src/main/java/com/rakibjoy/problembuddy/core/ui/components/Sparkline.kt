package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

/**
 * Minimal polyline sparkline. Points are normalized to their own min/max in Y
 * and plotted across the full width. Caller supplies height via [modifier].
 */
@Composable
fun Sparkline(
    points: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.appExtras.deltaNegative,
    showDot: Boolean = true,
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val minY = points.min()
        val maxY = points.max()
        val range = (maxY - minY).takeIf { it > 0f } ?: 1f
        val w = size.width
        val h = size.height
        val stepX = if (points.size > 1) w / (points.size - 1) else 0f

        fun xy(i: Int): Pair<Float, Float> {
            val nx = i * stepX
            // Higher value -> higher on screen (smaller y).
            val ny = h - ((points[i] - minY) / range) * h
            return nx to ny
        }

        val path = Path().apply {
            val (x0, y0) = xy(0)
            moveTo(x0, y0)
            for (i in 1 until points.size) {
                val (x, y) = xy(i)
                lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = color,
            alpha = 0.7f,
            style = Stroke(
                width = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
        if (showDot) {
            val (lx, ly) = xy(points.size - 1)
            drawCircle(
                color = color,
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(lx, ly),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SparklinePreview() {
    ProblemBuddyTheme {
        Sparkline(
            points = listOf(28f, 24f, 30f, 20f, 26f, 14f, 22f, 10f),
            modifier = Modifier.height(36.dp).then(Modifier),
        )
    }
}
