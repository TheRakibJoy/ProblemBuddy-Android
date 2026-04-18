package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier

@Composable
fun NextTierProgress(
    currentTier: Tier,
    nextTier: Tier?,
    ratingToGo: Int?,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(10.dp)
    val palette = currentTier.palette()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (nextTier == null || ratingToGo == null) {
            Text(
                text = "peak tier reached ✓",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = extras.accentVioletSoft,
            )
        } else {
            Text(
                text = "$ratingToGo to ${nextTier.label}",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.weight(1f))
        val barShape = RoundedCornerShape(2.dp)
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .clip(barShape)
                .background(extras.surfaceRaised, barShape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(palette.strong, barShape),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NextTierProgressPreview() {
    ProblemBuddyTheme {
        NextTierProgress(
            currentTier = Tier.CANDIDATE_MASTER,
            nextTier = Tier.MASTER,
            ratingToGo = 164,
            progress = 0.35f,
            modifier = Modifier.padding(12.dp),
        )
    }
}
