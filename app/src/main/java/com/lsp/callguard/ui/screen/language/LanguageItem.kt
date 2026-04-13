package com.lsp.callguard.ui.screen.language

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lsp.callguard.core.language.AppLanguage

@Composable
fun LanguageItem(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isSelected) {
                Text("✔")
            }
        }
    }
}

