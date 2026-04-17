package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier

/**
 * Codeforces-faithful handle rendering.
 *
 * - Each rank uses its canonical CF color from [Tier.palette].
 * - **Legendary Grandmaster** handles are rendered with the first character in
 *   pure black, the remainder in red — a nod to the long-standing CF convention.
 *
 * Pass [baseColor] to override the tier-derived color (e.g. when the handle sits
 * on a saturated background and needs to use `onColor` instead). The Legendary
 * first-letter treatment is still applied on top of [baseColor].
 */
@Composable
fun HandleText(
    handle: String,
    tier: Tier,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight? = null,
    baseColor: Color? = null,
    maxLines: Int = 1,
) {
    val tint = baseColor ?: tier.palette().strong
    val annotated: AnnotatedString = remember(handle, tier, tint) {
        buildAnnotatedString {
            if (tier == Tier.LEGENDARY && handle.isNotEmpty()) {
                withStyle(SpanStyle(color = Color.Black)) {
                    append(handle.first().toString())
                }
                if (handle.length > 1) {
                    withStyle(SpanStyle(color = tint)) {
                        append(handle.substring(1))
                    }
                }
            } else {
                withStyle(SpanStyle(color = tint)) { append(handle) }
            }
        }
    }
    Text(
        text = annotated,
        style = if (fontWeight != null) style.copy(fontWeight = fontWeight) else style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Preview(name = "Legendary handle")
@Composable
private fun HandleTextLegendaryPreview() {
    ProblemBuddyTheme {
        HandleText(
            handle = "tourist",
            tier = Tier.LEGENDARY,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(name = "Master handle")
@Composable
private fun HandleTextMasterPreview() {
    ProblemBuddyTheme {
        HandleText(
            handle = "RakibJoy",
            tier = Tier.MASTER,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(name = "Newbie handle")
@Composable
private fun HandleTextNewbiePreview() {
    ProblemBuddyTheme {
        HandleText(
            handle = "newbie42",
            tier = Tier.NEWBIE,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
    }
}
