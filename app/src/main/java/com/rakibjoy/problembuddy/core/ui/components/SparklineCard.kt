package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

/**
 * Card wrapping a [Sparkline] with a labeled header. Used on Home for the
 * weak-tag trend call-out.
 */
@Composable
fun SparklineCard(
    tag: String,
    trendLabel: String,
    trendIsDecline: Boolean,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = AppShapes.small
    val trendColor = if (trendIsDecline) extras.deltaNegative else extras.deltaPositive

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelMedium,
                color = extras.textSecondary,
            )
            Text(
                text = trendLabel,
                style = MaterialTheme.typography.labelSmall,
                color = trendColor,
            )
        }
        Spacer(Modifier.height(8.dp))
        Sparkline(
            points = points,
            modifier = Modifier.fillMaxWidth().height(36.dp),
            color = trendColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SparklineCardPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SparklineCard(
                tag = "data structures",
                trendLabel = "↓ 38% solve rate",
                trendIsDecline = true,
                points = listOf(28f, 24f, 30f, 20f, 26f, 14f, 22f, 10f),
            )
        }
    }
}
