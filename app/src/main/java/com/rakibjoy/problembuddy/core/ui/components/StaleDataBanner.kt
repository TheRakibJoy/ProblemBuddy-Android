package com.rakibjoy.problembuddy.core.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing

@Composable
fun StaleDataBanner(
    modifier: Modifier = Modifier,
    fetchedAtMillis: Long? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = AppShapes.small,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(Spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Offline data",
                    style = MaterialTheme.typography.titleSmall,
                )
                val suffix = if (fetchedAtMillis != null) {
                    " from ${formatRelative(fetchedAtMillis)}"
                } else {
                    ""
                }
                Text(
                    text = "Showing cached results$suffix.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun formatRelative(fetchedAtMillis: Long): String {
    val now = System.currentTimeMillis()
    return DateUtils.getRelativeTimeSpanString(
        fetchedAtMillis,
        now,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}

@Preview(name = "StaleDataBanner", showBackground = true)
@Composable
private fun StaleDataBannerPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(Spacing.lg)) {
            StaleDataBanner(fetchedAtMillis = System.currentTimeMillis() - 3_600_000)
        }
    }
}
