---

description: "Task list for 004-history-quick-allow"
---

# Tasks: Liberar Número a Partir do Histórico

**Input**: `specs/004-history-quick-allow/` · **Tests**: incluídos (lógica pura e ViewModel)

Prefixos: `main/` = `app/src/main/java/com/lsp/callguard/`, `test/` = `app/src/test/java/com/lsp/callguard/`,
`res/` = `app/src/main/res/`

## Phase 1: Setup

- [X] T001 Linha de base `./gradlew testDebugUnitTest assembleDebug` verde

## Phase 2: Foundational

- [X] T002 [P] Em `main/data/local/dao/AllowedNumberDao.kt` adicionar
  `@Query("DELETE FROM allowed_numbers WHERE phoneE164 = :phoneE164") suspend fun deleteByPhone(phoneE164: String)`;
  em `main/data/repository/WhitelistRepository.kt` `suspend fun deleteByPhone(phoneE164: String)`;
  implementar em `test/fakes/FakeAllowedNumberDao.kt`
- [X] T003 [P] Criar `test/ui/screen/history/HistoryQuickAllowTest.kt` com as 5 linhas do contrato
- [X] T004 Criar `main/ui/screen/history/HistoryQuickAllow.kt` com `enum class QuickAllowAction { ADD, IN_WHITELIST, NONE }`
  e `fun quickAllowAction(allowed: Boolean, canonical: String?, inWhitelist: Set<String>)`. T003 deve passar

## Phase 3: User Story 1 - Liberar um número bloqueado pelo histórico (P1) 🎯 MVP

**Independent Test**: quickstart 1–5.

- [X] T005 [P] [US1] Criar `test/fakes/FakeCallDecisionLogDao.kt` (estado em memória: `insert`,
  `observeLatest`, `deleteOlderThan`, `clear`)
- [X] T006 [P] [US1] Criar `test/ui/screen/history/CallHistoryViewModelTest.kt`: ações por item
  (bloqueado→ADD, já na whitelist→IN_WHITELIST, oculto/não interpretável/permitido→NONE, registro
  antigo sem `normalizedPhone` reinterpretado); `addToWhitelist` inclui e emite `Added`;
  `undoAdd` remove
- [X] T007 [US1] Reescrever `main/ui/screen/history/CallHistoryViewModel.kt`: recebe
  `WhitelistRepository`, `LicensePreferences`, `PhoneNormalizer`; estado com `actions` e
  `canonicalPhones` (R1); `addToWhitelist(phoneE164, label)`, `undoAdd(phoneE164)`, eventos.
  T006 deve passar
- [X] T008 [US1] Atualizar `main/ui/screen/history/CallHistoryViewModelFactory.kt` (whitelist com
  rótulo padrão `contact_no_name`, licença, `PhoneServices.normalizer`)
- [X] T009 [US1] Em `main/ui/screen/history/CallHistoryScreen.kt`/`CallHistoryRoute.kt`: botão
  "Adicionar à whitelist" nos itens `ADD`; selo "Na whitelist" nos `IN_WHITELIST`; diálogo de
  confirmação com o número formatado e campo de nome opcional; snackbar "Adicionado à whitelist"
  com "Desfazer"
- [X] T010 [P] [US1] Strings nos 3 idiomas (`res/values`, `res/values-pt-rBR`, `res/values-es-rES`)
- [X] T011 [US1] Validar quickstart 1–5 no emulador

## Phase 4: User Story 2 - Respeitar o limite do plano Grátis (P2)

**Independent Test**: quickstart 6.

- [X] T012 [US2] Em `CallHistoryViewModelTest.kt`: no limite → `LimitReached` e nada incluído;
  upgrade (licença PLUS) com pendência → inclusão automática + `AddedAfterUpgrade`
- [X] T013 [US2] No ViewModel: checar limite por `PlanFeatures` + contagem; guardar pendência;
  observar a licença e concluir a pendência quando couber. T012 deve passar
- [X] T014 [US2] Diálogo de limite no histórico com "Ver planos" e rota `onOpenPaywall` em
  `CallHistoryRoute.kt` + `main/ui/navigation/CallGuardNavHost.kt` (`Routes.Paywall.create(firstRun = false)`)
- [X] T015 [US2] Validar quickstart 6 (seletor de plano em debug)

## Phase 5: Polish

- [X] T016 Gates: `./gradlew testDebugUnitTest lintDebug assembleRelease`; `git diff --exit-code app/schemas/`
- [X] T017 Textos em en/es no emulador (quickstart 7) e registro em `checklists/manual-validation.md`
- [X] T018 `**Status**` → `Implemented` em `specs/004-history-quick-allow/spec.md`

## Dependencies

Setup → Foundational → US1 → US2 → Polish (US2 edita os mesmos arquivos da US1).
