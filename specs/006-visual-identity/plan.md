# Implementation Plan: Identidade Visual e Temas Claro/Escuro

**Branch**: `006-visual-identity` | **Date**: 2026-10-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-visual-identity/spec.md`

## Summary

Levar a logo escolhida (variação "Azul Confiança") ao app e criar o tema claro e escuro.

- **Ícone**: adaptive icon vetorial (símbolo com gradiente sobre navy), mais uma versão monocromática
  para ícones temáticos.
- **Abertura**: tela de abertura com o símbolo, sem flash branco.
- **Apresentação**: logo completa e tagline no topo do primeiro uso.
- **Paleta**: valores puros com contraste WCAG AA verificado por teste JVM, mapeados para todos os
  papéis do Material 3, mais cores estendidas de sucesso e aviso.
- **Escolha do tema**: Sistema / Claro / Escuro em Configurações, aplicada pelo modo noturno do
  AppCompat (o mesmo mecanismo já usado para o idioma) e incluída no backup (formato v2).

As telas já usam só `MaterialTheme.colorScheme`. Por isso a troca de paleta alcança o app inteiro.
O ajuste por tela fica restrito a três pontos:
- cores de estado no histórico;
- círculos de ícone que hoje usam `primary.copy(alpha)`;
- avisos, que passam a usar a cor de aviso.

## Technical Context

**Language/Version**: Kotlin 2.3.20

**Primary Dependencies**:
- Compose BOM com Material 3 e AppCompat 1.7.0, que já existem no projeto.
- **Nova**: `androidx.core:core-splashscreen`, para a tela de abertura; ver R6 e o Complexity Tracking.

**Storage**:
- DataStore `app_preferences` ganha a nova chave `theme_mode`.
- Arquivo de backup sobe para o formato v2.
- Room: sem mudança.

**Testing**: JUnit 4 na JVM (`testDebugUnitTest`) e validação manual no emulador ([quickstart.md](quickstart.md)).

**Target Platform**: Android, `minSdk 26` e `targetSdk 36`.
- A persistência do modo noturno pelo sistema vale a partir do API 31.
- Ícones temáticos funcionam a partir do API 33.

**Project Type**: mobile-app (Android nativo, um módulo `app`)

**Performance Goals**:
- A troca de tema fica visível em menos de 1 s (SC-003).
- A leitura do tema na abertura leva no máximo 300 ms, com `SYSTEM` como alternativa se demorar mais.

**Constraints**:
- Nenhuma leitura de preferência no `Application.onCreate`, porque o processo também sobe pelo
  serviço de triagem (Princípio I).
- Sem cores dinâmicas do Material You.

**Scale/Scope**:
- 12 telas e os diálogos em 2 temas.
- 6 strings novas × 3 idiomas.
- Recursos do ícone em 1 SVG de origem.

## Constitution Check

| Princípio | Avaliação |
|---|---|
| I. Fail-Open | ✅ O tema não toca na triagem. A preferência é lida só na `MainActivity`, nunca no `Application`, que o serviço de triagem também inicia. |
| II. Privacidade Local-First | ✅ O tema é preferência local. O backup continua no arquivo criptografado e no Drive do usuário. Nenhuma permissão nova e nenhuma rede. |
| III. Identidade Canônica | ➖ Não se aplica. |
| IV. Motor Puro e Testado | ✅ Sem mudança no motor. O schema Room não muda. `ThemeMode`, a paleta (contraste) e o codec v2 são testados na JVM. |
| V. Paridade de Localização | ✅ 6 strings novas nos 3 idiomas, listadas em [contracts/appearance-settings.md](contracts/appearance-settings.md). "CallGuard" vem de `app_name`. |
| VI. Monetização Honesta | ✅ Temas disponíveis em todos os planos. Nenhuma mudança no paywall além das cores. |
| Restrições técnicas | ⚠️ Uma dependência nova (`core-splashscreen`), justificada abaixo. Versão centralizada em `libs.versions.toml`. |

**Pós-design**: reavaliado após a Fase 1, sem violações novas.

## Project Structure

### Documentation (this feature)

```text
specs/006-visual-identity/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── color-roles.md
│   ├── appearance-settings.md
│   └── backup-format-v2.md
├── assets/
│   ├── logo-board.png            # prancha escolhida (referência)
│   ├── callguard-symbol.svg      # símbolo redesenhado (fonte do ícone) — a criar, com aprovação
│   └── play-icon-512.png         # exportado do SVG para a ficha da Play
├── checklists/requirements.md
└── tasks.md                      # /speckit-tasks
```

### Source Code (repository root)

```text
app/src/main/java/com/lsp/callguard/
├── MainActivity.kt                         # installSplashScreen, enableEdgeToEdge, aplica tema antes de super.onCreate
├── core/theme/
│   ├── CallGuardPalette.kt                 # NOVO: valores ARGB puros, Light/Dark
│   ├── ExtendedColors.kt                   # NOVO: success/warning + CompositionLocal
│   ├── ThemeModeApplier.kt                 # NOVO: ThemeMode → AppCompatDelegate / UiModeManager
│   ├── Theme.kt                            # light/darkColorScheme completos, darkTheme param
│   └── Color.kt                            # REMOVIDO (substituído pela paleta)
├── domain/model/ThemeMode.kt               # NOVO
├── domain/backup/{BackupData,BackupCodec}.kt   # v2: themeMode
├── data/local/preferences/AppearancePreferences.kt  # NOVO
├── data/backup/BackupRepository.kt         # exporta/aplica themeMode
├── ui/components/BrandLogo.kt              # NOVO: símbolo + wordmark + tagline
├── ui/screen/onboarding/OnboardingScreen.kt    # BrandLogo no topo
├── ui/screen/settings/{SettingsScreen,SettingsViewModel,SettingsViewModelFactory,AppearanceSection}.kt
├── ui/screen/history/CallHistoryScreen.kt  # cores de estado
└── ui/screen/** (revisão)                  # primary.copy(alpha) → primaryContainer; avisos → warning

app/src/main/res/
├── drawable/ic_launcher_{foreground,background,monochrome}.xml, ic_brand_symbol.xml
├── mipmap-anydpi/ic_launcher{,_round}.xml  # monochrome próprio
├── mipmap-*dpi/*.webp                      # REMOVIDOS (não usados com minSdk 26)
├── values/{themes,colors}.xml              # DayNight + Theme.CallGuard.Starting; cores da marca
├── values-night/{themes,colors}.xml        # NOVO
└── values{,-pt-rBR,-es-rES}/strings.xml    # 6 strings

app/src/test/java/com/lsp/callguard/
├── core/theme/CallGuardPaletteContrastTest.kt   # NOVO
├── domain/model/ThemeModeTest.kt                # NOVO
└── domain/backup/BackupCodecTest.kt             # + v1 fixture, v2 round-trip
```

**Structure Decision**: o módulo único `app` segue as camadas atuais (`ui → domain ← data`, com `core`
para o tema). `CallGuardPalette` fica em `core/theme` e não depende do Compose, para que o teste JVM
o leia direto.

## Ordem de execução sugerida

1. **Paleta e tema** (US2, base de tudo):
   - Escrever a paleta, o tema claro e escuro completos e o teste de contraste.
   - Revisar as telas no claro.
2. **Escolha de tema** (US3):
   - `ThemeMode`, preferência, aplicação na abertura e card "Aparência".
   - Barras do sistema e `values-night`.
3. **Marca** (US1):
   - SVG do símbolo, enviado **para aprovação**.
   - Depois da aprovação: ícone, monocromático, abertura e `BrandLogo` na apresentação.
4. **Backup v2** e validação completa ([quickstart.md](quickstart.md)).

O passo 3 depende da aprovação do desenho. Por isso os passos 1 e 2 vêm antes: podem ser entregues e
testados com o ícone atual.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| Nova dependência `androidx.core:core-splashscreen` | Abertura com o símbolo e fundo do tema em todas as versões (API 26–36). Sem ela, o API 26–30 mostra tela branca e o 31+ mostra o fundo padrão do sistema (flash branco no escuro, US1-4). | Atributos `windowSplashScreen*` só em `values-v31` deixam o API 26–30 sem abertura. Biblioteca AndroidX oficial, pequena e sem permissões. |
