package com.rakibjoy.problembuddy.domain.usecase

import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.data.recommender.TierIndex
import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Interaction
import com.rakibjoy.problembuddy.domain.model.RecommendationsResult
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import com.rakibjoy.problembuddy.domain.repository.InteractionRepository
import com.rakibjoy.problembuddy.domain.repository.ProblemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetRecommendationsUseCase @Inject constructor(
    private val codeforces: CodeforcesRepository,
    private val computeWeakTags: ComputeWeakTagsUseCase,
    private val problemRepository: ProblemRepository,
    private val interactionRepository: InteractionRepository,
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

        val excludedProblemIds: Set<Long> = interactionRepository.getAll()
            .asSequence()
            .filter {
                it.status == Interaction.Status.SOLVED ||
                    it.status == Interaction.Status.NOT_INTERESTED ||
                    it.status == Interaction.Status.HIDDEN
            }
            .map { it.problemId }
            .toSet()

        // Resolve interaction problemIds back to (contestId, problemIndex) pairs so
        // we can dedupe against Problem domain values (which no longer carry the row id).
        val excludedKeys: Set<Pair<Int, String>> = if (excludedProblemIds.isEmpty()) {
            emptySet()
        } else {
            problemRepository.resolveKeys(excludedProblemIds).values.toSet()
        }

        val weakTags = computeWeakTags(tier, perTagSolved, topN = 10, minCorpusCount = 5)

        val problems = withContext(Dispatchers.IO) {
            problemRepository.observeByTier(tier).first()
        }

        val tierIndex = TierIndex.build(problems)
        val rankedIndices = tierIndex.rank(weakTags).ifEmpty { problems.indices.toList() }

        val filtered = rankedIndices.asSequence()
            .map { idx -> problems[idx] }
            .filter { p -> (p.contestId to p.problemIndex) !in solvedKeys }
            .filter { p -> (p.contestId to p.problemIndex) !in excludedKeys }
            .filter { p ->
                val rating = p.rating
                if (effectiveMinRating != null && (rating == null || rating < effectiveMinRating)) return@filter false
                if (effectiveMaxRating != null && (rating == null || rating > effectiveMaxRating)) return@filter false
                true
            }
            .filter { p ->
                if (filters.includeTags.isEmpty()) true
                else p.tags.any { it in filters.includeTags }
            }
            .filter { p -> p.tags.none { it in filters.excludeTags } }
            .filter { p ->
                if (!filters.weakOnly || weakTags.isEmpty()) true
                else p.tags.any { it in weakTags }
            }
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
