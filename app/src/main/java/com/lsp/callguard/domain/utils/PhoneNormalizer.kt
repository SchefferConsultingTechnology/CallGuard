package com.lsp.callguard.domain.utils

object PhoneNormalizer {

    fun normalize(input: String): String {
        // MVP: assume que já está em E.164
        return input.trim()
    }
}

