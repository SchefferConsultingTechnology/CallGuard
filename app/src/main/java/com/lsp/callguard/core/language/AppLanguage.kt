package com.lsp.callguard.core.language

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

    }
}