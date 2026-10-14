# Implementation Plan: Fluxo de Abertura e Idioma

**Branch**: `002-app-launch-flow` | **Date**: 2026-10-14 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/002-app-launch-flow/spec.md`

## Summary

Persistir o progresso do primeiro uso (`OnboardingProgress`) e decidir a tela inicial em
`MainActivity` antes de compor a UI, por uma função pura testável. O onboarding passa a ser a
primeira tela (com o idioma do aparelho pré-marcado); a tela isolada de idioma fica só em
Configurações, aplica ao tocar e volta. A oferta do Premium ganha o argumento `firstRun` para
concluir o primeiro uso ou simplesmente voltar à origem. Usuários da versão anterior são
detectados por sinais existentes (idioma salvo, whitelist, licença).

## Technical Context

**Language/Version**: Kotlin 2.3.20, JVM target 11

**Primary Dependencies**: Jetpack Compose (BOM 2026.03.01), Navigation Compose 2.9.7,
AppCompat 1.7 (per-app language), DataStore Preferences 1.1.7 — nenhuma dependência nova

**Storage**: DataStore `app_preferences` — nova chave `onboarding_progress`; Room inalterado (v2)

**Testing**: JUnit 4 + kotlinx-coroutines-test (JVM); validação de navegação ponta a ponta no
emulador via `adb`/`uiautomator`

**Target Platform**: Android minSdk 26, target/compileSdk 36; validação em emulador API 37

**Project Type**: mobile-app (módulo único `:app`)

**Performance Goals**: decisão da tela inicial sem frame intermediário visível (leitura de
DataStore antes do `setContent`, já existente)

**Constraints**: sem novas permissões, offline, 3 idiomas, nenhum usuário existente revê o
onboarding (SC-005)

**Scale/Scope**: ~8 arquivos de produção (navegação, MainActivity, onboarding, idioma, paywall,
preferências), 3 arquivos de teste, poucas strings

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Avaliação | Pós-design |
|---|---|---|
| I. Fail-Open | Não toca triagem/motor. | ✅ Sem impacto. |
| II. Privacidade | Nova preferência local, sem dado pessoal. | ✅ Mesma política de backup das demais (revisão na 004). |
| III. Identidade de números | Não se aplica. | ✅ — |
| IV. Motor puro e testado | Lógica de decisão da tela inicial isolada em função pura. | ✅ `resolveStartDestination`, `AppLanguage.forLocale`, `OnboardingPreferences` com testes JVM; sem mudança de schema. |
| V. Localização | String nova "Concluir"; texto obsoleto removido da tela de idioma. | ✅ 3 idiomas; validação nos 3 no quickstart. |
| VI. Monetização honesta | Oferta continua dispensável ("continuar grátis") e acessível depois. | ✅ Primeiro uso conclui com "continuar grátis" tanto quanto com compra. |
| Restrições técnicas | Sem dependência nova; camadas preservadas. | ✅ |
| Quality gates | Mudança não é em triagem/motor/normalizador → validação manual recomendada, não obrigatória. | ✅ Quickstart prevê validação no emulador mesmo assim. |

**Resultado**: PASS, sem violações.

## Project Structure

### Documentation (this feature)

```text
specs/002-app-launch-flow/
├── spec.md
├── plan.md              # este arquivo
├── research.md          # Fase 0
├── data-model.md        # Fase 1
├── quickstart.md        # Fase 1
├── contracts/
│   └── launch-navigation.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Fase 2 (/speckit-tasks)
```

### Source Code (repository root)

```text
app/src/main/java/com/lsp/callguard/
├── MainActivity.kt                                # resolve tela inicial antes do setContent
├── CallGuardApp.kt                                # repassa startDestination
├── core/language/AppLanguage.kt                   # + forLocale(Locale)
├── domain/model/OnboardingProgress.kt             # NOVO enum
├── data/local/preferences/OnboardingPreferences.kt  # NOVO (progress / advanceTo)
├── ui/navigation/
│   ├── StartDestination.kt                        # NOVO: LegacySignals, resolveStartDestination
│   ├── Routes.kt                                  # paywall?firstRun={firstRun}
│   └── CallGuardNavHost.kt                        # startDestination param; fluxos novos
└── ui/screen/
    ├── onboarding/OnboardingScreen.kt             # seleção inicial pelo idioma vigente/aparelho;
    │                                              # "Continuar" sempre habilitado
    ├── language/{LanguageSelectionScreen,LanguageSelectionViewModel}.kt
    │                                              # aplica ao tocar; botão "Concluir"; sem texto obsoleto
    └── paywall/PaywallRoute.kt                    # callbacks por contexto (firstRun)

app/src/main/res/values{,-pt-rBR,-es-rES}/strings.xml   # + done

app/src/test/java/com/lsp/callguard/
├── ui/navigation/StartDestinationTest.kt          # NOVO (tabela do contrato)
├── core/language/AppLanguageTest.kt               # NOVO (forLocale)
└── data/local/preferences/OnboardingPreferencesTest.kt  # NOVO (ausente, avanço, não-regressão)
```

**Structure Decision**: Módulo único `:app`, camadas por pacote (constituição). A decisão de
rota inicial é UI-level (`ui/navigation`) mas pura e testável; persistência em
`data/local/preferences`; o enum de progresso em `domain/model`.

## Fases de entrega

1. **Fundação**: `OnboardingProgress`, `OnboardingPreferences`, `resolveStartDestination`,
   `AppLanguage.forLocale` + testes; `MainActivity` resolve e passa a rota.
2. **US1 + US2 (P1)**: start destination dinâmico; onboarding como primeira tela; paywall
   `firstRun` conclui o primeiro uso; Home limpa a pilha.
3. **US3 (P2)**: tela de idioma aplica ao tocar e volta; botão "Concluir".
4. **US4 (P3)**: paywall `firstRun=false` volta à origem em todos os atalhos.
5. **Polimento**: strings, quickstart no emulador, gates.

## Riscos

| Risco | Mitigação |
|---|---|
| Usuário da versão anterior revê o onboarding | Derivação por 3 sinais + gravação imediata (R3); teste de atualização no quickstart §6. |
| Recriação por troca de idioma muda a tela | Restauração do `NavController`; validação em 3 telas (R8). |
| Instalação nova confundida com antiga após escolher idioma | Derivação gravada antes de qualquer toque (R3). |
| Paywall abre e fecha sozinho para quem já é Premium (`isPremiumActive`) | Comportamento existente; com `firstRun=false` resulta em voltar — aceitável, registrado. |

## Complexity Tracking

Sem violações da constituição a justificar.
