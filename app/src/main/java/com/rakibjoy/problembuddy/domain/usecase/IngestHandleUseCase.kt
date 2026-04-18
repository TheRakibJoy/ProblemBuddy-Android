package com.rakibjoy.problembuddy.domain.usecase

import com.rakibjoy.problembuddy.domain.model.IngestProgress
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.TagCounter
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import com.rakibjoy.problembuddy.domain.repository.CounterRepository
import com.rakibjoy.problembuddy.domain.repository.HandleRepository
import com.rakibjoy.problembuddy.domain.repository.ProblemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IngestHandleUseCase @Inject constructor(
    private val codeforces: CodeforcesRepository,
    private val problemRepository: ProblemRepository,
    private val counterRepository: CounterRepository,
    private val handleRepository: HandleRepository,
) {
    operator fun invoke(handles: List<String>): Flow<IngestProgress> = flow {
        if (handles.isEmpty()) return@flow

        for (handle in handles) {
            emit(
                IngestProgress(
                    handle = handle,
                    tier = null,
                    done = 0,
                    total = 0,
                    phase = IngestProgress.Phase.FETCHING_SUBMISSIONS,
                ),
            )

            val result = codeforces.userStatus(handle, from = 1, count = 100_000)
            val submissions = result.getOrNull()
            if (submissions == null) {
                emit(
                    IngestProgress(
                        handle = handle,
                        tier = null,
                        done = 0,
                        total = 0,
                        phase = IngestProgress.Phase.FAILED,
                    ),
                )
                continue
            }

            val accepted = submissions
                .asSequence()
                .filter { it.verdict == "OK" }
                .filter { it.problem.contestId != 0 }
                .filter { it.problem.problemIndex.isNotBlank() }
                .filter { it.problem.rating != null }
                .toList()

            val deduped: List<Submission> = accepted
                .associateBy { it.problem.contestId to it.problem.problemIndex }
                .values
                .toList()

            withContext(Dispatchers.IO) {
                handleRepository.insert(handle)
            }

            val byTier: Map<Tier, List<Submission>> = deduped.groupBy { sub ->
                Tier.forMaxRating(sub.problem.rating!!)
            }

            for ((tier, group) in byTier) {
                val total = group.size
                emit(
                    IngestProgress(
                        handle = handle,
                        tier = tier,
                        done = 0,
                        total = total,
                        phase = IngestProgress.Phase.WRITING_CORPUS,
                    ),
                )

                val problems: List<Problem> = group.map { sub ->
                    Problem(
                        contestId = sub.problem.contestId,
                        problemIndex = sub.problem.problemIndex,
                        name = sub.problem.name,
                        rating = sub.problem.rating,
                        tags = sub.problem.tags,
                    )
                }

                // Count tag increments for this group.
                val increments: Map<String, Int> = buildMap {
                    for (sub in group) {
                        for (tag in sub.problem.tags) {
                            if (tag.isBlank()) continue
                            put(tag, (get(tag) ?: 0) + 1)
                        }
                    }
                }

                withContext(Dispatchers.IO) {
                    problemRepository.insertAll(problems)

                    if (increments.isNotEmpty()) {
                        val current = counterRepository.getByTier(tier)
                        val byTag = current.associateBy { it.tagName }
                        val merged = increments.map { (tag, delta) ->
                            val existing = byTag[tag]
                            if (existing != null) {
                                existing.copy(count = existing.count + delta)
                            } else {
                                TagCounter(
                                    tagName = tag,
                                    tier = tier,
                                    count = delta,
                                )
                            }
                        }
                        counterRepository.upsertAll(merged)
                    }
                }

                emit(
                    IngestProgress(
                        handle = handle,
                        tier = tier,
                        done = total,
                        total = total,
                        phase = IngestProgress.Phase.WRITING_CORPUS,
                    ),
                )
            }
        }

        emit(
            IngestProgress(
                handle = handles.last(),
                tier = null,
                done = 0,
                total = 0,
                phase = IngestProgress.Phase.COMPLETED,
            ),
        )
    }
}
