package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Tier

/**
 * "Problem of the day" card that sits at the top of Home.
 *
 * Layout mirrors the redesign mock: rating rail on the left, title + meta +
 * tags in the middle column, a full-width primary CTA at the bottom.
 */
@Composable
fun DailyProblemCard(
    problem: Problem,
    onSolve: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(16.dp)
    val tier = Tier.forMaxRating(problem.rating ?: 0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.accentVioletSoft, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "TODAY'S PROBLEM",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = extras.textTertiary,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RatingRail(rating = problem.rating ?: 0, tier = tier)

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = problem.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${problem.contestId} · ${problem.problemIndex}",
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textTertiary,
                )
                if (problem.tags.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        problem.tags.take(4).forEach { tag ->
                            TagChip(tag = tag)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onSolve,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            Text(
                text = "solve today →",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyProblemCardPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            DailyProblemCard(
                problem = Problem(
                    contestId = 1854,
                    problemIndex = "G",
                    name = "Segment Tree Beats",
                    rating = 2100,
                    tags = listOf("data structures", "segment tree", "dp", "greedy"),
                ),
                onSolve = {},
            )
        }
    }
}
