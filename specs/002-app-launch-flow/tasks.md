---

description: "Task list for 002-app-launch-flow"
---

# Tasks: Fluxo de Abertura e Idioma

**Input**: Design documents from `specs/002-app-launch-flow/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md,
contracts/launch-navigation.md, quickstart.md

**Tests**: INCLUÍDOS para a lógica de decisão pura (Princípio IV: lógica isolada e testável);
navegação validada ponta a ponta no emulador (quickstart).

**Organization**: Tarefas agrupadas por user story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependência pendente)
- **[Story]**: User story da spec (US1–US4)
- Prefixos: `main/` = `app/src/main/java/com/lsp/callguard/`,
  `test/` = `app/src/test/java/com/lsp/callguard/`, `res/` = `app/src/main/res/`
- Gradle (ambiente desta máquina): `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew
  --no-daemon -Porg.gradle.java.installations.auto-detect=false <tasks>`

---

## Phase 1: Setup

- [X] T001 Confirmar a linha de base: `./gradlew testDebugUnitTest assembleDebug` verde antes de
  qualquer mudança (com as flags acima)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Estado do primeiro uso, decisão da tela inicial e rota da oferta com `firstRun`.

**⚠️ CRITICAL**: Nenhuma user story começa antes desta fase terminar.

- [X] T002 [P] Criar `enum class OnboardingProgress { NOT_STARTED, ONBOARDING_DONE, COMPLETED }`
  em `main/domain/model/OnboardingProgress.kt` (ordem do enum = ordem de avanço)
- [X] T003 [P] Criar `test/core/language/AppLanguageTest.kt`: `forLocale(Locale("pt","BR"))`,
  `Locale("pt","PT")` → `PT_BR`; `Locale("es","MX")` → `ES_ES`; `Locale.US`, `Locale.UK` →
  `EN_US`; `Locale.FRENCH` → `null`
- [X] T004 Adicionar em `main/core/language/AppLanguage.kt` (companion)
  `fun forLocale(locale: Locale): AppLanguage?` mapeando por `locale.language` (`pt`, `es`, `en`).
  T003 deve passar
- [X] T005 [P] Criar `test/data/local/preferences/OnboardingPreferencesTest.kt` com DataStore em
  `File(tempFolder.root, "onboarding.preferences_pb")`: chave ausente → `progress() == null`;
  `advanceTo(ONBOARDING_DONE)` → lido de volta; `advanceTo(NOT_STARTED)` depois de
  `ONBOARDING_DONE` é ignorado (não regride); `advanceTo(COMPLETED)` final; valor corrompido
  (string desconhecida gravada direto) → `null`
- [X] T006 Criar `class OnboardingPreferences(dataStore)` em
  `main/data/local/preferences/OnboardingPreferences.kt` com
  `stringPreferencesKey("onboarding_progress")`, `suspend fun progress(): OnboardingProgress?`
  (`runCatching { valueOf }`) e `suspend fun advanceTo(progress)` dentro de um único
  `dataStore.edit` que só grava se `progress.ordinal > atual.ordinal` (ou atual ausente).
  T005 deve passar
- [X] T007 Em `main/ui/navigation/Routes.kt` trocar `Paywall` para rota com argumento:
  `data object Paywall : Routes("paywall?firstRun={firstRun}") { const val ARG_FIRST_RUN = "firstRun"; fun create(firstRun: Boolean) = "paywall?firstRun=$firstRun" }`
- [X] T008 [P] Criar `test/ui/navigation/StartDestinationTest.kt` cobrindo 1:1 as 7 linhas da
  tabela `resolveStartDestination` em
  `specs/002-app-launch-flow/contracts/launch-navigation.md` (rota e `progressToPersist`)
- [X] T009 Criar `main/ui/navigation/StartDestination.kt` com `data class LegacySignals`,
  `data class StartResolution(route: String, progressToPersist: OnboardingProgress?)` e
  `fun resolveStartDestination(progress, legacy): StartResolution` conforme o contrato (Home =
  `Routes.Home.route`, oferta = `Routes.Paywall.create(firstRun = true)`, apresentação =
  `Routes.Onboarding.route`). T008 deve passar
- [X] T010 Rodar `./gradlew testDebugUnitTest assembleDebug` — compila (a rota `Paywall` com
  argumento ainda não é usada com `navArgument`; usos antigos de `Routes.Paywall.route` serão
  trocados em US1/US4) e testes verdes

**Checkpoint**: decisão da tela inicial pronta e testada.

---

## Phase 3: User Story 1 - Abrir o app vai direto para a Home (Priority: P1) 🎯 MVP

**Goal**: Com o primeiro uso concluído, toda abertura mostra a Home; "voltar" na Home fecha o app.

**Independent Test**: Concluir o primeiro uso (oferta → "continuar grátis"), `am force-stop` e
reabrir 10× → Home sempre (quickstart §3); atualização de versão anterior com dados → Home (§6).

### Implementation for User Story 1

- [X] T011 [US1] Em `main/MainActivity.kt`, na coroutine antes do `setContent`: ler
  `OnboardingPreferences(appPreferencesDataStore).progress()` em `runCatching` (falha → `null`);
  se `null`, montar `LegacySignals` em `runCatching` (idioma salvo via
  `languagePreferences.selectedLanguageFlow.first() != null`, whitelist via
  `CallGuardDatabase.getInstance(applicationContext).allowedNumberDao().observeCount().first() > 0`,
  licença via `LicensePreferences(appPreferencesDataStore).licenseFlow.first().isLicensed`; falha →
  todos `false` e **não** persistir); chamar `resolveStartDestination`; se
  `progressToPersist != null` e não houve falha, `advanceTo(it)`; passar `resolution.route` para
  `CallGuardApp(startDestination = …)`
- [X] T012 [US1] Em `main/CallGuardApp.kt` e `main/ui/navigation/CallGuardNavHost.kt` adicionar o
  parâmetro `startDestination: String` e usá-lo no `NavHost` no lugar de `Routes.Language.route`;
  o `CallGuardNavHost` recebe também `onboardingPreferences: OnboardingPreferences` (criado em
  `MainActivity` e repassado por `CallGuardApp`)
- [X] T013 [US1] Em `main/ui/navigation/CallGuardNavHost.kt` substituir `composable(Routes.Paywall.route)`
  por `composable(route = Routes.Paywall.route, arguments = listOf(navArgument(Routes.Paywall.ARG_FIRST_RUN) { type = NavType.BoolType; defaultValue = false }))`;
  ler `firstRun` do `backStackEntry.arguments`; com `firstRun = true`, `onContinueFreeClick` e
  `onPurchased` fazem, **nesta ordem e dentro da mesma coroutine**:
  `scope.launch { onboardingPreferences.advanceTo(COMPLETED); navController.navigate(Routes.Home.route) { popUpTo(navController.graph.id) { inclusive = true } } }`
  (com `rememberCoroutineScope()`). Navegar só **depois** de a gravação terminar: o `popUpTo`
  remove a oferta da composição e cancelaria o scope antes de gravar (análise U1). Ignorar
  cliques repetidos enquanto grava (flag `remember { mutableStateOf(false) }`). Com
  `firstRun = false`, ambos fazem
  `navController.popBackStack()` (usado por US4)
- [X] T014 [US1] Gerar APK, instalar no emulador e executar quickstart §3 (10 aberturas → Home;
  "voltar" fecha) e §6 (atualização com dados → Home; sem dados → apresentação)

**Checkpoint**: MVP — o incômodo diário (atravessar 3 telas a cada abertura) acabou.

---

## Phase 4: User Story 2 - Primeiro uso pergunta o idioma uma única vez (Priority: P1)

**Goal**: Primeira tela = apresentação com idioma pré-marcado; troca ao tocar; oferta em seguida;
retomada na oferta se o app fechar ali.

**Independent Test**: Instalação nova → apresentação com idioma do aparelho marcado; tocar outro
idioma traduz na hora; "Continuar" → oferta; fechar/reabrir → oferta; "voltar" → apresentação
(quickstart §2, §7).

### Implementation for User Story 2

- [X] T015 [US2] Em `main/ui/navigation/CallGuardNavHost.kt`, no `composable(Routes.Onboarding.route)`:
  `onContinue` faz `scope.launch { onboardingPreferences.advanceTo(ONBOARDING_DONE); navController.navigate(Routes.Paywall.create(firstRun = true)) }`
  — navegar só depois de gravar (análise U1) — **sem** `popUpTo` (voltar da oferta retorna à
  apresentação)
- [X] T016 [US2] Em `main/ui/screen/onboarding/OnboardingScreen.kt`: seleção inicial =
  `remember { LocaleManagerHelper.currentLanguage() ?: AppLanguage.forLocale(Locale.getDefault()) ?: AppLanguage.EN_US }`
  (deixar de usar `persistedLanguage`/`collectAsState` do DataStore para isso);
  `localSelection ?: inicial` define o cartão marcado; botão "Continuar" sempre habilitado
  (remover `enabled = selectedLanguage != null`); o toque continua salvando e aplicando o idioma.
  Se a validação de quickstart §7 mostrar que o Android **não** usa os textos de `pt-rBR`/`es-rES`
  para variantes como `pt-PT`/`es-MX` (tela em inglês com outro idioma marcado), corrigir para
  marcar o idioma pelo texto realmente exibido: comparar `stringResource(R.string.app_locale_tag)`
  (nova string com a tag de cada pasta: `en-US`/`pt-BR`/`es-ES`) em vez de `Locale.getDefault()`
  (análise U3)
- [X] T017 [US2] Em `main/ui/navigation/CallGuardNavHost.kt` remover do `composable(Routes.Language.route)`
  a navegação para `Routes.Onboarding` (o destino inicial nunca mais é `Language`; o
  comportamento final da tela vem em US3) — garantir que nenhum caminho do primeiro uso leva à
  tela de idioma
- [X] T018 [US2] Instalar no emulador e executar quickstart §2 e §7

**Checkpoint**: US1 + US2 entregam o fluxo de abertura completo.

---

## Phase 5: User Story 3 - Trocar o idioma pelas Configurações e voltar (Priority: P2)

**Goal**: Configurações → Idioma mostra o idioma vigente, aplica ao tocar e volta.

**Independent Test**: Home → Configurações → Idioma → tocar "English" → tela em inglês na hora →
"Concluir" → Configurações em inglês (quickstart §4).

### Implementation for User Story 3

- [X] T019 [US3] Em `main/ui/screen/language/LanguageSelectionViewModel.kt`: `onLanguageSelected`
  atualiza o estado, grava em `languagePreferences.saveSelectedLanguage(language)` (em
  `viewModelScope`) e chama `LocaleManagerHelper.applyLanguage(language)`; remover `onConfirm` e
  `isSaving`
- [X] T020 [US3] Em `main/ui/screen/language/LanguageSelectionScreen.kt`: trocar o parâmetro
  `onConfirm` por `onDone`; botão inferior com `stringResource(R.string.done)`, sempre habilitado,
  chamando `onDone`; remover o `Text(R.string.language_change_later)`
- [X] T021 [US3] Em `main/ui/navigation/CallGuardNavHost.kt`, no `composable(Routes.Language.route)`:
  `onSelectLanguage = viewModel::onLanguageSelected`, `onDone = { navController.popBackStack() }`;
  remover a leitura de `uiState.selectedLanguage` para navegação e o uso de
  `LocaleManagerHelper` aqui
- [X] T022 [P] [US3] Adicionar `done` ("Done" / "Concluir" / "Listo") em `res/values/strings.xml`,
  `res/values-pt-rBR/strings.xml` e `res/values-es-rES/strings.xml`; remover
  `language_change_later` dos três após `grep -rn language_change_later app/src` confirmar que
  não há outros usos
- [X] T023 [US3] Instalar no emulador e executar quickstart §4

**Checkpoint**: US3 funciona sem afetar US1/US2.

---

## Phase 6: User Story 4 - Oferta a partir do app volta à origem (Priority: P3)

**Goal**: Atalhos internos para a oferta usam `firstRun = false` e voltam à tela de origem.

**Independent Test**: Whitelist → oferta → "continuar grátis" → Whitelist; Home → oferta →
"continuar grátis" → Home; "voltar" fecha o app de primeira (quickstart §5).

### Implementation for User Story 4

- [X] T024 [US4] Em `main/ui/navigation/CallGuardNavHost.kt` trocar todas as chamadas
  `navController.navigate(Routes.Paywall.route)` dos atalhos internos (Home, Whitelist, Settings,
  ContactsConsent — hoje nas linhas ~85, ~105, ~116, ~164) por
  `navController.navigate(Routes.Paywall.create(firstRun = false))`; confirmar com
  `grep -n "Routes.Paywall.route" main/ui/navigation/CallGuardNavHost.kt` que só resta o uso no
  `composable(route = …)`
- [X] T025 [US4] Instalar no emulador e executar quickstart §5

**Checkpoint**: Todas as stories completas.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T026 [P] Verificar recriação por troca de idioma em 3 telas (apresentação, oferta com
  `firstRun = true`, tela de idioma das Configurações): a tela atual é preservada (FR-011)
- [X] T027 [P] Conferir com `grep -rn "R.string.confirm" app/src/main/java` se `confirm` ainda é
  usado; se não, removê-lo dos 3 `strings.xml`
- [X] T028 Rodar os gates: `./gradlew testDebugUnitTest lintDebug assembleRelease` e
  `git diff --exit-code app/schemas/`
- [X] T029 Executar a validação dos 3 idiomas ponta a ponta (instalação nova em cada idioma do
  aparelho/app, aberturas seguintes, troca pelas Configurações) e registrar todos os resultados
  do quickstart em `specs/002-app-launch-flow/checklists/manual-validation.md`, incluindo: fechar o
  app logo após "Continuar grátis" (U1), variante de idioma `pt-PT` (U3) e compra cancelada no
  primeiro uso permanece na oferta (C1)
- [X] T030 Atualizar `**Status**: Draft` → `**Status**: Implemented` em
  `specs/002-app-launch-flow/spec.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup → Foundational → US1 → US2 → US3 → US4 → Polish
- US1 e US2 são ambas P1 e editam `CallGuardNavHost.kt`; executar US1 antes (a rota inicial
  dinâmica e a oferta `firstRun` são pré-requisito do fluxo da US2)
- US3 e US4 dependem só da Phase 2, mas também editam `CallGuardNavHost.kt` → sequenciais

### Within Phase 2

- T002 → T006 e T009; T003 → T004; T005 → T006; T007 → T009; T008 → T009 → T010

### Parallel Opportunities

- Phase 2: T002, T003, T005, T008 em paralelo; T004 ∥ T006 ∥ T007
- US3: T022 em paralelo com T019–T021
- Polish: T026 ∥ T027

---

## Parallel Example: Phase 2

```text
Task: "T002 OnboardingProgress em main/domain/model/OnboardingProgress.kt"
Task: "T003 AppLanguageTest"
Task: "T005 OnboardingPreferencesTest"
Task: "T008 StartDestinationTest"
```

---

## Implementation Strategy

### MVP First (US1)

1. Phase 1 + Phase 2
2. Phase 3 (US1): app abre direto na Home depois do primeiro uso
3. **PARAR e VALIDAR** (quickstart §3, §6)

### Incremental Delivery

1. US1 → aberturas diretas na Home
2. US2 → primeiro uso enxuto (idioma uma vez)
3. US3 → idioma pelas Configurações coerente
4. US4 → oferta volta à origem

---

## Notes

- Sem mudança de schema Room; qualquer diff em `app/schemas/` é erro
- Commits por story (Conventional Commits)
