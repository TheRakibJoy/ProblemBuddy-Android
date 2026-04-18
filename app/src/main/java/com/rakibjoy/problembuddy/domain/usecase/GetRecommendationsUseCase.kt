package com.rakibjoy.problembuddy.domain.usecase

import com.rakibjoy.problembuddy.core.database.dao.InteractionDao
import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.core.database.entity.ProblemEntity
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.data.mapper.toDomain
import com.rakibjoy.problembuddy.data.recommender.TierIndex
import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.RecommendationsResult
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetRecommendationsUseCase @Inject constructor(
    private val codeforces: CodeforcesRepository,
    private val computeWeakTags: ComputeWeakTagsUseCase,
    private val problemDao: ProblemDao,
    private val interactionDao: InteractionDao,
    private val settingsStore: SettingsStore,
) {
    suspend operator fun invoke(filters: Filters): Result<RecommendationsResult> {
        val handle = settingsStore.cfHandle.first()
        if (handle.isNullOrBlank()) {
            return Result.failure(IllegalStateException("No handle set"))
        }

        val userInfoResult = codeforces.userInfoWithFallback(handle)
        val userInfoFresh = userInfoResult.getOrElse { return Result.failure(it) }
        val userInfo = userInfoFresh.value
        val tier = Tier.forMaxRating(userInfo.maxRating ?: userInfo.rating ?: 0)

        // "Difficulty offset" setting shifts the default recommendation window
        // around the user's *current* rating. Explicit filter bounds override it.
        val difficultyOffset = settingsStore.difficultyOffset.first()
        val baselineRating = userInfo.rating ?: userInfo.maxRating
        val effectiveMinRating = filters.minRating
            ?: baselineRating?.let { it + difficultyOffset - RATING_WINDOW_BELOW }
        val effectiveMaxRating = filters.maxRating
            ?: baselineRating?.let { it + difficultyOffset + RATING_WINDOW_ABOVE }

        val statusResult = codeforces.userStatusWithFallback(handle, 1, 100_000)
        val statusFresh = statusResult.getOrElse { return Result.failure(it) }
        val submissions = statusFresh.value
        val stale = userInfoFresh.stale || statusFresh.stale

        val accepted = submissions.filter { it.verdict == "OK" }

        val solvedKeys: Set<Pair<Int, String>> = accepted
            .asSequence()
            .filter { it.problem.contestId != 0 && it.problem.problemIndex.isNotBlank() }
            .map { it.problem.contestId to it.problem.problemIndex }
            .toSet()

        val perTagSolved: Map<String, Int> = buildMap {
            for (sub in accepted) {
                val rating = sub.problem.rating ?: continue
                if (Tier.forMaxRating(rating) != tier) continue
                for (tag in sub.problem.tags) {
                    if (tag.isBlank()) continue
                    put(tag, (get(tag) ?: 0) + 1)
                }
            }
        }

        val excludedProblemIds: Set<Long> = interactionDao.observeAll().first()
            .asSequence()
            .filter { it.status == "solved" || it.status == "not_interested" || it.status == "hidden" }
            .map { it.problemId }
            .toSet()

        val weakTags = computeWeakTags(tier, perTagSolved, topN = 10, minCorpusCount = 5)

        val entities: List<ProblemEntity> = withContext(Dispatchers.IO) {
            problemDao.observeByTier(tier.name.lowercase()).first()
        }
        val problems: List<Problem> = entities.map { it.toDomain() }

        val tierIndex = TierIndex.build(problems)
        val rankedIndices = tierIndex.rank(weakTags).ifEmpty { problems.indices.toList() }

        val filtered = rankedIndices.asSequence()
            .map { idx -> entities[idx] to problems[idx] }
            .filter { (_, p) -> (p.contestId to p.problemIndex) !in solvedKeys }
            .filter { (e, _) -> e.id !in excludedProblemIds }
            .filter { (_, p) ->
                val rating = p.rating
                if (effectiveMinRating != null && (rating == null || rating < effectiveMinRating)) return@filter false
                if (effectiveMaxRating != null && (rating == null || rating > effectiveMaxRating)) return@filter false
                true
            }
            .filter { (_, p) ->
                if (filters.includeTags.isEmpty()) true
                else p.tags.any { it in filters.includeTags }
            }
            .filter { (_, p) -> p.tags.none { it in filters.excludeTags } }
            .filter { (_, p) ->
                if (!filters.weakOnly || weakTags.isEmpty()) true
                else p.tags.any { it in weakTags }
            }
            .map { it.second }
            .take(filters.count)
            .toList()

        return Result.success(RecommendationsResult(problems = filtered, stale = stale))
    }

    private companion object {
        // "Slightly below" → 100 under current rating. "Slightly above" → 200.
        // These match the classic CP practice advice to work in a narrow window
        // just above the user's own rating.
        const val RATING_WINDOW_BELOW = 100
        const val RATING_WINDOW_ABOVE = 200
    }
}
