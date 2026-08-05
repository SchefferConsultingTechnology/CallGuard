package com.lsp.callguard.domain.protection

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * RoleManager.ROLE_CALL_SCREENING only exists from API 29 (Android 10) onward.
 * On older devices there is no programmatic way to check or request the role,
 * so callers must fall back to a manual settings flow without a reliable status check.
 */
object CallScreeningRoleChecker {

    fun isRoleHeld(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

        val roleManager = context.getSystemService(RoleManager::class.java) ?: return false

        return roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
            roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun createRequestRoleIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

        val roleManager = context.getSystemService(RoleManager::class.java) ?: return null

        if (!roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) return null

        return roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
    }

    fun createLegacyFallbackIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    }
}
