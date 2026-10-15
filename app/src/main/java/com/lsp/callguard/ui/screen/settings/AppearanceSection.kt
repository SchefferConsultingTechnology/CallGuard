package com.lsp.callguard.ui.screen.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lsp.callguard.R
import com.lsp.callguard.domain.model.ThemeMode

private val themeOptions = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)

/** Theme choice (spec 006, US3): applied as soon as it is tapped, like the language. */
@Composable
internal fun AppearanceCard(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    SettingsCard {
        SettingsItemHeader(
            icon = Icons.Outlined.Contrast,
            title = stringResource(R.string.appearance_title),
            description = stringResource(R.string.appearance_description)
        )

        Spacer(modifier = Modifier.height(14.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            themeOptions.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selected,
                    onClick = { if (mode != selected) onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                    // No check mark: the filled segment already shows the choice, and the label
                    // must fit at 200% font size ("Sistema" was cut off).
                    icon = {},
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(mode.labelRes()),
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}
