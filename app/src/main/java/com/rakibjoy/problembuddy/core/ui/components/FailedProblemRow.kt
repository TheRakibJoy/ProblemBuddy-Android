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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.feature.profile.FailedProblem

@Composable
fun FailedProblemRow(
    item: FailedProblem,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(10.dp)
    val tier = Tier.forMaxRating(item.rating ?: 0)
    val tierColor = tier.palette().strong

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: rating + mini tier bar (36dp wide)
        Row(
            modifier = Modifier.width(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.rating?.toString() ?: "—",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = tierColor,
                maxLines = 1,
            )
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(18.dp)
                    .background(tierColor.copy(alpha = 0.6f), RoundedCornerShape(1.dp)),
            )
        }

        Spacer(Modifier.width(10.dp))

        // Middle: title + meta
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.lastVerdict} · ${item.attempts} attempts · ${relativeTime(item.lastAttemptSeconds)}",
                style = MaterialTheme.typography.labelSmall,
                color = extras.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Right: retry arrow
        IconButton(onClick = onRetry, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Retry",
                tint = extras.accentVioletSoft,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FailedProblemRowPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            FailedProblemRow(
                item = FailedProblem(
                    contestId = 1978,
                    problemIndex = "C",
                    name = "Adjacent XOR",
                    rating = 1600,
                    tags = listOf("dp", "greedy"),
                    lastVerdict = "WRONG_ANSWER",
                    attempts = 3,
                    lastAttemptSeconds = java.time.Instant.now().epochSecond - 3600 * 26,
                ),
                onRetry = {},
            )
        }
    }
}
