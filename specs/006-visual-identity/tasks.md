---

description: "Task list for 006 — Identidade Visual e Temas Claro/Escuro"
---

# Tasks: Identidade Visual e Temas Claro/Escuro

**Input**: Design documents from `/specs/006-visual-identity/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: incluídos — a pesquisa (R11) define testes JVM de contraste (SC-001), `ThemeMode` e codec v2.

**Organization**: por história. US2 (paleta) vem antes de US1 (marca) apesar de ambas serem P1: US1
depende de aprovação do desenho do símbolo (R7), e US2/US3 não dependem dela.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência pendente)
- **[Story]**: US1 marca/ícone · US2 esquema de cores · US3 escolha de tema
- Caminhos relativos à raiz; código em `app/src/main/java/com/lsp/callguard/` (abreviado `…/`)

---

## Phase 1: Setup

- [X] T001 Adicionar `androidx.core:core-splashscreen` (versão estável mais recente) em `gradle/libs.versions.toml` (`[versions] coreSplashscreen`, `[libraries] androidx-core-splashscreen`) e `implementation(libs.androidx.core.splashscreen)` em `app/build.gradle.kts`; `assembleDebug` verde
- [X] T002 [P] Limpar `app/src/main/res/values/colors.xml`: remover purple/teal/black/white do template (confirmar com grep que nada os usa) e definir cores da marca para XML: `brand_navy #0B1F3B`, `brand_blue #2563EB`, `brand_cyan #22D3EE`, `window_background #F5F7FB`; criar `app/src/main/res/values-night/colors.xml` com `window_background #071426`

---

## Phase 2: Foundational (bloqueia todas as histórias)

**Purpose**: paleta pura e tema Compose com parâmetro claro/escuro — tudo depende disso.

- [X] T003 [P] Criar teste `app/src/test/java/com/lsp/callguard/core/theme/CallGuardPaletteContrastTest.kt`: função de luminância relativa/contraste WCAG 2.x sobre ARGB `Long`; para `CallGuardPalette.Light` e `.Dark`, verificar todos os pares de texto (≥ 4,5) e ícone/borda (≥ 3,0) listados em `specs/006-visual-identity/contracts/color-roles.md`; mensagem de falha com nome do par e valor (deve falhar até T004)
- [X] T004 Criar `…/core/theme/CallGuardPalette.kt`: `data class PaletteColors(...)` com os 28 papéis do `data-model.md` como `Long` ARGB e objetos `CallGuardPalette.Light` / `.Dark` com os valores de `research.md` § R2 (primário escuro `#5B9BFA`/`onPrimary #0B1F3B`; `secondary = primary`; `tertiary = #22D3EE`; `secondaryContainer = primaryContainer`; `surfaceVariant #EEF2F8/#16304F`; `outline #7A879C/#6F84A3`; `dialogSurface #FFFFFF/#132C4F`; `on*Container` de erro/sucesso/aviso); sem import de Compose; T003 verde
- [X] T005 [P] Criar `…/core/theme/ExtendedColors.kt`: `@Immutable data class CallGuardExtendedColors(success, successContainer, onSuccessContainer, warning, warningContainer, onWarningContainer: Color)`, `LocalCallGuardExtendedColors = staticCompositionLocalOf { … light … }` e `object CallGuardTheme { val extendedColors @Composable get() = LocalCallGuardExtendedColors.current }`
- [X] T006 Reescrever `…/core/theme/Theme.kt`: `CallGuardTheme(darkTheme: Boolean = isSystemInDarkTheme(), content)` montando `lightColorScheme`/`darkColorScheme` a partir da paleta com **todos** os papéis (incl. `surfaceContainer`, `surfaceContainerLow/Lowest/High/Highest` = surface ou dialogSurface, `surfaceTint = surface` para não tingir, `inverseSurface`, `inverseOnSurface`, `inversePrimary`, `error*`, `scrim`), provendo `LocalCallGuardExtendedColors`; sem cores dinâmicas; apagar `…/core/theme/Color.kt` e corrigir imports (`BluePrimary` etc.) onde existirem

**Checkpoint**: app compila, tema claro com nova paleta; testes JVM verdes.

---

## Phase 3: User Story 2 — Cores da marca, legíveis (Priority: P1) 🎯 MVP

**Goal**: todas as telas e diálogos seguem os papéis de [contracts/color-roles.md](contracts/color-roles.md).

**Independent Test**: quickstart §4 no tema claro — capturas de todas as telas conferidas com o contrato; bloqueado vermelho e permitido verde com ícone e texto.

- [X] T007 [P] [US2] Em `…/ui/screen/history/CallHistoryScreen.kt`: item do histórico e resumo usam `CallGuardTheme.extendedColors.success` (permitido) e `MaterialTheme.colorScheme.error` (bloqueado) para ícone e texto de status; o círculo do ícone usa `successContainer`/`errorContainer`; manter ícone Verified/Block e texto (cor nunca é o único sinal); ação "Adicionar à whitelist" continua `primary`
- [X] T008 [P] [US2] Em `…/ui/screen/home/HomeScreen.kt`: `DowngradeNoticeCard` e demais avisos usam `warningContainer`/`onWarningContainer` + ícone `warning`; círculos `primary.copy(alpha=…)` → `primaryContainer` com ícone `primary`
- [X] T009 [P] [US2] Trocar `primary.copy(alpha = …)` de fundos de ícone por `MaterialTheme.colorScheme.primaryContainer` em `…/ui/screen/settings/SettingsScreen.kt`, `…/ui/screen/paywall/PaywallScreen.kt` e `…/ui/components/LanguageOptionCard.kt` (estado selecionado mantém contraste ≥ 3 com borda/ícone)
- [X] T010 [P] [US2] Mesma troca em `…/ui/screen/whitelist/WhitelistScreen.kt` e no aviso de limite do plano Grátis usar `warningContainer`/`onWarningContainer`
- [X] T011 [P] [US2] Mesma troca em `…/ui/screen/protection/ProtectionSetupScreen.kt`, `…/ui/screen/consent/ContactsConsentScreen.kt` e `…/ui/screen/contacts/ContactsImportScreen.kt`
- [X] T012 [US2] Varredura: `grep -rn "Color(0x\|Color\.\(White\|Black\|Gray\|Red\|Green\)\|\.copy(alpha" app/src/main/java --include=*.kt | grep -v core/theme`; cada ocorrência restante vira papel do tema ou é justificada em comentário (ex.: scrim); diálogos (`AlertDialog`) sem `containerColor` fixo
- [X] T013 [US2] Validar no emulador (tema claro) conforme quickstart §4; corrigir desvios encontrados; registrar em `specs/006-visual-identity/checklists/manual-validation.md` (criar) com capturas citadas

**Checkpoint**: tema claro completo com a paleta da marca.

---

## Phase 4: User Story 3 — Claro / Escuro / Seguir o sistema (Priority: P2)

**Goal**: escolha em Configurações, aplicada na hora, persistida, incluída no backup.

**Independent Test**: quickstart §5 e §6.

### Tests

- [X] T014 [P] [US3] Criar `app/src/test/java/com/lsp/callguard/domain/model/ThemeModeTest.kt`: `fromStorage` de `"system"/"light"/"dark"` → enum; `null`, `""`, `"blue"` → `SYSTEM`; `storageValue` ida e volta para os 3
- [X] T015 [P] [US3] Ampliar `app/src/test/java/com/lsp/callguard/domain/backup/BackupCodecTest.kt`: (a) ida e volta v2 com cada `themeMode` e `null`; (b) fixture de bytes v1 escrita à mão no teste no layout antigo (`writeInt(1)…` sem tema) decodifica com `themeMode = null`; (c) versão 3 → `BackupError.NewerFormat`

### Implementation

- [X] T016 [P] [US3] Criar `…/domain/model/ThemeMode.kt`: `enum class ThemeMode(val storageValue: String) { SYSTEM("system"), LIGHT("light"), DARK("dark") }` + `companion fun fromStorage(value: String?): ThemeMode`; T014 verde
- [X] T017 [P] [US3] Criar `…/data/local/preferences/AppearancePreferences.kt` (padrão de `SettingsPreferences`): chave `theme_mode`, `themeModeFlow: Flow<ThemeMode>` com `catch { emit(SYSTEM) }`, `suspend fun setThemeMode(mode)`
- [X] T018 [US3] Criar `…/core/theme/ThemeModeApplier.kt`: `fun apply(context, mode)`: no Android 12+ (API 31) usar **só** `context.getSystemService(UiModeManager::class.java).setApplicationNightMode(MODE_NIGHT_AUTO | NO | YES)`; abaixo disso, só `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM | NO | YES)`; nos dois casos não chamar quando o modo já for o atual; conferir no emulador que a troca recria a tela uma única vez (um `onCreate` no log)
- [X] T019 [US3] Em `…/MainActivity.kt`, **antes** de `super.onCreate`: `runBlocking { withTimeoutOrNull(300) { AppearancePreferences(…).themeModeFlow.first() } } ?: SYSTEM` → `ThemeModeApplier.apply`; chamar `enableEdgeToEdge()` após `super.onCreate`; **não** ler tema em `CallGuardApplication` (comentário citando o Princípio I)
- [X] T020 [US3] `app/src/main/res/values/themes.xml`: `Theme.CallGuard` com parent `Theme.AppCompat.DayNight.NoActionBar` e `android:windowBackground=@color/window_background` (evita flash entre a abertura e o Compose)
- [X] T021 [US3] Conferir insets com edge-to-edge em todas as telas (`Scaffold` com `contentWindowInsets` / `windowInsetsPadding`) em `…/ui/screen/**`; corrigir conteúdo atrás das barras se houver
- [X] T022 [P] [US3] Strings `appearance_title`, `appearance_description`, `theme_system`, `theme_light`, `theme_dark` em `app/src/main/res/values/strings.xml`, `values-pt-rBR/strings.xml`, `values-es-rES/strings.xml` (textos do contrato appearance-settings)
- [X] T023 [US3] `…/ui/screen/settings/SettingsViewModel.kt` + `SettingsViewModelFactory.kt`: receber `AppearancePreferences`, expor `themeMode` no estado e `fun setThemeMode(mode)` que grava e chama `ThemeModeApplier.apply`
- [X] T024 [US3] Criar `…/ui/screen/settings/AppearanceSection.kt`: `AppearanceCard(selected, onSelect)` com `SettingsCard` + `SettingsItemHeader(Icons.Outlined.Contrast, …)` + `SingleChoiceSegmentedButtonRow` (Sistema · Claro · Escuro); inserir em `SettingsScreen.kt` na seção "Aplicativo" logo após `AppSettingsCard`, ligando estado e callback via `SettingsRoute.kt`
- [X] T025 [US3] Backup v2: `…/domain/backup/BackupData.kt` (`BACKUP_FORMAT_VERSION = 2`, `BackupSettings.themeMode: String?`), `…/domain/backup/BackupCodec.kt` (grava `nullUTF themeMode` no fim; lê só se `version >= 2`), `…/data/backup/BackupRepository.kt` (exporta `themeMode` atual; na importação, se `ThemeMode.fromStorage` reconhecer o valor, grava e aplica; `null` não altera); T015 verde
- [X] T026 [US3] Validar no emulador quickstart §5 (troca na hora mantendo a tela, persistência, seguir sistema com `cmd uimode night`, fonte 200%) e §6 (backup v2 e v1); capturas de todas as telas no escuro (SC-004); medir SC-003 gravando a tela (`adb shell screenrecord`) do toque em "Escuro" até a tela inteira escura (< 1 s) e registrar o valor; registrar em `checklists/manual-validation.md`

**Checkpoint**: temas completos e selecionáveis; entregável sem a marca nova.

---

## Phase 5: User Story 1 — Ícone, abertura e logo (Priority: P1)

**Goal**: símbolo como ícone (com monocromático), abertura sem flash, logo completa na apresentação.

**Independent Test**: quickstart §2 e §3.

- [X] T027 [US1] Desenhar `specs/006-visual-identity/assets/callguard-symbol.svg` (viewBox 108×108, conteúdo dentro do círculo seguro de 66 no centro): hexágono em fita formando o "G" como na prancha, gradiente linear `#2563EB → #22D3EE`; gerar prévia PNG lado a lado com `assets/logo-board.png` (ex.: `rsvg-convert`/Inkscape/Python) e incluir prévia nas máscaras círculo/quadrado/gota
- [X] T028 [US1] ⛔ **Ponto de aprovação**: apresentar a prévia à pessoa responsável; iterar T027 até aprovação explícita. Não seguir para T029+ sem aprovação
- [X] T029 [P] [US1] Converter o SVG aprovado em `app/src/main/res/drawable/ic_launcher_foreground.xml` (vector 108dp, gradiente via `aapt:attr`), `ic_launcher_background.xml` (navy `@color/brand_navy` sólido), `ic_launcher_monochrome.xml` (mesmo traçado, cor sólida única) e `ic_brand_symbol.xml` (símbolo recortado sem margem do adaptive icon, para uso na UI)
- [X] T030 [US1] `app/src/main/res/mipmap-anydpi/ic_launcher.xml` e `ic_launcher_round.xml`: `<monochrome android:drawable="@drawable/ic_launcher_monochrome"/>`; remover `app/src/main/res/mipmap-{m,h,xh,xxh,xxxh}dpi/*.webp` (não usados com minSdk 26) e conferir que nada referencia esses arquivos
- [X] T031 [US1] Exportar `specs/006-visual-identity/assets/play-icon-512.png` (512×512, fundo navy, símbolo, sem transparência e sem texto) do SVG aprovado
- [X] T032 [US1] Abertura: em `app/src/main/res/values/themes.xml` criar `Theme.CallGuard.Starting` (parent `Theme.SplashScreen`, `windowSplashScreenBackground=@color/window_background`, `windowSplashScreenAnimatedIcon=@drawable/ic_launcher_foreground`, `postSplashScreenTheme=@style/Theme.CallGuard`); aplicar em `<activity .MainActivity android:theme>` no `AndroidManifest.xml`; em `…/MainActivity.kt` chamar `installSplashScreen()` antes de `super.onCreate` e `setKeepOnScreenCondition { startDestination == null }` até a rota inicial ser decidida, com `@Volatile private var startDestination: String? = null` na Activity preenchido pela coroutine que já decide a rota; símbolo direto sobre o fundo do tema, **sem** `windowSplashScreenIconBackgroundColor`
- [X] T033 [P] [US1] String `brand_tagline` nos 3 `strings.xml` (contrato appearance-settings)
- [X] T034 [US1] Criar `…/ui/components/BrandLogo.kt`: `Image(painterResource(R.drawable.ic_brand_symbol))` + wordmark `buildAnnotatedString` de `stringResource(R.string.app_name)` com "Call" em `onBackground` e "Guard" em `primary` (dividir pelo sufixo "Guard"; se `app_name` não terminar assim, cor única) + tagline em `onSurfaceVariant`, caixa alta com `uppercase(Locale.current)`, `letterSpacing`; `contentDescription` = app_name
- [X] T035 [US1] Em `…/ui/screen/onboarding/OnboardingScreen.kt`: substituir o ícone `PhoneLocked` do topo por `BrandLogo`; conferir rolagem em telas pequenas e fonte 200%
- [X] T036 [US1] Validar quickstart §2 (máscaras, ícone temático) e §3 (abertura no escuro sem flash, apresentação nos 3 idiomas); registrar em `checklists/manual-validation.md`

**Checkpoint**: identidade completa.

---

## Phase 6: Polish & Cross-Cutting

- [X] T037 Gates: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon -Porg.gradle.java.installations.auto-detect=false :app:testDebugUnitTest :app:lintDebug :app:assembleRelease`; lint sem erros novos; `app/schemas/` inalterado
- [X] T038 [P] Não regressão (quickstart §7): `adb emu gsm call` de desconhecido (bloqueado) e de número da whitelist (permitido) com o papel de triagem concedido
- [X] T039 [P] Atualizar `specs/006-visual-identity/spec.md` (Status: Implemented) e o guia da Play Console (memória/artefato) com o caminho de `assets/play-icon-512.png`

---

## Dependencies & Execution Order

```text
Setup (T001–T002) ─► Foundational (T003–T006) ─┬─► US2 (T007–T013) ─┐
                                                ├─► US3 (T014–T026) ─┼─► Polish (T037–T039)
                                                └─► US1 (T027–T036) ─┘
                                                     └ T028 aprovação bloqueia T029–T036
```

- US2 e US3 só dependem da fase 2; podem andar juntas (arquivos diferentes, exceto `SettingsScreen.kt`: T009 antes de T024).
- US1 só depende da fase 2 (T032 também de T001 e T020); T027 pode começar cedo para a aprovação não atrasar o resto.
- T019 e T032 mexem em `MainActivity.kt`: fazer em sequência.
- T025 depende de T016 e T017.

## Parallel Examples

- Fase 2: T003 ‖ T005 (depois T004 → T006).
- US2: T007 ‖ T008 ‖ T009 ‖ T010 ‖ T011 → T012 → T013.
- US3: T014 ‖ T015 ‖ T016 ‖ T017 ‖ T022 → T018 → T019 → T023 → T024 → T025.
- US1: T027 enquanto US2/US3 andam; após T028: T029 ‖ T033 → T030, T031, T032, T034 → T035.

## Implementation Strategy

1. **MVP** = fases 1–3: paleta da marca no tema claro, contraste garantido por teste. Commit.
2. **+US3**: tema escuro e escolha em Configurações, backup v2. Commit.
3. **+US1**: depois da aprovação do símbolo — ícone, abertura, logo. Commit.
4. Polish e validação final.
