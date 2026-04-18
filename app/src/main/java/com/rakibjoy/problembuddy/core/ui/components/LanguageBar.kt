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
fun LanguageBar(
    counts: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    if (counts.isEmpty()) return
    val total = counts.values.sum().takeIf { it > 0 } ?: return
    val extras = MaterialTheme.appExtras

    fun colorFor(lang: String): Color = when (lang) {
        "C++" -> extras.accentVioletSoft
        "Python" -> extras.accentCyanSoft
        "Java" -> Color(0xFFF59E0B)
        "Kotlin" -> extras.deltaPositive
        "JavaScript" -> Color(0xFFF59E0B)
        else -> extras.textTertiary
    }

    // Normalize keys loosely: match canonical names
    fun canon(name: String): String {
        val n = name.lowercase()
        return when {
            n.contains("c++") || n.contains("gnu c++") -> "C++"
            n.contains("python") || n.contains("pypy") -> "Python"
            n.contains("kotlin") -> "Kotlin"
            n.contains("java") && !n.contains("script") -> "Java"
            n.contains("javascript") || n.contains("node") -> "JavaScript"
            else -> name
        }
    }

    val merged = counts.entries
        .groupBy { canon(it.key) }
        .mapValues { (_, list) -> list.sumOf { it.value } }

    val sortedAll = merged.entries.sortedByDescending { it.value }
    val top5 = sortedAll.take(5)
    val otherCount = sortedAll.drop(5).sumOf { it.value }
    val display: List<Pair<String, Int>> = buildList {
        top5.forEach { add(it.key to it.value) }
        if (otherCount > 0) add("Other" to otherCount)
    }

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
            display.forEach { (lang, c) ->
                val segW = (c.toFloat() / total) * w
                drawRect(
                    color = colorFor(lang),
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
            display.take(4).forEachIndexed { i, (lang, c) ->
                val pct = ((c.toFloat() / total) * 100f).roundToInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colorFor(lang), CircleShape),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$lang $pct%",
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.textSecondary,
                    )
                }
                if (i < display.take(4).lastIndex) {
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

@Preview(showBackground = true)
@Composable
private fun LanguageBarPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            LanguageBar(
                counts = mapOf(
                    "GNU C++17" to 620,
                    "Python 3" to 140,
                    "Kotlin" to 80,
                    "Java 11" to 40,
                    "Rust" to 10,
                ),
            )
        }
    }
}
