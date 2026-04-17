package com.rakibjoy.problembuddy.domain.model

enum class Tier(val floor: Int, val target: Int, val label: String) {
    PUPIL(0, 1200, "Pupil"),
    SPECIALIST(1200, 1400, "Specialist"),
    EXPERT(1400, 1600, "Expert"),
    CANDIDATE_MASTER(1600, 1900, "Candidate Master"),
    MASTER(1900, 2100, "Master"),
    INTL_MASTER(2100, 2300, "International Master"),
    GRANDMASTER(2300, 2400, "Grandmaster"),
    INTL_GRANDMASTER(2400, 2600, "International Grandmaster"),
    LEGENDARY(2600, 3000, "Legendary Grandmaster");

    companion object {
        fun forMaxRating(rating: Int): Tier = entries.lastOrNull { rating >= it.floor } ?: PUPIL
    }
}
