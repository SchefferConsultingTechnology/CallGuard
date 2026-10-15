---

description: "Task list for 005-drive-backup"
---

# Tasks: Backup no Google Drive

**Tests**: incluídos. Prefixos: `main/` = `app/src/main/java/com/lsp/callguard/`,
`test/` = `app/src/test/java/com/lsp/callguard/`, `res/` = `app/src/main/res/`

## Phase 1: Setup

- [X] T001 Linha de base `./gradlew testDebugUnitTest assembleDebug` verde

## Phase 2: Foundational (formato, cifra e combinação)

- [X] T002 [P] Criar `main/domain/backup/BackupData.kt` (`BackupData`, `BackupEntry`,
  `BackupSettings`, `BackupLog`, `sealed interface BackupError` com `WrongPasswordOrCorrupted`,
  `NewerFormat`, `Unreadable`, `const val BACKUP_FORMAT_VERSION = 1`)
- [X] T003 [P] `test/domain/backup/BackupCodecTest.kt` + `main/domain/backup/BackupCodec.kt`
  (`encode(BackupData): ByteArray`, `decode(ByteArray): Result`) conforme o contrato
- [X] T004 [P] `test/domain/backup/BackupCipherTest.kt` + `main/domain/backup/BackupCipher.kt`
  (`encrypt(plain, password)`, `decrypt(file, password)`) conforme o contrato
- [X] T005 [P] `test/domain/backup/BackupMergeTest.kt` + `main/domain/backup/BackupMerge.kt`
  (`mergeWhitelist(local, imported, normalizer, region): WhitelistMerge(toInsert, toUpdate)`)

## Phase 3: User Story 4 - Exportar e importar um arquivo protegido (P2) 🎯 entrega atual

- [X] T006 [US4] `test/data/backup/BackupRepositoryTest.kt` (fakes): exportar coleta whitelist,
  configurações e histórico opcional; importar aplica a combinação, configurações e histórico (sem
  duplicar ids), retorna contagens; senha errada não altera nada
- [X] T007 [US4] `main/data/backup/BackupRepository.kt`: `suspend fun export(password, includeHistory): ByteArray`,
  `suspend fun import(bytes, password): ImportResult` usando `AllowedNumberDao`, `CallDecisionLogDao`,
  `SettingsPreferences`, `LanguagePreferences`, `LicensePreferences` (retenção após importar
  histórico) e `PhoneNormalizer`/`RegionProvider`. T006 deve passar
- [X] T008 [US4] Settings: seção "Cópia de segurança" com "Exportar dados" (diálogo: senha,
  confirmação, mínimo 8, "incluir histórico") e "Importar dados" (diálogo de senha); seletor de
  arquivos `CreateDocument("application/octet-stream")` com nome `callguard-AAAA-MM-DD.cgbk` e
  `OpenDocument`; leitura/escrita via `ContentResolver` em IO; snackbar com o resultado; idioma
  importado aplicado com `LocaleManagerHelper`
- [X] T009 [P] [US4] Strings nos 3 idiomas e atualização de `privacy_policy_body` (exportação
  criptografada, só onde a pessoa escolher)
- [X] T010 [US4] Validar quickstart 2–8 no emulador

## Phase 4–7: Google Drive (US1, US2, US3, US5) — BLOQUEADO

Pré-requisito externo: projeto Google Cloud com tela de consentimento OAuth, cliente Android
(`com.lsp.callguard` + SHA-1 da chave de debug e da de upload) e Google Drive API habilitada.
Ao desbloquear, gerar as tarefas com `/speckit-tasks` (escopo `drive.appdata`, envio
automático em até 15 min, restauração no primeiro uso, desativar/apagar), reaproveitando
`BackupCodec`/`BackupMerge`, e trocar `PlanCatalog.PRO_ON_SALE` para `true` ao publicar.

- [ ] T011 [US1] BLOQUEADO — ativar backup no Drive (Pro)
- [ ] T012 [US2] BLOQUEADO — restaurar do Drive no primeiro uso
- [ ] T013 [US3] BLOQUEADO — envio automático e vários aparelhos (registro de remoções)
- [ ] T014 [US5] BLOQUEADO — desativar e apagar a cópia remota

## Phase 8: Polish (desta entrega)

- [X] T015 Gates `./gradlew testDebugUnitTest lintDebug assembleRelease`; `git diff --exit-code app/schemas/`
- [X] T016 Registrar validação em `checklists/manual-validation.md`; status da spec
