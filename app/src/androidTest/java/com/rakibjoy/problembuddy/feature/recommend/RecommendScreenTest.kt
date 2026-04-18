package com.rakibjoy.problembuddy.feature.recommend

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakibjoy.problembuddy.domain.model.Problem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecommendScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun fixtureProblem(
        id: Int,
        rating: Int,
        tags: List<String>,
        name: String = "Problem $id",
    ): Problem = Problem(
        contestId = id,
        problemIndex = "A",
        name = name,
        rating = rating,
        tags = tags,
    )

    @Test
    fun renders_skeletons_when_loading() {
        composeRule.setContent {
            RecommendScreen(
                state = RecommendState(loading = true),
                onIntent = {},
            )
        }

        val nodes = composeRule.onAllNodesWithTag("skeleton-card").fetchSemanticsNodes()
        assertTrue("Expected at least one skeleton card", nodes.isNotEmpty())
    }

    @Test
    fun renders_problem_cards_when_loaded() {
        val problems = listOf(
            fixtureProblem(1, 800, listOf("dp")),
            fixtureProblem(2, 1000, listOf("greedy")),
            fixtureProblem(3, 1200, listOf("math")),
        )
        composeRule.setContent {
            RecommendScreen(
                state = RecommendState(loading = false, problems = problems),
                onIntent = {},
            )
        }

        composeRule.onNodeWithText("Problem 1", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Problem 2", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Problem 3", substring = true).assertIsDisplayed()
    }

    @Test
    fun filter_icon_emits_open_filters_intent() {
        val received = mutableListOf<RecommendIntent>()
        composeRule.setContent {
            RecommendScreen(
                state = RecommendState(loading = false, problems = emptyList()),
                onIntent = { received += it },
            )
        }

        composeRule.onNodeWithContentDescription("Filters").performClick()

        assertTrue(
            "Expected OpenFilters intent, got $received",
            received.contains(RecommendIntent.OpenFilters),
        )
    }

    @Test
    fun skip_button_emits_intent() {
        val problem = fixtureProblem(42, 1500, listOf("dp", "trees"))
        val received = mutableListOf<RecommendIntent>()
        composeRule.setContent {
            RecommendScreen(
                state = RecommendState(loading = false, problems = listOf(problem)),
                onIntent = { received += it },
            )
        }

        // The card's right-side action is a Skip icon button with
        // contentDescription "Skip". Left-swipe triggers the same intent;
        // testing the button click is simpler and covers the same path.
        composeRule.onNodeWithContentDescription("Skip").performClick()

        val skipped = received.filterIsInstance<RecommendIntent.Skip>()
        assertEquals(1, skipped.size)
        assertEquals(problem, skipped.first().problem)
    }
}
