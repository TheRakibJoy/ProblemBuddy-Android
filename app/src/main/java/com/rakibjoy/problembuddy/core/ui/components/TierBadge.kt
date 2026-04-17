package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier

@Composable
fun TierBadge(
    tier: Tier,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val palette = tier.palette()
    val text = if (compact) tier.abbreviation() else tier.label
    val style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
    val hPad: Dp = if (compact) 10.dp else 12.dp
    val vPad: Dp = if (compact) 4.dp else 6.dp
    Surface(
        modifier = modifier,
        color = palette.strong,
        contentColor = palette.onColor,
        shape = AppShapes.small,
    ) {
        Row(
            modifier = Modifier
                .background(palette.strong)
                .padding(horizontal = hPad, vertical = vPad),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = text,
                style = style.copy(fontWeight = FontWeight.Medium),
                color = palette.onColor,
            )
        }
    }
}

private fun Tier.abbreviation(): String =
    label.split(' ').filter { it.isNotBlank() }.joinToString("") { it.first().uppercase() }

@Preview(name = "TierBadge - Full", showBackground = true)
@Composable
private fun TierBadgeFullPreview() {
    ProblemBuddyTheme {
        androidx.compose.material3.Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TierBadge(Tier.PUPIL)
                TierBadge(Tier.EXPERT)
                TierBadge(Tier.LEGENDARY)
            }
        }
    }
}

@Preview(name = "TierBadge - Compact", showBackground = true)
@Composable
private fun TierBadgeCompactPreview() {
    ProblemBuddyTheme {
        androidx.compose.material3.Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TierBadge(Tier.INTL_MASTER, compact = true)
                TierBadge(Tier.GRANDMASTER, compact = true)
                TierBadge(Tier.LEGENDARY, compact = true)
            }
        }
    }
}
