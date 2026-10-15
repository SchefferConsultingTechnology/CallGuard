package com.lsp.callguard.ui.screen.settings

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import com.lsp.callguard.R

/** The pages of the Ajustes tab (feature 007), in list order. [key] is the route segment. */
enum class SettingsCategory(val key: String, @param:StringRes val title: Int) {
    PLAN("plan", R.string.settings_category_plan),
    PROTECTION("protection", R.string.settings_category_protection),
    CONTACTS("contacts", R.string.settings_category_contacts),
    BACKUP("backup", R.string.settings_category_backup),
    APPEARANCE("appearance", R.string.settings_category_appearance),
    PRIVACY("privacy", R.string.settings_category_privacy);

    val icon: ImageVector
        get() = when (this) {
            PLAN -> Icons.Outlined.WorkspacePremium
            PROTECTION -> Icons.Outlined.Security
            CONTACTS -> Icons.Outlined.Contacts
            BACKUP -> Icons.Outlined.Backup
            APPEARANCE -> Icons.Outlined.Contrast
            PRIVACY -> Icons.Outlined.PrivacyTip
        }

    companion object {
        fun fromKey(key: String?): SettingsCategory? = entries.firstOrNull { it.key == key }
    }
}
