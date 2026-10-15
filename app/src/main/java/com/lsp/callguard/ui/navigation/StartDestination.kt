package com.lsp.callguard.ui.navigation

import com.lsp.callguard.domain.model.OnboardingProgress
import kotlin.coroutines.cancellation.CancellationException

/** Usage left behind by versions that did not record [OnboardingProgress]. */
data class LegacySignals(
    val hasSavedLanguage: Boolean,
    val hasWhitelistEntries: Boolean,
    val isLicensed: Boolean
) {
    val hasAny: Boolean
        get() = hasSavedLanguage || hasWhitelistEntries || isLicensed
}

data class LegacyRead(
    val signals: LegacySignals,
    /** False when at least one source could not be read; the result may then be incomplete. */
    val isComplete: Boolean
)

/**
 * Reads each legacy source on its own, so one failing source (e.g. an unreadable database) does
 * not hide the others, such as a saved language.
 */
suspend fun readLegacySignals(
    hasSavedLanguage: suspend () -> Boolean,
    hasWhitelistEntries: suspend () -> Boolean,
    isLicensed: suspend () -> Boolean
): LegacyRead {
    var isComplete = true

    suspend fun read(source: suspend () -> Boolean): Boolean {
        return try {
            source()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            isComplete = false
            false
        }
    }

    return LegacyRead(
        signals = LegacySignals(
            hasSavedLanguage = read(hasSavedLanguage),
            hasWhitelistEntries = read(hasWhitelistEntries),
            isLicensed = read(isLicensed)
        ),
        isComplete = isComplete
    )
}

/**
 * Whether a derived progress can be saved: always when it comes from a stored progress or from a
 * complete read; after a partial read only when some usage was found (that conclusion cannot be
 * overturned by the sources that failed).
 */
fun shouldPersistDerivation(progressWasRead: Boolean, legacy: LegacyRead?): Boolean {
    return progressWasRead || legacy != null && (legacy.isComplete || legacy.signals.hasAny)
}

data class StartResolution(
    val route: String,
    /** Derived progress to save right away, so later choices are never mistaken for legacy usage. */
    val progressToPersist: OnboardingProgress?
)

fun resolveStartDestination(progress: OnboardingProgress?, legacy: LegacySignals): StartResolution {
    return when (progress) {
        OnboardingProgress.COMPLETED -> StartResolution(Routes.Home.route, null)
        OnboardingProgress.ONBOARDING_DONE -> StartResolution(Routes.Paywall.create(firstRun = true), null)
        OnboardingProgress.NOT_STARTED -> StartResolution(Routes.Onboarding.route, null)
        null -> if (legacy.hasAny) {
            StartResolution(Routes.Home.route, OnboardingProgress.COMPLETED)
        } else {
            StartResolution(Routes.Onboarding.route, OnboardingProgress.NOT_STARTED)
        }
    }
}
