package com.rakibjoy.problembuddy.data.repository

import com.rakibjoy.problembuddy.core.network.CodeforcesApi
import com.rakibjoy.problembuddy.core.network.dto.CfEnvelope
import com.rakibjoy.problembuddy.data.mapper.toDomain
import com.rakibjoy.problembuddy.domain.model.CodeforcesException
import com.rakibjoy.problembuddy.domain.model.RatingChange
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.UserInfo
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeforcesRepositoryImpl @Inject constructor(
    private val api: CodeforcesApi,
) : CodeforcesRepository {

    private data class CacheEntry<T>(val value: T, val expiresAt: Long)

    private val cache = ConcurrentHashMap<String, CacheEntry<*>>()

    private val defaultTtlMs: Long = 5 * 60 * 1000L

    @Suppress("UNCHECKED_CAST")
    private fun <T> getCached(key: String): T? {
        val entry = cache[key] ?: return null
        if (System.currentTimeMillis() > entry.expiresAt) {
            cache.remove(key)
            return null
        }
        return entry.value as T
    }

    private fun <T> putCached(key: String, value: T, ttlMs: Long = defaultTtlMs) {
        cache[key] = CacheEntry(value, System.currentTimeMillis() + ttlMs)
    }

    override suspend fun userInfo(handle: String): Result<UserInfo> =
        fetch("userInfo:$handle") {
            val env = api.userInfo(handle)
            val dto = env.resolveOk().firstOrNull()
                ?: throw CodeforcesException.HandleNotFound()
            dto.toDomain()
        }

    override suspend fun userRating(handle: String): Result<List<RatingChange>> =
        fetch("userRating:$handle") {
            api.userRating(handle).resolveOk().map { it.toDomain() }
        }

    override suspend fun userStatus(
        handle: String,
        from: Int,
        count: Int,
    ): Result<List<Submission>> =
        fetch("userStatus:$handle:$from:$count") {
            api.userStatus(handle, from, count).resolveOk().map { it.toDomain() }
        }

    private suspend fun <T> fetch(
        cacheKey: String,
        ttlMs: Long = defaultTtlMs,
        block: suspend () -> T,
    ): Result<T> {
        getCached<T>(cacheKey)?.let { return Result.success(it) }
        return withContext(Dispatchers.IO) {
            try {
                val value = block()
                putCached(cacheKey, value, ttlMs)
                Result.success(value)
            } catch (e: CodeforcesException) {
                Result.failure(e)
            } catch (e: HttpException) {
                val mapped = when (e.code()) {
                    429, 503 -> CodeforcesException.RateLimited()
                    else -> CodeforcesException.CodeforcesUnavailable(
                        e.message() ?: "HTTP ${e.code()}"
                    )
                }
                Result.failure(mapped)
            } catch (e: IOException) {
                Result.failure(CodeforcesException.CodeforcesUnavailable(e.message ?: "Network error"))
            } catch (e: Exception) {
                Result.failure(CodeforcesException.CodeforcesUnavailable(e.message ?: "Unknown error"))
            }
        }
    }

    private fun <T> CfEnvelope<T>.resolveOk(): T {
        if (status == "OK") {
            return result ?: throw CodeforcesException.CodeforcesUnavailable("Empty result")
        }
        val comment = comment.orEmpty()
        throw if (comment.contains("not found", ignoreCase = true)) {
            CodeforcesException.HandleNotFound(comment)
        } else {
            CodeforcesException.CodeforcesUnavailable(comment.ifEmpty { "Codeforces FAILED" })
        }
    }
}
