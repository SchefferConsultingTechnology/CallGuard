package com.lsp.callguard.core.language

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    EN_US("en-US", "English", "English"),
    PT_BR("pt-BR", "Português", "Português"),
    ES_ES("es-ES", "Español", "Español")
}