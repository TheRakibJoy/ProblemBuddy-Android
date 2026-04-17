package com.rakibjoy.problembuddy.domain.model

sealed class CodeforcesException(message: String) : Exception(message) {
    class HandleNotFound(message: String = "Codeforces handle not found") :
        CodeforcesException(message)

    class CodeforcesUnavailable(message: String = "Codeforces is unavailable") :
        CodeforcesException(message)

    class RateLimited(message: String = "Rate limited by Codeforces") :
        CodeforcesException(message)
}
