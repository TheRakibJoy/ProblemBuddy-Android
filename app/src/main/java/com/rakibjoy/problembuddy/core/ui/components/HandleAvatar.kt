package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.gradient
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier

/**
 * Circular avatar that loads the Codeforces `titlePhoto`/`avatar` if present,
 * otherwise falls back to a tier-gradient disc with the handle's first letter.
 */
@Composable
fun HandleAvatar(
    handle: String?,
    avatarUrl: String?,
    tier: Tier?,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier,
) {
    val initial = handle?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val fallbackBrush: Brush = tier?.gradient()
        ?: Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
    // Codeforces convention: Legendary Grandmaster handles render their first
    // character in black. Honor it here for the single-letter avatar fallback.
    val fallbackTextColor = when {
        tier == Tier.LEGENDARY -> Color.Black
        else -> tier?.palette()?.onColor ?: Color.White
    }

    var loadFailed by remember(avatarUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fallbackBrush),
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
            Text(
                text = initial,
                color = fallbackTextColor,
                fontWeight = FontWeight.Bold,
                style = when {
                    size >= 72.dp -> MaterialTheme.typography.displaySmall
                    size >= 48.dp -> MaterialTheme.typography.headlineSmall
                    else -> MaterialTheme.typography.titleMedium
                },
            )
        }
    }
}

@Preview
@Composable
private fun HandleAvatarFallbackPreview() {
    ProblemBuddyTheme {
        HandleAvatar(handle = "tourist", avatarUrl = null, tier = Tier.LEGENDARY, size = 80.dp)
    }
}
