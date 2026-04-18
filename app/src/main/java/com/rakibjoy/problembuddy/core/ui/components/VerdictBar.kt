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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import kotlin.math.roundToInt

@Composable
fun VerdictBar(
    counts: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    if (counts.isEmpty()) return
    val total = counts.values.sum().takeIf { it > 0 } ?: return
    val extras = MaterialTheme.appExtras

    fun colorFor(v: String): Color = when (v) {
        "OK" -> extras.deltaPositive
        "WRONG_ANSWER" -> extras.deltaNegative
        "TIME_LIMIT_EXCEEDED" -> Color(0xFFF59E0B)
        "RUNTIME_ERROR" -> Color(0xFFEF4444)
        "COMPILATION_ERROR" -> extras.textSecondary
        "MEMORY_LIMIT_EXCEEDED" -> Color(0xFFA78BFA)
        else -> extras.textTertiary
    }

    val sorted = counts.entries.sortedByDescending { it.value }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(percent = 50)),
        ) {
            var cursor = 0f
            val w = size.width
            val h = size.height
            sorted.forEach { (v, c) ->
                val segW = (c.toFloat() / total) * w
                drawRect(
                    color = colorFor(v),
                    topLeft = Offset(cursor, 0f),
                    size = Size(segW, h),
                )
                cursor += segW
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            sorted.take(4).forEachIndexed { i, (v, c) ->
                val pct = ((c.toFloat() / total) * 100f).roundToInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colorFor(v), CircleShape),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${shortLabel(v)} $pct%",
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.textSecondary,
                    )
                }
                if (i < sorted.take(4).lastIndex) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.textTertiary,
                    )
                }
            }
        }
    }
}

private fun shortLabel(verdict: String): String = when (verdict) {
    "OK" -> "OK"
    "WRONG_ANSWER" -> "WA"
    "TIME_LIMIT_EXCEEDED" -> "TLE"
    "RUNTIME_ERROR" -> "RE"
    "COMPILATION_ERROR" -> "CE"
    "MEMORY_LIMIT_EXCEEDED" -> "MLE"
    else -> verdict.take(3)
}

@Preview(showBackground = true)
@Composable
private fun VerdictBarPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            VerdictBar(
                counts = mapOf(
                    "OK" to 730,
                    "WRONG_ANSWER" to 180,
                    "TIME_LIMIT_EXCEEDED" to 50,
                    "RUNTIME_ERROR" to 30,
                    "COMPILATION_ERROR" to 10,
                ),
            )
        }
    }
}
