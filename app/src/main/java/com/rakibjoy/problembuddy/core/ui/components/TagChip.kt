package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import kotlin.math.abs

/**
 * Derive a stable color from a tag string using the golden angle
 * (~137.508 degrees) so adjacent tag hashes land far apart in hue space.
 */
internal fun tagAccentColor(tag: String): Color {
    val goldenAngle = 137.508f
    val hue = (abs(tag.hashCode()) * goldenAngle) % 360f
    return Color.hsl(hue, saturation = 0.6f, lightness = 0.6f)
}

@Composable
fun TagChip(
    tag: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val clickableMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Surface(
        modifier = modifier.then(clickableMod),
        color = bg,
        contentColor = fg,
        shape = AppShapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Spacer(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = tagAccentColor(tag), shape = CircleShape),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall,
                color = fg,
            )
        }
    }
}

@Preview(name = "TagChip - Unselected", showBackground = true)
@Composable
private fun TagChipUnselectedPreview() {
    ProblemBuddyTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TagChip("dp")
                TagChip("graphs")
                TagChip("greedy")
            }
        }
    }
}

@Preview(name = "TagChip - Selected", showBackground = true)
@Composable
private fun TagChipSelectedPreview() {
    ProblemBuddyTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TagChip("dp", selected = true, onClick = {})
                TagChip("graphs", selected = true, onClick = {})
            }
        }
    }
}
