package com.rakibjoy.problembuddy.data.repository

import com.rakibjoy.problembuddy.data.local.ProblemBuddyDatabase
import com.rakibjoy.problembuddy.data.model.*
import com.rakibjoy.problembuddy.data.remote.CodeforcesApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeforcesRepository @Inject constructor(
    private val apiService: CodeforcesApiService,
    private val database: ProblemBuddyDatabase
) {

    // User Profile Operations
    suspend fun getUserProfile(handle: String): Result<UserProfile> {
        return try {
            val userInfo = apiService.getUserInfo(handle)
            if (userInfo.status == "OK" && userInfo.result.isNotEmpty()) {
                val user = userInfo.result[0]
                val skillLevel = getRatingLevel(user.maxRating)

                val profile = UserProfile(
                    handle = user.handle,
                    maxRating = user.maxRating,
                    maxRank = user.maxRank,
                    currentRating = skillLevel.minRating,
                    targetRating = skillLevel.targetRating,
                    photoUrl = user.titlePhoto
                )

                // Save to local database
                database.userDao().insertUser(
                    LocalUser(
                        handle = user.handle,
                        maxRating = user.maxRating
                    )
                )

                Result.success(profile)
            } else {
                Result.failure(Exception("Failed to fetch user info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Weak Areas Analysis
    suspend fun getWeakAreas(handle: String): Result<List<WeakArea>> {
        return try {
            val ratingResponse = apiService.getUserRating(handle)
            val statusResponse = apiService.getUserStatus(handle)

            if (ratingResponse.status == "OK" && statusResponse.status == "OK") {
                val skillLevel = getRatingLevel(ratingResponse.result.maxOfOrNull { it.newRating } ?: 0)
                val weakAreas = analyzeWeakAreas(statusResponse.result, skillLevel)
                Result.success(weakAreas)
            } else {
                Result.failure(Exception("Failed to fetch user data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Problem Recommendations
    suspend fun getRecommendedProblems(handle: String, weakTags: List<String>): Result<List<RecommendedProblem>> {
        return try {
            val userInfo = apiService.getUserInfo(handle)
            if (userInfo.status == "OK" && userInfo.result.isNotEmpty()) {
                val user = userInfo.result[0]
                val skillLevel = getRatingLevel(user.maxRating)

                // Get problems from local database
                val problems = database.problemDao().getProblemsBySkillLevel(skillLevel.name.lowercase())
                    .collect { localProblems ->
                        if (localProblems.isNotEmpty()) {
                            val recommendedProblems = recommendProblems(localProblems, weakTags, handle)
                            Result.success(recommendedProblems)
                        } else {
                            // Fallback to sample problems if database is empty
                            val fallbackProblems = getFallbackProblems(skillLevel, weakTags)
                            Result.success(fallbackProblems)
                        }
                    }

                // For now, return fallback problems
                val fallbackProblems = getFallbackProblems(skillLevel, weakTags)
                Result.success(fallbackProblems)
            } else {
                Result.failure(Exception("Failed to fetch user info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Local Database Operations
    fun getLocalProblems(skillLevel: String): Flow<List<LocalProblem>> {
        return database.problemDao().getProblemsBySkillLevel(skillLevel)
    }

    suspend fun saveProblems(problems: List<LocalProblem>) {
        database.problemDao().insertProblems(problems)
    }

    suspend fun saveSubmissions(submissions: List<LocalSubmission>) {
        database.submissionDao().insertSubmissions(submissions)
    }

    suspend fun isProblemSolved(contestId: Int, index: String): Boolean {
        return database.submissionDao().getSubmission(contestId, index) != null
    }

    // Helper Functions
    private fun analyzeWeakAreas(submissions: List<Submission>, skillLevel: RatingLevel): List<WeakArea> {
        val tagCounts = mutableMapOf<String, Int>()

        // Count solved problems by tag
        submissions.filter { it.verdict == "OK" && it.problem.rating != null && it.problem.rating > skillLevel.minRating }
            .forEach { submission ->
                submission.problem.tags.forEach { tag ->
                    tagCounts[tag] = tagCounts.getOrDefault(tag, 0) + 1
                }
            }

        // Calculate weak areas based on expected vs actual
        return tagCounts.map { (tag, count) ->
            val expectedCount = getExpectedCountForTag(tag, skillLevel)
            val percentage = if (expectedCount > 0) {
                ((expectedCount - count) * 100) / expectedCount
            } else 0

            WeakArea(
                tag = tag,
                percentage = percentage,
                expectedCount = expectedCount,
                actualCount = count
            )
        }.filter { it.percentage > 0 }
            .sortedByDescending { it.percentage }
    }

    private fun recommendProblems(
        problems: List<LocalProblem>,
        weakTags: List<String>,
        handle: String
    ): List<RecommendedProblem> {
        // Simple recommendation based on weak tags
        return problems.filter { problem ->
            val problemTags = problem.tags.split(",").map { it.trim() }
            weakTags.any { weakTag -> problemTags.contains(weakTag) }
        }.map { problem ->
            RecommendedProblem(
                contestId = problem.contestId,
                index = problem.index,
                rating = problem.rating,
                tags = problem.tags.split(",").map { it.trim() }
            )
        }.take(10) // Return top 10 recommendations
    }

    private fun getFallbackProblems(skillLevel: RatingLevel, weakTags: List<String>): List<RecommendedProblem> {
        // Return sample problems based on skill level
        val sampleProblems = when (skillLevel) {
            RatingLevel.BEGINNER, RatingLevel.PUPIL -> listOf(
                RecommendedProblem(1, "A", 1200, listOf("implementation", "math")),
                RecommendedProblem(1, "B", 1300, listOf("greedy", "implementation")),
                RecommendedProblem(2, "A", 1250, listOf("math", "implementation"))
            )
            RatingLevel.SPECIALIST -> listOf(
                RecommendedProblem(3, "A", 1400, listOf("greedy", "implementation")),
                RecommendedProblem(3, "B", 1500, listOf("dp", "implementation")),
                RecommendedProblem(4, "A", 1450, listOf("math", "greedy"))
            )
            RatingLevel.EXPERT -> listOf(
                RecommendedProblem(5, "A", 1600, listOf("dp", "greedy")),
                RecommendedProblem(5, "B", 1700, listOf("graphs", "implementation")),
                RecommendedProblem(6, "A", 1650, listOf("math", "dp"))
            )
            RatingLevel.CANDIDATE_MASTER -> listOf(
                RecommendedProblem(7, "A", 1900, listOf("graphs", "dp")),
                RecommendedProblem(7, "B", 2000, listOf("trees", "implementation")),
                RecommendedProblem(8, "A", 1950, listOf("math", "graphs"))
            )
            RatingLevel.MASTER -> listOf(
                RecommendedProblem(9, "A", 2100, listOf("advanced", "dp")),
                RecommendedProblem(9, "B", 2200, listOf("graphs", "advanced")),
                RecommendedProblem(10, "A", 2150, listOf("math", "advanced"))
            )
        }

        // Filter by weak tags if available
        return if (weakTags.isNotEmpty()) {
            sampleProblems.filter { problem ->
                weakTags.any { weakTag -> problem.tags.contains(weakTag) }
            }.take(3)
        } else {
            sampleProblems.take(3)
        }
    }

    private fun getExpectedCountForTag(tag: String, skillLevel: RatingLevel): Int {
        // This would be based on the training data from successful users
        // For now, return a simple heuristic
        return when (skillLevel) {
            RatingLevel.BEGINNER -> 5
            RatingLevel.PUPIL -> 8
            RatingLevel.SPECIALIST -> 12
            RatingLevel.EXPERT -> 15
            RatingLevel.CANDIDATE_MASTER -> 20
            RatingLevel.MASTER -> 25
        }
    }
}