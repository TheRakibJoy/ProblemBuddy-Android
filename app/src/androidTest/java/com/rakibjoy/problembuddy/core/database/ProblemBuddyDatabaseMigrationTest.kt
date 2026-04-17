package com.rakibjoy.problembuddy.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProblemBuddyDatabaseMigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ProblemBuddyDatabase::class.java,
    )

    @Test
    fun migrate1To2_preservesExistingRows() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                "INSERT INTO problems(tier, contestId, problemIndex, rating, tags) " +
                    "VALUES('expert', 1, 'A', 1500, 'dp,graphs')",
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        migrated.query("SELECT contestId, problemIndex, tags FROM problems").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
            assertEquals("A", cursor.getString(1))
            assertEquals("dp,graphs", cursor.getString(2))
        }

        migrated.query("SELECT COUNT(*) FROM cached_payloads").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
    }
}
