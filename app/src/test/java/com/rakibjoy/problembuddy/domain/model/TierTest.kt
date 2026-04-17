package com.rakibjoy.problembuddy.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TierTest {

    @ParameterizedTest(name = "rating {0} -> {1}")
    @CsvSource(
        "-100, PUPIL",
        "0, PUPIL",
        "1199, PUPIL",
        "1200, SPECIALIST",
        "1399, SPECIALIST",
        "1400, EXPERT",
        "1599, EXPERT",
        "1600, CANDIDATE_MASTER",
        "1899, CANDIDATE_MASTER",
        "1900, MASTER",
        "2099, MASTER",
        "2100, INTL_MASTER",
        "2299, INTL_MASTER",
        "2300, GRANDMASTER",
        "2399, GRANDMASTER",
        "2400, INTL_GRANDMASTER",
        "2599, INTL_GRANDMASTER",
        "2600, LEGENDARY",
        "3500, LEGENDARY"
    )
    fun `forMaxRating returns expected tier for boundary`(rating: Int, expected: Tier) {
        assertEquals(expected, Tier.forMaxRating(rating))
    }
}
