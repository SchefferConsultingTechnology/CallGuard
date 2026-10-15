package com.lsp.callguard.core.language

import java.util.Locale

enum class AppLanguage(
    val code: String,
    val title: String,
    val subtitle: String,
    val flag: String
) {
    PT_BR("pt-BR", "Português", "Brasil", "🇧🇷"),
    EN_US("en-US", "English", "United States", "🇺🇸"),
    ES_ES("es-ES", "Español", "España", "🇪🇸");

    companion object {

        fun fromCode(code: String?): AppLanguage? {
            return entries.find { it.code == code }
        }

        /** Supported language for a device locale, matched by language only (pt-PT → PT_BR). */
        fun forLocale(locale: Locale): AppLanguage? {
            return when (locale.language) {
                "pt" -> PT_BR
                "es" -> ES_ES
                "en" -> EN_US
                else -> null
            }
        }

    }
}