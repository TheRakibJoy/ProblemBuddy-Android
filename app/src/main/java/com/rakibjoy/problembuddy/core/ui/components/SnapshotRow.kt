package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
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
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.feature.profile.ProfileSnapshot

@Composable
fun SnapshotRow(
    oneYearAgo: ProfileSnapshot,
    today: ProfileSnapshot,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = AppShapes.small

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SnapshotColumn(title = "A year ago", snapshot = oneYearAgo, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = extras.textTertiary,
            modifier = Modifier.size(18.dp).padding(horizontal = 2.dp),
        )
        Spacer(Modifier.width(8.dp))
        SnapshotColumn(title = "Today", snapshot = today, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SnapshotColumn(
    title: String,
    snapshot: ProfileSnapshot,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val tierColor = snapshot.tier?.palette()?.strong ?: extras.textSecondary
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
        Text(
            text = snapshot.rating?.toString() ?: "—",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = tierColor,
        )
        Text(
            text = "${snapshot.solvedCount} solved",
            style = MaterialTheme.typography.labelSmall,
            color = extras.textSecondary,
        )
        Text(
            text = snapshot.tier?.label ?: "—",
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SnapshotRowPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            SnapshotRow(
                oneYearAgo = ProfileSnapshot(0L, 1320, 210, Tier.PUPIL),
                today = ProfileSnapshot(0L, 1620, 512, Tier.EXPERT),
            )
        }
    }
}
