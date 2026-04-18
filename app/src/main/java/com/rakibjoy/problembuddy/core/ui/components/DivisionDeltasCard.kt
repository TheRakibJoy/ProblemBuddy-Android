package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.feature.profile.DivisionAverage
import kotlin.math.abs

@Composable
fun DivisionDeltasCard(
    divisions: List<DivisionAverage>,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = AppShapes.small
    val maxAbs = (divisions.maxOfOrNull { abs(it.averageDelta) } ?: 0f).coerceAtLeast(1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "DIVISION DELTAS",
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
        divisions.forEach { d ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = d.division,
                    style = MaterialTheme.typography.labelMedium,
                    color = extras.textSecondary,
                    modifier = Modifier.width(64.dp),
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(extras.surfaceRaised, RoundedCornerShape(4.dp)),
                ) {
                    val frac = (abs(d.averageDelta) / maxAbs).coerceIn(0f, 1f)
                    val color = if (d.averageDelta >= 0) extras.deltaPositive else extras.deltaNegative
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(frac)
                            .height(8.dp)
                            .background(color, RoundedCornerShape(4.dp)),
                    )
                }
                Spacer(Modifier.width(10.dp))
                val sign = if (d.averageDelta >= 0) "+" else ""
                Text(
                    text = "$sign${"%.1f".format(d.averageDelta)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (d.averageDelta >= 0) extras.deltaPositive else extras.deltaNegative,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DivisionDeltasCardPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            DivisionDeltasCard(
                divisions = listOf(
                    DivisionAverage("Div. 2", 12.3f, 14),
                    DivisionAverage("Div. 3", -8.0f, 4),
                    DivisionAverage("Edu", 18.5f, 7),
                ),
            )
        }
    }
}
