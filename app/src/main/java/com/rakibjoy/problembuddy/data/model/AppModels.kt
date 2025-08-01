package com.rakibjoy.problembuddy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// App-specific models for recommendation system
data class RecommendedProblem(
    val contestId: Int,
    val index: String,
    val rating: Int,
    val tags: List<String>,
    val similarity: Double = 0.0
)

data class UserProfile(
    val handle: String,
    val maxRating: Int,
    val maxRank: String,
    val currentRating: Int,
    val targetRating: Int,
    val photoUrl: String
)

data class WeakArea(
    val tag: String,
    val percentage: Int,
    val expectedCount: Int,
    val actualCount: Int
)

data class SkillLevel(
    val current: Int,
    val target: Int,
    val levelName: String
)

// Room Database Entities
@Entity(tableName = "local_problems")
data class LocalProblem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val contestId: Int,
    val index: String,
    val rating: Int,
    val tags: String, // Comma-separated tags
    val skillLevel: String // "pupil", "specialist", "expert", "candidate_master", "master"
)

@Entity(tableName = "local_submissions")
data class LocalSubmission(
    @PrimaryKey
    val id: Long,
    val contestId: Int,
    val index: String,
    val rating: Int,
    val tags: String,
    val submissionTime: Long,
    val verdict: String,
    val handle: String
)

@Entity(tableName = "local_users")
data class LocalUser(
    @PrimaryKey
    val handle: String,
    val maxRating: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

// UI State Models
data class RecommendationState(
    val isLoading: Boolean = false,
    val problems: List<RecommendedProblem> = emptyList(),
    val error: String? = null
)

data class ProfileState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val weakAreas: List<WeakArea> = emptyList(),
    val error: String? = null
)

data class AuthState(
    val isLoggedIn: Boolean = false,
    val handle: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Rating levels for targeting
enum class RatingLevel(val minRating: Int, val targetRating: Int, val displayName: String) {
    BEGINNER(0, 1200, "Pupil"),
    PUPIL(1200, 1400, "Specialist"),
    SPECIALIST(1400, 1600, "Expert"),
    EXPERT(1600, 1900, "Candidate Master"),
    CANDIDATE_MASTER(1900, 2100, "Master"),
    MASTER(2100, 2500, "Grandmaster")
}

fun getRatingLevel(currentRating: Int): RatingLevel {
    return when {
        currentRating >= 2100 -> RatingLevel.MASTER
        currentRating >= 1900 -> RatingLevel.CANDIDATE_MASTER
        currentRating >= 1600 -> RatingLevel.EXPERT
        currentRating >= 1400 -> RatingLevel.SPECIALIST
        currentRating >= 1200 -> RatingLevel.PUPIL
        else -> RatingLevel.BEGINNER
    }
}