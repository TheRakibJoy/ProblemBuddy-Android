package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier

/**
 * Circular avatar. Tier-tinted faint disc with the first two characters of the
 * handle (uppercase, monospace SemiBold) as fallback. Loads the user's
 * Codeforces titlePhoto when available; falls back on failure.
 *
 * Legendary handles render first char in black, second in CF-red — matching
 * the site's handle rendering convention.
 */
@Composable
fun HandleAvatar(
    handle: String?,
    avatarUrl: String?,
    tier: Tier?,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier,
) {
    val palette = tier?.palette()
    val bg = palette?.strong?.copy(alpha = 0.15f) ?: MaterialTheme.appExtras.surfaceElevated
    val borderColor = palette?.strong?.copy(alpha = 0.30f) ?: MaterialTheme.appExtras.borderSubtle
    var loadFailed by remember(avatarUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .border(1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!avatarUrl.isNullOrBlank() && !loadFailed) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = handle?.let { "$it avatar" },
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape),
                onState = { state ->
                    if (state is AsyncImagePainter.State.Error) loadFailed = true
                },
            )
        } else {
            // Fallback: first two chars (uppercase) or "?" if no handle.
            val fontSize = (size.value / 3f).sp
            val tertiary = MaterialTheme.appExtras.textTertiary
            if (handle.isNullOrBlank()) {
                Text(
                    text = "?",
                    color = tertiary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                )
            } else {
                val raw = handle.trim()
                val chars = raw.take(2).uppercase()
                val annotated = buildAnnotatedString {
                    if (tier == Tier.LEGENDARY) {
                        if (chars.isNotEmpty()) {
                            withStyle(SpanStyle(color = Color.Black)) {
                                append(chars[0].toString())
                            }
                        }
                        if (chars.length > 1) {
                            withStyle(SpanStyle(color = Color(0xFFFF0000))) {
                                append(chars[1].toString())
                            }
                        }
                    } else {
                        val color = palette?.strong ?: MaterialTheme.colorScheme.primary
                        withStyle(SpanStyle(color = color)) { append(chars) }
                    }
                }
                Text(
                    text = annotated,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                )
            }
        }
    }
}

@Preview
@Composable
private fun HandleAvatarLegendaryPreview() {
    ProblemBuddyTheme {
        HandleAvatar(handle = "tourist", avatarUrl = null, tier = Tier.LEGENDARY, size = 48.dp)
    }
}

@Preview
@Composable
private fun HandleAvatarMasterPreview() {
    ProblemBuddyTheme {
        HandleAvatar(handle = "rakibjoy", avatarUrl = null, tier = Tier.MASTER, size = 48.dp)
    }
}
