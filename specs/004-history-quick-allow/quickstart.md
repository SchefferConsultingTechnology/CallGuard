# Quickstart: Liberar pelo histórico

Emulador API 33+, papel de triagem concedido, idioma pt-BR. Build e gates como na 001.

1. Grátis, whitelist vazia, "bloquear desconhecidos" ligado: `adb emu gsm call 6505550199` → bloqueada.
2. Histórico → item `(650) 555-0199` com "Adicionar à whitelist" → nome "Clínica" → confirmar →
   aviso com "Desfazer"; item passa a "Na whitelist".
3. `adb emu gsm call 6505550199` → **permitida** (`ALLOWED_BY_WHITELIST`).
4. Repetir 1–2 com outro número e tocar "Desfazer" → número sai da whitelist.
5. Itens de número oculto, `12345` e chamadas permitidas não oferecem a ação.
6. Grátis com 5 números: ação → diálogo de limite → "Ver planos" → (debug) seletor Plus → voltar
   ao histórico → inclusão concluída sozinha.
7. Conferir textos em en e es.

Registrar em `checklists/manual-validation.md`.
