package com.rakibjoy.problembuddy.data.repository

import com.rakibjoy.problembuddy.core.database.dao.ReviewDao
import com.rakibjoy.problembuddy.core.database.entity.ReviewEntity
import com.rakibjoy.problembuddy.domain.model.Review
import com.rakibjoy.problembuddy.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val dao: ReviewDao,
) : ReviewRepository {

    override fun observeDue(nowSeconds: Long): Flow<List<Review>> =
        dao.observeDue(nowSeconds).map { list -> list.map { it.toDomain() } }

    override suspend fun scheduleInitial(
        problemId: Long,
        contestId: Int,
        problemIndex: String,
    ) {
        val existing = dao.getForProblem(problemId)
        if (existing != null) return
        val nowSeconds = System.currentTimeMillis() / 1000L
        dao.upsert(
            ReviewEntity(
                problemId = problemId,
                contestId = contestId,
                problemIndex = problemIndex,
                box = 0,
                lastReviewAt = nowSeconds,
                nextReviewAt = nowSeconds + BOX_INTERVAL_SECONDS[0],
                lastOutcome = "initial",
            ),
        )
    }

    override suspend fun markCorrect(review: Review) {
        val newBox = (review.box + 1).coerceAtMost(5)
        val nowSeconds = System.currentTimeMillis() / 1000L
        dao.upsert(
            ReviewEntity(
                id = review.id,
                problemId = review.problemId,
                contestId = review.contestId,
                problemIndex = review.problemIndex,
                box = newBox,
                lastReviewAt = nowSeconds,
                nextReviewAt = nowSeconds + BOX_INTERVAL_SECONDS[newBox],
                lastOutcome = "correct",
            ),
        )
    }

    override suspend fun markMissed(review: Review) {
        val nowSeconds = System.currentTimeMillis() / 1000L
        dao.upsert(
            ReviewEntity(
                id = review.id,
                problemId = review.problemId,
                contestId = review.contestId,
                problemIndex = review.problemIndex,
                box = 0,
                lastReviewAt = nowSeconds,
                nextReviewAt = nowSeconds + BOX_INTERVAL_SECONDS[0],
                lastOutcome = "missed",
            ),
        )
    }

    override suspend fun clearAll() {
        dao.deleteAll()
    }

    private fun ReviewEntity.toDomain(): Review = Review(
        id = id,
        problemId = problemId,
        contestId = contestId,
        problemIndex = problemIndex,
        box = box,
        lastReviewAt = lastReviewAt,
        nextReviewAt = nextReviewAt,
        lastOutcome = when (lastOutcome) {
            "correct" -> Review.Outcome.CORRECT
            "missed" -> Review.Outcome.MISSED
            else -> Review.Outcome.INITIAL
        },
    )

    companion object {
        private const val DAY_SECONDS = 86_400L
        // Box 0..5 intervals in days: 1, 3, 7, 14, 30, 90.
        private val BOX_INTERVAL_SECONDS = longArrayOf(
            1L * DAY_SECONDS,
            3L * DAY_SECONDS,
            7L * DAY_SECONDS,
            14L * DAY_SECONDS,
            30L * DAY_SECONDS,
            90L * DAY_SECONDS,
        )
    }
}
