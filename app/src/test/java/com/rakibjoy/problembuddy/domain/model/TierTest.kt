package com.rakibjoy.problembuddy.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TierTest {

    @ParameterizedTest(name = "rating {0} -> {1}")
    @CsvSource(
        "-100, NEWBIE",
        "0, NEWBIE",
        "1199, NEWBIE",
        "1200, PUPIL",
        "1399, PUPIL",
        "1400, SPECIALIST",
        "1599, SPECIALIST",
        "1600, EXPERT",
        "1899, EXPERT",
        "1900, CANDIDATE_MASTER",
        "2099, CANDIDATE_MASTER",
        "2100, MASTER",
        "2136, MASTER",
        "2299, MASTER",
        "2300, INTL_MASTER",
        "2399, INTL_MASTER",
        "2400, GRANDMASTER",
        "2599, GRANDMASTER",
        "2600, INTL_GRANDMASTER",
        "2999, INTL_GRANDMASTER",
        "3000, LEGENDARY",
        "3500, LEGENDARY"
    )
    fun `forMaxRating returns expected tier for boundary`(rating: Int, expected: Tier) {
        assertEquals(expected, Tier.forMaxRating(rating))
    }
}
