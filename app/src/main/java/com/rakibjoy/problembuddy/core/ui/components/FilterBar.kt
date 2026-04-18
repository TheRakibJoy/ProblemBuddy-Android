package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

/** Per-chip selection state in the [FilterBar]. */
enum class ChipState { Neutral, Include, Exclude }

data class FilterChipSpec(
    val label: String,
    val state: ChipState = ChipState.Neutral,
)

/**
 * Horizontally scrolling filter row. A cyan-bordered rating-range pill sits
 * on the left (if a range is provided); chips reuse [TagChip] with
 * include/exclude visual variants.
 */
@Composable
fun FilterBar(
    ratingRange: IntRange?,
    chips: List<FilterChipSpec>,
    onChipToggled: (String) -> Unit,
    onRangeClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Row(
        modifier = modifier
            .horizontalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (ratingRange != null) {
            RatingRangePill(
                text = "${ratingRange.first}–${ratingRange.last}",
                onClick = onRangeClicked,
            )
        }
        chips.forEach { spec ->
            TagChip(
                tag = spec.label,
                selected = spec.state == ChipState.Include,
                excluded = spec.state == ChipState.Exclude,
                onClick = { onChipToggled(spec.label) },
            )
        }
    }
}

@Composable
private fun RatingRangePill(text: String, onClick: () -> Unit) {
    val cyan = MaterialTheme.appExtras.accentCyanSoft
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(Color.Transparent, shape)
            .border(0.5.dp, cyan, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = cyan,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterBarPreview() {
    ProblemBuddyTheme {
        Surface {
            FilterBar(
                ratingRange = 1800..2200,
                chips = listOf(
                    FilterChipSpec("weak only", ChipState.Include),
                    FilterChipSpec("data structures", ChipState.Include),
                    FilterChipSpec("greedy", ChipState.Exclude),
                    FilterChipSpec("graphs"),
                    FilterChipSpec("dp"),
                ),
                onChipToggled = {},
                onRangeClicked = {},
            )
        }
    }
}
