package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.feature.profile.ActivityStats
import com.rakibjoy.problembuddy.feature.profile.TagScore
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun TagRadar(
    scores: List<TagScore>,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val violetStrong = extras.accentVioletSoft
    val violetFill = violetStrong.copy(alpha = 0.25f)
    val borderColor = extras.borderSubtle
    val labelColor = extras.textSecondary

    // Canonical spokes
    val spokes: List<TagScore> = ActivityStats.RadarTags.map { tag ->
        scores.firstOrNull { it.tag == tag } ?: TagScore(tag, 0)
    }.let { list ->
        if (list.size >= 8) list.take(8) else list + List(8 - list.size) { TagScore("", 0) }
    }
    val maxCount = (spokes.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)

    Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = min(size.width, size.height) / 2f * 0.72f
            val n = spokes.size

            // Background rings
            val rings = 4
            for (r in 1..rings) {
                val rr = radius * r / rings
                val ringPath = Path()
                for (i in 0 until n) {
                    val angle = (-Math.PI / 2 + 2 * Math.PI * i / n).toFloat()
                    val x = cx + rr * cos(angle)
                    val y = cy + rr * sin(angle)
                    if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
                }
                ringPath.close()
                drawPath(
                    path = ringPath,
                    color = borderColor,
                    style = Stroke(width = 0.5.dp.toPx()),
                )
            }
            // Spokes
            for (i in 0 until n) {
                val angle = (-Math.PI / 2 + 2 * Math.PI * i / n).toFloat()
                drawLine(
                    color = borderColor,
                    start = Offset(cx, cy),
                    end = Offset(cx + radius * cos(angle), cy + radius * sin(angle)),
                    strokeWidth = 0.5.dp.toPx(),
                )
            }

            // Data polygon
            val dataPath = Path()
            for (i in 0 until n) {
                val angle = (-Math.PI / 2 + 2 * Math.PI * i / n).toFloat()
                val frac = spokes[i].count.toFloat() / maxCount
                val rr = radius * frac
                val x = cx + rr * cos(angle)
                val y = cy + rr * sin(angle)
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()
            drawPath(path = dataPath, color = violetFill)
            drawPath(
                path = dataPath,
                color = violetStrong,
                style = Stroke(width = 1.2.dp.toPx()),
            )

            // Labels
            val labelPaint = android.graphics.Paint().apply {
                color = labelColor.toArgb()
                textSize = 10.sp.toPx()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            for (i in 0 until n) {
                val tag = spokes[i].tag
                if (tag.isEmpty()) continue
                val angle = (-Math.PI / 2 + 2 * Math.PI * i / n).toFloat()
                val lr = radius + 14.dp.toPx()
                val x = cx + lr * cos(angle)
                val y = cy + lr * sin(angle) + labelPaint.textSize / 3f
                drawContext.canvas.nativeCanvas.drawText(tag, x, y, labelPaint)
            }
        }
    }
}

private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)

@Preview(showBackground = true)
@Composable
private fun TagRadarPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TagRadar(
                scores = listOf(
                    TagScore("dp", 48),
                    TagScore("graphs", 21),
                    TagScore("math", 30),
                    TagScore("strings", 12),
                    TagScore("ds", 26),
                    TagScore("greedy", 40),
                    TagScore("constructive algorithms", 17),
                    TagScore("implementation", 52),
                ),
            )
        }
    }
}
