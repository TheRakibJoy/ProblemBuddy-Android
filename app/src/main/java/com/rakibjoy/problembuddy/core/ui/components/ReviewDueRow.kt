package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.domain.model.ThemeMode

@Composable
fun ReviewDueRow(
    problemLabel: String,
    box: Int,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(10.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BoxPill(box = box)

        Text(
            text = problemLabel,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "review →",
            style = MaterialTheme.typography.labelSmall,
            color = extras.accentVioletSoft,
            modifier = Modifier.clickable(onClick = onReview),
        )
    }
}

@Composable
private fun BoxPill(box: Int) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .background(extras.surfaceElevated, shape)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(
            text = "BOX $box",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
            ),
            color = extras.textTertiary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewDueRowPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            ReviewDueRow(
                problemLabel = "1854G",
                box = 2,
                onReview = {},
            )
        }
    }
}
