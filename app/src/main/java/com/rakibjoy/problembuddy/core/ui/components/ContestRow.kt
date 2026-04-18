package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.feature.profile.ContestResult

@Composable
fun ContestRow(
    contest: ContestResult,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val tier = Tier.forMaxRating(contest.newRating)
    val delta = contest.newRating - contest.oldRating
    val deltaColor = if (delta >= 0) extras.deltaPositive else extras.deltaNegative
    val deltaText = if (delta >= 0) "+$delta" else "$delta"

    Row(
        modifier = modifier.fillMaxWidth().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(tier.palette().strong, CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = contest.name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "rank #${contest.rank} · ${contest.division ?: ""}",
                style = MaterialTheme.typography.labelSmall,
                color = extras.textTertiary,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .background(deltaColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = deltaText,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = deltaColor,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContestRowPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(8.dp)) {
            Column {
                ContestRow(ContestResult(1, "Codeforces Round 920", 1423, 1580, 1622, 0L, "Div. 3"))
                ContestRow(ContestResult(2, "Educational Round 150", 3002, 1622, 1612, 0L, "Div. 2"))
            }
        }
    }
}
