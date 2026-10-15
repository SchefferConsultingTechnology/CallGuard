package com.lsp.callguard.ui.navigation

import com.lsp.callguard.domain.model.OnboardingProgress
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mirrors the resolveStartDestination table in specs/002-app-launch-flow/contracts. */
class StartDestinationTest {

    private val none = LegacySignals(hasSavedLanguage = false, hasWhitelistEntries = false, isLicensed = false)
    private val all = LegacySignals(hasSavedLanguage = true, hasWhitelistEntries = true, isLicensed = true)

    private fun assertResolution(
        progress: OnboardingProgress?,
        legacy: LegacySignals,
        route: String,
        toPersist: OnboardingProgress?
    ) {
        assertEquals(StartResolution(route, toPersist), resolveStartDestination(progress, legacy))
    }

    @Test
    fun `completed opens home`() {
        assertResolution(OnboardingProgress.COMPLETED, none, Routes.Home.route, null)
        assertResolution(OnboardingProgress.COMPLETED, all, Routes.Home.route, null)
    }

    @Test
    fun `onboarding done resumes on the first-run offer`() {
        assertResolution(OnboardingProgress.ONBOARDING_DONE, all, Routes.Paywall.create(firstRun = true), null)
    }

    @Test
    fun `not started opens onboarding even with a saved language`() {
        assertResolution(OnboardingProgress.NOT_STARTED, all, Routes.Onboarding.route, null)
    }

    @Test
    fun `fresh install opens onboarding and persists NOT_STARTED`() {
        assertResolution(null, none, Routes.Onboarding.route, OnboardingProgress.NOT_STARTED)
    }

    @Test
    fun `previous version with any usage signal opens home and persists COMPLETED`() {
        assertResolution(null, none.copy(hasSavedLanguage = true), Routes.Home.route, OnboardingProgress.COMPLETED)
        assertResolution(null, none.copy(hasWhitelistEntries = true), Routes.Home.route, OnboardingProgress.COMPLETED)
        assertResolution(null, none.copy(isLicensed = true), Routes.Home.route, OnboardingProgress.COMPLETED)
    }

    // Legacy reading: one failing source must not hide the others

    private val boom: suspend () -> Boolean = { throw IllegalStateException("db unreadable") }

    @Test
    fun `failing database keeps the saved language signal`() = runTest {
        val read = readLegacySignals(
            hasSavedLanguage = { true },
            hasWhitelistEntries = boom,
            isLicensed = { false }
        )

        assertEquals(LegacySignals(true, false, false), read.signals)
        assertFalse(read.isComplete)
        assertEquals(Routes.Home.route, resolveStartDestination(null, read.signals).route)
        assertTrue(shouldPersistDerivation(progressWasRead = false, legacy = read))
    }

    @Test
    fun `partial read without usage is not persisted so it is retried`() = runTest {
        val read = readLegacySignals(
            hasSavedLanguage = { false },
            hasWhitelistEntries = boom,
            isLicensed = { false }
        )

        assertEquals(Routes.Onboarding.route, resolveStartDestination(null, read.signals).route)
        assertFalse(shouldPersistDerivation(progressWasRead = false, legacy = read))
    }

    @Test
    fun `complete read is persisted`() = runTest {
        val read = readLegacySignals({ false }, { false }, { false })

        assertTrue(read.isComplete)
        assertTrue(shouldPersistDerivation(progressWasRead = false, legacy = read))
    }
}
