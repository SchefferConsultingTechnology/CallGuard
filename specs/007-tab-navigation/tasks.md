---

description: "Task list for 007 — Navegação por Abas"
---

# Tasks: Navegação por Abas

**Input**: Design documents from `/specs/007-tab-navigation/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: incluídos — a pesquisa (R10) define testes JVM para as funções puras (rota → aba, resumos,
exibição de números).

**Organization**: por história. US1 (abas) é a base. US2 (Início) e US3 (Ajustes) dependem dela. US4
(retornos) fecha os fluxos.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência pendente)
- **[Story]**: US1 abas · US2 painel do Início · US3 Ajustes por categoria · US4 telas de tarefa
- `…/` = `app/src/main/java/com/lsp/callguard/`; testes em `app/src/test/java/com/lsp/callguard/`

---

## Phase 1: Setup

- [X] T001 [P] Adicionar as 25 strings dos contratos (`navigation-map.md`, `home-dashboard.md`, `settings-categories.md`) em `app/src/main/res/values/strings.xml`, `values-pt-rBR/strings.xml` e `values-es-rES/strings.xml`, agrupadas sob um comentário `<!-- 007 tabs -->`

---

## Phase 2: Foundational (bloqueia as histórias)

- [X] T002 [P] Teste `…test/ui/navigation/AppTabTest.kt`: `tabFor("home")`→HOME, `"whitelist"`→WHITELIST, `"call_history"`→HISTORY, `"settings"` e `"settings/backup"`→SETTINGS; `null`, `"paywall?firstRun={firstRun}"`, `"onboarding"`, `"protection_setup"`, `"contacts_consent"`, `"contacts_import"`, `"terms"`, `"privacy_policy"`, `"language"` → `null`
- [X] T003 [P] Criar `…/ui/navigation/AppTab.kt`: `enum class AppTab(val route: String, @StringRes label, icon: ImageVector, selectedIcon: ImageVector)` conforme data-model (HOME/home, WHITELIST/whitelist, HISTORY/call_history, SETTINGS/settings_graph) e `fun tabFor(route: String?): AppTab?` (prefixo `settings` → SETTINGS); T002 verde
- [X] T004 [P] Teste `…test/domain/phone/CallLogDisplayTest.kt`: log com `normalizedPhone` → formatado; log legado sem `normalizedPhone` com número válido → normalizado e formatado; número não interpretável → texto bruto; `phone = null` → `null` (usar `PhoneNormalizer()` e `PhoneNumberDisplayFormatter` reais, região "BR")
- [X] T005 Criar `…/domain/phone/CallLogDisplay.kt` com `canonicalOf(log, region, normalizer)` e `displayPhoneOf(log, region, normalizer, formatter)` movendo a lógica de `CallHistoryViewModel.canonicalOf` e do `formatter.formatOrRaw(...)`; fazer `…/ui/screen/history/CallHistoryViewModel.kt` usá-las sem mudar comportamento; T004 e testes existentes do histórico verdes
- [X] T006 Em `…/ui/navigation/Routes.kt`: adicionar `SettingsGraph("settings_graph")` e `SettingsCategoryPage("settings/{category}")` com `create(category: String)`; manter `Settings("settings")` como início do grafo

**Checkpoint**: funções puras testadas; app compila sem mudança visível.

---

## Phase 3: User Story 1 — Abas a um toque (Priority: P1) 🎯 MVP

**Goal**: barra com 4 abas, estado por aba, voltar → Início → sair, reseleção.

**Independent Test**: quickstart §2.

- [X] T007 [US1] Criar `…/ui/navigation/CallGuardAppShell.kt`:
  - `Scaffold(contentWindowInsets = WindowInsets(0), bottomBar = { if (tabFor(currentRoute) != null) CallGuardNavigationBar(...) })`.
  - Conteúdo: `CallGuardNavHost(modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding), …)`.
  - `NavigationBar` com `NavigationBarItem` por `AppTab`: ícone `selectedIcon` quando marcada, rótulo com `maxLines = 1`, e `alwaysShowLabel = LocalDensity.current.fontScale <= 1.5f` (R9).
  - Troca de aba: `navController.navigate(tab.route) { popUpTo(Routes.Home.route) { saveState = true }; launchSingleTop = true; restoreState = true }`.
  - Reseleção: SETTINGS fora de `settings` → `popBackStack(Routes.Settings.route, false)`; senão emite em `val tabReselection = remember { MutableSharedFlow<AppTab>(extraBufferCapacity = 1) }`.
  - Criar o `navController` aqui e passá-lo ao NavHost.
  - Transições (U1): entre abas, sem animação (`EnterTransition.None`/`ExitTransition.None` quando origem e destino são abas, via `tabFor` nas rotas do `initialState`/`targetState`). Telas de tarefa entram e saem com fade curto (`tween(150)`). Isso substitui o fade padrão de ~700 ms do `NavHost`.
- [X] T008 [US1] Em `…/ui/navigation/CallGuardNavHost.kt`:
  - Receber `navController`, `modifier` e `tabReselection: SharedFlow<AppTab>`.
  - Envolver `settings` em `navigation(startDestination = Routes.Settings.route, route = Routes.SettingsGraph.route)`. Por enquanto a rota `settings` renderiza o `SettingsRoute` atual.
  - Home: `onOpenWhitelist`, `onOpenCallHistory` e, temporariamente, `onOpenSettings` (engrenagem, removida na T016) passam a usar a navegação de aba (função `navigateToTab(navController, AppTab)` em `CallGuardAppShell.kt`, reutilizada). Nenhum parâmetro de `HomeRoute` é removido nesta tarefa, para o app continuar compilando.
  - Whitelist e Histórico: remover `onBack`.
- [X] T009 [US1] `…/CallGuardApp.kt`: usar `CallGuardAppShell` no lugar de `CallGuardNavHost` direto
- [X] T010 [P] [US1] `…/ui/screen/whitelist/WhitelistScreen.kt` + `WhitelistRoute.kt`:
  - Remover o parâmetro `onBack` e a seta do cabeçalho (`ArrowBack` e o import).
  - Receber `reselection: Flow<Unit>` e, ao emitir, `listState.animateScrollToItem(0)`. Usar o `LazyListState` existente ou criar um com `rememberLazyListState()`.
- [X] T011 [P] [US1] `…/ui/screen/history/CallHistoryScreen.kt` + `CallHistoryRoute.kt`: mesma mudança da T010 (sem seta; reseleção rola ao topo)
- [X] T012 [US1] Conectar a reseleção:
  - No NavHost, passar a cada aba `tabReselection.filter { it == AppTab.X }.map { }`.
  - O Início também rola ao topo: `…/ui/screen/home/HomeScreen.kt` + `HomeRoute.kt`, com `LazyListState`.
- [X] T013 [US1] Validar quickstart §2 no emulador (tema claro e escuro): troca de aba sem animação lenta (aparece em menos de 200 ms, medido pelo logcat como na 006), abas, estado preservado (Histórico rolado), voltar → Início → sai, reseleção, sem seta em Whitelist/Histórico; "Adicionar número" e snackbars acima da barra; registrar em `specs/007-tab-navigation/checklists/manual-validation.md` (criar)

**Checkpoint**: app navegável por abas; Início e Configurações ainda no formato antigo.

---

## Phase 4: User Story 2 — Início numa tela (Priority: P1)

**Goal**: painel com proteção, whitelist + plano lado a lado, 3 últimas chamadas ([contracts/home-dashboard.md](contracts/home-dashboard.md)).

**Independent Test**: quickstart §3.

- [X] T014 [US2] `…/ui/screen/home/HomeViewModel.kt`:
  - Receber `CallDecisionLogRepository`, `PhoneNormalizer`, `PhoneNumberDisplayFormatter` e `RegionProvider`.
  - Adicionar `data class RecentCall(id, displayPhone: String?, allowed, createdAt)` e `HomeUiState.recentCalls` (até 3, via `displayPhoneOf`).
  - Remover `useContactsAutomatically`, `importedContactsCount` e o `DeviceContactDao`.
  - Atualizar a factory em `…/ui/screen/home/HomeRoute.kt`.
- [X] T015 [US2] `…/ui/screen/home/HomeScreen.kt`:
  - Remover `HomeHeader` com engrenagem (manter título + subtítulo sem botão), `LockedContactsCard`, `PrivacyCard` e `CallHistoryCard`.
  - Trocar `PlanStatusCard` + `WhitelistLimitCard` por uma `Row` com `WhitelistSummaryCard` e `PlanSummaryCard` compactos, de mesma altura (`IntrinsicSize.Min`) e tocáveis (`onClick`).
  - Whitelist: "3 de 5" + barra (aviso no limite) ou plural ilimitado.
  - Plano: nome do nível + "Ver planos ›" (Grátis) ou "Gerenciar" (pago).
  - Criar `RecentCallsCard(recentCalls, onSeeAll)`: título `home_recent_calls`, "Ver tudo" à direita, até 3 linhas (ícone e texto de estado com as cores da 006, número ou "Número privado", hora `HH:mm` hoje / data curta antes) e estado vazio `home_no_calls_yet`.
  - Ordem: aviso de rebaixamento (quando houver) → proteção → linha whitelist/plano → últimas chamadas.
- [X] T016 [US2] `…/ui/screen/home/HomeRoute.kt` + NavHost: remover `onOpenSettings` e `onOpenContactsConsent`; ligar `onOpenWhitelist` e `onOpenCallHistory` à navegação de aba e o card de plano a `Routes.Paywall.create(false)`
- [X] T017 [US2] Validar quickstart §3:
  - Sem rolagem em 6,1" simulado (`wm size 1080x2400`, `wm density 420`), nos 3 idiomas.
  - Atalhos, estado vazio e aviso de rebaixamento.
  - Plano alterado em outra aba: em Ajustes → Plano, trocar Plus → Grátis (seletor debug) e tocar em Início. O aviso deve aparecer (U2).
  - Registrar em `checklists/manual-validation.md`.

**Checkpoint**: Início compacto.

---

## Phase 5: User Story 3 — Ajustes por categoria (Priority: P2)

**Goal**: lista de 6 categorias com resumo + páginas ([contracts/settings-categories.md](contracts/settings-categories.md)).

**Independent Test**: quickstart §4.

- [X] T018 [P] [US3] Criar `…/ui/screen/settings/SettingsCategory.kt`: enum `PLAN, PROTECTION, CONTACTS, BACKUP, APPEARANCE, PRIVACY` com `key` (`plan`, `protection`, …), `@StringRes title`, `icon`; `fromKey(key: String?): SettingsCategory?`
- [X] T019 [P] [US3] Teste `…test/ui/screen/settings/SettingsSummariesTest.kt`: cada linha da tabela de regras do data-model (PLAN por tier; PROTECTION 4 combinações; CONTACTS Grátis/ligado/desligado; APPEARANCE tema × idioma e idioma nulo; BACKUP e PRIVACY fixos)
- [X] T020 [US3] Criar `…/ui/screen/settings/SettingsSummaries.kt`: `data class Summary(@StringRes text: Int, args: List<Any> = emptyList())` e `fun settingsSummaries(state: SettingsUiState): Map<SettingsCategory, Summary>` (argumentos que são ids de string ficam como `Int` e a UI resolve); T019 verde
- [X] T021 [US3] `…/ui/screen/settings/SettingsViewModel.kt` + `SettingsViewModelFactory.kt`: adicionar `language: AppLanguage?` ao `SettingsUiState` a partir de `LanguagePreferences.selectedLanguageFlow`; a factory recebe o `ViewModelStoreOwner` para escopo no `settings_graph` (`rememberSettingsViewModel(owner: ViewModelStoreOwner)`)
- [X] T022 [US3] Criar `…/ui/screen/settings/SettingsListScreen.kt`: cabeçalho "Ajustes" + subtítulo (sem seta), um `SettingsCard` com as 6 categorias (ícone, título, resumo resolvido, chevron, divisórias), rodapé com `AppInfoCard` (versão); `onOpenCategory(SettingsCategory)`; aceita `reselection` para rolar ao topo
- [X] T023 [US3] Criar `…/ui/screen/settings/SettingsCategoryScreen.kt`:
  - `Scaffold` com cabeçalho (seta + título da categoria), `SnackbarHost` e conteúdo por categoria.
  - PLAN → `LicenseCard`. PROTECTION → `ProtectionSettingsCard`. CONTACTS → `ContactsSettingsCard`. BACKUP → `BackupCard`.
  - APPEARANCE → `AppearanceCard` + linha "Idioma" (abre `language`).
  - PRIVACY → `PrivacySettingsCard`.
  - Tornar `internal` os cards hoje `private` em `SettingsScreen.kt`.
- [X] T024 [US3] Dividir `…/ui/screen/settings/SettingsRoute.kt`:
  - `SettingsListRoute(viewModel, onOpenCategory, reselection)` e `SettingsCategoryRoute(category, viewModel, onBack, onOpenPaywall, onOpenLanguage, onOpenTerms, onOpenPrivacyPolicy, onOpenContactsConsent)`.
  - Lançadores de arquivo, diálogos de backup e snackbars ficam na rota da categoria (BACKUP).
  - As snackbars de restaurar compras vão para PLAN.
  - Remover `SettingsScreen()` (a função de tela única) e `AppSettingsCard`; manter os cards reaproveitados.
- [X] T025 [US3] No NavHost, dentro de `settings_graph`:
  - `composable("settings")` → `SettingsListRoute` e `composable(Routes.SettingsCategoryPage.route)` → `SettingsCategoryRoute`.
  - Os dois com `viewModel` do `navController.getBackStackEntry(Routes.SettingsGraph.route)`.
  - Abrir uma categoria: `navigate(Routes.SettingsCategoryPage.create(key))`.
  - O consentimento de contatos sai de Ajustes → Contatos (o card já tem a ação; passar `onOpenContactsConsent`).
- [X] T026 [US3] Validar quickstart §4 (6 categorias sem rolar em 6,1" simulado — `wm size 1080x2400`, `wm density 420` — nos 3 idiomas, resumos atualizando, "sem anúncios" uma vez, tema e idioma pela nova página); registrar em `checklists/manual-validation.md`

**Checkpoint**: Ajustes reorganizado.

---

## Phase 6: User Story 4 — Tarefas voltam à origem (Priority: P2)

**Goal**: telas de tarefa sem barra, retorno à aba e página de origem ([contracts/navigation-map.md](contracts/navigation-map.md)).

**Independent Test**: quickstart §5.

- [X] T027 [US4] NavHost: `ContactsImportRoute.onFinished` → `navController.popBackStack()` (em vez de `navigate(home)`); conferir que `contacts_consent` continua com `popUpTo(contacts_consent){inclusive}` ao abrir a importação
- [X] T028 [US4] Confirmar e ajustar os retornos restantes no NavHost:
  - `paywall?firstRun=false`, `protection_setup`, `terms`, `privacy_policy` e `language` → `popBackStack()`.
  - Primeiro uso concluído → `navigate(home){ popUpTo(graph){inclusive} }`.
  - Nenhuma navegação para `Routes.Settings`/`Routes.Home` fora da barra.
  - `grep -n "navigate(Routes.Home\|navigate(Routes.Settings" …/ui/navigation/CallGuardNavHost.kt` deve achar só o fim do primeiro uso.
- [X] T029 [US4] Validar quickstart §5 (tabela de tarefas, incluindo inclusão pendente do histórico após assinar e troca de idioma voltando à página) e §6 (girar, tema, fonte 200%, primeiro uso com `pm clear`); registrar em `checklists/manual-validation.md`

---

## Phase 7: Polish & Cross-Cutting

- [X] T030 Remover código morto: strings sem uso após a mudança (ex.: `manage_whitelist`, `view_call_history` se não usadas, `section_*` não usadas) nos 3 `strings.xml`; imports sem uso; conferir com `lintDebug` (UnusedResources)
- [X] T031 Gates: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon -Porg.gradle.java.installations.auto-detect=false :app:testDebugUnitTest :app:lintDebug :app:assembleRelease`; sem erros de lint; `app/schemas/` inalterado
- [X] T032 [P] Não regressão (quickstart §7) pelos novos caminhos:
  - 001: ligação de desconhecido bloqueada e de número da whitelist permitida.
  - 003: oferta pelo card de plano do Início e por Ajustes → Plano; "Continuar com versão gratuita" volta à origem.
  - 004: liberar pela aba Histórico + desfazer.
  - 005: exportar e importar por Ajustes → Cópia de segurança.
  - 006: tema por Ajustes → Aparência e idioma, mantendo a página.
- [X] T033 [P] Atualizar `specs/007-tab-navigation/spec.md` (Status: Implemented)

---

## Dependencies & Execution Order

```text
Setup (T001) ─► Foundational (T002–T006) ─► US1 (T007–T013) ─┬─► US2 (T014–T017) ─┐
                                                              ├─► US3 (T018–T026) ─┼─► US4 (T027–T029) ─► Polish
                                                              └────────────────────┘
```

- US2 e US3 só dependem da US1. Os dois mexem no NavHost (T016, T025): fazer nessa ordem.
- T005 antes de T014 (o Início usa `displayPhoneOf`).
- T010/T011 em paralelo; T012 depois deles.
- T021 antes de T022–T025.

## Parallel Examples

- Fase 2: T002 ‖ T004 ‖ T006 → T003, T005.
- US1: T010 ‖ T011 → T012.
- US3: T018 ‖ T019 → T020; T021 → T022 ‖ T023 → T024 → T025.

## Implementation Strategy

1. **MVP** = fases 1–3: o app navega por abas usando as telas atuais.
2. **+US2**: Início compacto.
3. **+US3**: Ajustes por categoria.
4. **+US4** e polish: retornos e validação completa.

Sugestão de commits: um por fase de história, após a validação dela.
