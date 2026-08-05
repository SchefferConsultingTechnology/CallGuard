package com.lsp.callguard.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The app was never released, so there is no real schema history to migrate from.
 * Schema v2 is the first tracked baseline (see app/schemas). This test guards against
 * silent schema drift: any future version bump must ship a real Migration and an
 * exported schema, or this (and the app's database open call) will start failing
 * instead of silently wiping user data.
 */
@RunWith(AndroidJUnit4::class)
class CallGuardDatabaseMigrationTest {

    private val testDbName = "callguard-migration-test.db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CallGuardDatabase::class.java
    )

    @Test
    fun schemaV2CreatesSuccessfullyFromExportedSchema() {
        helper.createDatabase(testDbName, 2).close()
    }
}
