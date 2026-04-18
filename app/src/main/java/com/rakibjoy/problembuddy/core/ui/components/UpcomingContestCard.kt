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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.domain.model.UpcomingContest
import kotlinx.coroutines.delay

@Composable
fun UpcomingContestCard(
    contest: UpcomingContest,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(10.dp)
    val violet = extras.accentVioletSoft

    val countdown by produceState(
        initialValue = formatCountdown(contest.startTimeSeconds - nowSeconds()),
        key1 = contest.startTimeSeconds,
    ) {
        while (true) {
            value = formatCountdown(contest.startTimeSeconds - nowSeconds())
            delay(1000L)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, violet.copy(alpha = 0.4f), shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = null,
            tint = violet,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contest.name.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            val prefix = contest.division?.let { "$it · " }.orEmpty()
            Text(
                text = "${prefix}starts in $countdown",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = extras.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TextButton(onClick = onRegister) {
            Text(
                text = "register →",
                style = MaterialTheme.typography.labelSmall,
                color = violet,
            )
        }
    }
}

private fun nowSeconds(): Long = System.currentTimeMillis() / 1000L

private fun formatCountdown(seconds: Long): String {
    if (seconds <= 0L) return "starting now"
    val days = seconds / 86_400L
    val hours = (seconds % 86_400L) / 3_600L
    val minutes = (seconds % 3_600L) / 60L
    val secs = seconds % 60L
    return when {
        days >= 1L -> "${days}d ${hours}h"
        hours >= 1L -> "${hours}h ${minutes}m"
        else -> "${minutes}m ${secs}s"
    }
}

@Preview(showBackground = true)
@Composable
private fun UpcomingContestCardPreview() {
    ProblemBuddyTheme {
        UpcomingContestCard(
            contest = UpcomingContest(
                id = 2100,
                name = "Codeforces Round 999 (Div. 2)",
                startTimeSeconds = System.currentTimeMillis() / 1000L + 3600L * 6,
                durationSeconds = 7200L,
                division = "Div 2",
            ),
            onRegister = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}
