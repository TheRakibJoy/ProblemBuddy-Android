package com.rakibjoy.problembuddy.domain.model

/**
 * Codeforces rating tiers. Bands match the current Codeforces rank system:
 * - Newbie: < 1200
 * - Pupil: 1200 – 1399
 * - Specialist: 1400 – 1599
 * - Expert: 1600 – 1899
 * - Candidate Master: 1900 – 2099
 * - Master: 2100 – 2299
 * - International Master: 2300 – 2399
 * - Grandmaster: 2400 – 2599
 * - International Grandmaster: 2600 – 2999
 * - Legendary Grandmaster: 3000+
 */
enum class Tier(val floor: Int, val target: Int, val label: String) {
    NEWBIE(0, 1200, "Newbie"),
    PUPIL(1200, 1400, "Pupil"),
    SPECIALIST(1400, 1600, "Specialist"),
    EXPERT(1600, 1900, "Expert"),
    CANDIDATE_MASTER(1900, 2100, "Candidate Master"),
    MASTER(2100, 2300, "Master"),
    INTL_MASTER(2300, 2400, "International Master"),
    GRANDMASTER(2400, 2600, "Grandmaster"),
    INTL_GRANDMASTER(2600, 3000, "International Grandmaster"),
    LEGENDARY(3000, 4000, "Legendary Grandmaster");

    companion object {
        fun forMaxRating(rating: Int): Tier = entries.lastOrNull { rating >= it.floor } ?: NEWBIE
    }
}
