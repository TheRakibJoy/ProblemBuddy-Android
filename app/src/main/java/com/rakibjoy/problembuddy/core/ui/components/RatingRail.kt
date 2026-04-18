package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier

/**
 * Left rail on a problem card: big tier-colored rating number + a short
 * tier-colored bar beneath.
 */
@Composable
fun RatingRail(
    rating: Int,
    tier: Tier,
    modifier: Modifier = Modifier,
) {
    val color = tier.palette().strong
    Column(
        modifier = modifier.width(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = rating.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 17.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.85).sp, // -0.05em @ 17sp
            ),
            color = color,
        )
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(28.dp)
                .background(color.copy(alpha = 0.5f), RoundedCornerShape(2.dp)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingRailPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            RatingRail(rating = 2100, tier = Tier.MASTER)
        }
    }
}
