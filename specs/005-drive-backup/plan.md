# Implementation Plan: Backup no Google Drive

**Branch**: `005-drive-backup` | **Date**: 2026-10-15 | **Spec**: [spec.md](spec.md)

## Summary

Entrega em duas etapas. **Agora**: arquivo de backup protegido por senha (AES-256-GCM, chave por
PBKDF2), exportado e importado pelo seletor de arquivos do Android, com combinação de dados pelo
número canônico — disponível em todos os planos. **Depois** (bloqueado por configuração de conta
Google Cloud): cópia na pasta privada do app no Google Drive, restauração no primeiro uso e
sincronização automática no Pro, reaproveitando o mesmo formato e a mesma combinação.

## Technical Context

**Language/Version**: Kotlin 2.3.20 · **Primary Dependencies**: `javax.crypto` (plataforma),
Activity Result API (`CreateDocument`/`OpenDocument`) — nenhuma nova nesta etapa; a etapa Drive
exigirá Google Identity (Credential Manager / Authorization API) + Drive REST, a justificar então ·
**Storage**: arquivo escolhido pela pessoa; Room v2 e DataStore inalterados ·
**Testing**: JVM para cifra, formato e combinação; emulador para o fluxo ·
**Target Platform**: Android minSdk 26 / target 36 · **Project Type**: mobile-app (`:app`) ·
**Constraints**: nenhuma rede nesta etapa; arquivo ilegível sem a senha; importação nunca perde dados
locais · **Scale/Scope**: ~8 arquivos de produção, 4 de teste

## Constitution Check

| Princípio | Avaliação |
|---|---|
| I. Fail-Open | ✅ Importação só acrescenta/atualiza a whitelist; nada passa a ser bloqueado. |
| II. Privacidade (v2.0.0) | ✅ Exportação criptografada (exigência explícita); sem rede; contatos e licença fora do arquivo; histórico só por escolha. |
| III. Identidade | ✅ Números importados reinterpretados pela regra única; combinação pelo canônico. |
| IV. Puro e testado | ✅ Cifra, formato e combinação em JVM com testes; schema inalterado. |
| V. Localização | ✅ Textos e política de privacidade nos 3 idiomas. |
| VI. Monetização honesta | ✅ Exportar/importar em todos os planos; nada de Drive anunciado antes de existir. |

**Resultado**: PASS.

## Project Structure

```text
app/src/main/java/com/lsp/callguard/
├── domain/backup/BackupData.kt          # NOVO modelo
├── domain/backup/BackupCodec.kt         # NOVO serialização versionada
├── domain/backup/BackupCipher.kt        # NOVO AES-GCM + PBKDF2
├── domain/backup/BackupMerge.kt         # NOVO combinação pura
├── data/backup/BackupRepository.kt      # NOVO coleta/aplica dados (Room, DataStore)
├── ui/screen/settings/{SettingsScreen,SettingsRoute,SettingsViewModel,SettingsViewModelFactory}.kt
│                                        # seção Cópia de segurança, diálogos, seletor de arquivos
└── res/values*/strings.xml              # textos + política de privacidade
app/src/test/java/com/lsp/callguard/domain/backup/{BackupCipherTest,BackupCodecTest,BackupMergeTest}.kt
app/src/test/java/com/lsp/callguard/data/backup/BackupRepositoryTest.kt
```

## Complexity Tracking

Sem violações.
