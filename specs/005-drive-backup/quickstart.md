# Quickstart: Backup (arquivo exportado)

1. Gates: `./gradlew testDebugUnitTest lintDebug assembleRelease` (flags da 001); schema inalterado.
2. Emulador: whitelist com 3 números, "bloquear números ocultos" desligado.
3. Configurações → Cópia de segurança → Exportar → senha `segredo123` (2×) → salvar em Downloads.
4. `adb pull` do arquivo: conteúdo ilegível (não contém os números em texto).
5. `pm clear` → concluir primeiro uso → Importar → arquivo → senha errada → recusa, nada muda.
6. Importar com `segredo123` → resumo "3 números importados"; whitelist e configuração restauradas.
7. Importar de novo → "0 adicionados" (sem duplicar).
8. Textos em en/es.

Drive (US1–US3, US5): bloqueado até o projeto Google Cloud existir.
