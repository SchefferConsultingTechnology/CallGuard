package com.lsp.callguard.ui.screen.language

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.lsp.callguard.core.language.AppLanguage

@Composable
fun LanguageSelectionScreen(
    onConfirm: (AppLanguage) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf<AppLanguage?>(null) }

    val languages = listOf(
        AppLanguage.EN_US,
        AppLanguage.PT_BR,
        AppLanguage.ES_ES
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(24.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Spacer(modifier = Modifier.height(24.dp))

                // Header
                Text(
                    text = "Bem-vindo",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Escolha seu idioma preferido",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(languages) { language ->
                        LanguageItem(
                            language = language,
                            isSelected = selectedLanguage == language,
                            onClick = {
                                selectedLanguage = language
                            }
                        )
                    }
                }
            }

            Column {

                Button(
                    onClick = {
                        selectedLanguage?.let { onConfirm(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = selectedLanguage != null
                ) {
                    Text("Confirmar")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Você pode alterar o idioma nas configurações",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}