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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier

@Composable
fun ComparisonCard(
    myHandle: String,
    myRating: Int?,
    myTier: Tier?,
    theirHandle: String,
    theirRating: Int?,
    theirTier: Tier?,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = AppShapes.small

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(14.dp),
    ) {
        Text(
            text = "COMPARE",
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HandleColumn(
                handle = myHandle,
                rating = myRating,
                tier = myTier,
                modifier = Modifier.weight(1f),
            )
            HandleColumn(
                handle = theirHandle,
                rating = theirRating,
                tier = theirTier,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HandleColumn(
    handle: String,
    rating: Int?,
    tier: Tier?,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val color = tier?.palette()?.strong ?: extras.textSecondary
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = handle,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = color,
        )
        Text(
            text = rating?.toString() ?: "—",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = tier?.label ?: "—",
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ComparisonCardPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            ComparisonCard(
                myHandle = "rakib",
                myRating = 1522,
                myTier = Tier.SPECIALIST,
                theirHandle = "tourist",
                theirRating = 3800,
                theirTier = Tier.LEGENDARY,
            )
        }
    }
}
