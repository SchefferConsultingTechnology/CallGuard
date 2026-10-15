# Implementation Plan: Navegação por Abas

**Branch**: `007-tab-navigation` | **Date**: 2026-10-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/007-tab-navigation/spec.md`

## Summary

Trocar as duas rolagens longas (Home e Configurações) por uma barra inferior com 4 abas: Início,
Whitelist, Histórico e Ajustes.

- **Navegação**: continua um único `NavHost`.
  - As abas são destinos de nível superior, e Ajustes é um grafo aninhado (lista + páginas de
    categoria).
  - A troca de aba usa `saveState`/`restoreState`, o que preserva o estado e faz o voltar ir sempre
    ao Início.
  - As telas de tarefa continuam empilhadas sem a barra e voltam com `popBackStack`.
- **Início**: vira painel com:
  - proteção;
  - whitelist e plano lado a lado;
  - as 3 últimas chamadas, com a formatação extraída do Histórico para uma função comum.
- **Ajustes**: vira uma lista de 6 categorias com resumos calculados por uma função pura. Os cards
  atuais são redistribuídos em páginas, que compartilham um único `SettingsViewModel`.

## Technical Context

**Language/Version**: Kotlin 2.3.20

**Primary Dependencies**: Compose BOM (Material 3: `NavigationBar`, `NavigationBarItem`), Navigation
Compose 2.9.7 e material-icons-extended, todos já no projeto. **Nenhuma dependência nova.**

**Storage**: sem mudança (Room, DataStore e o formato do backup ficam como estão).

**Testing**: JUnit 4 na JVM (`AppTabTest`, `SettingsSummariesTest`, `CallLogDisplayTest`) e validação
manual no emulador ([quickstart.md](quickstart.md)).

**Target Platform**: Android, `minSdk 26` e `targetSdk 36`.

**Project Type**: mobile-app (Android nativo, módulo único `app`)

**Performance Goals**: troca de aba instantânea, sem recarregar dados. Os ViewModels das abas
sobrevivem na pilha salva.

**Constraints**:
- O Início e a lista de Ajustes cabem sem rolar em 6,1" (1080×2400 a 420 dpi), nos 3 idiomas.
- Nenhuma função atual pode ser perdida (SC-005).

**Scale/Scope**:
- 4 abas, 6 páginas de categoria.
- 25 strings novas × 3 idiomas.
- Telas mexidas: Home, Whitelist, Histórico, Configurações e a navegação.

## Constitution Check

| Princípio | Avaliação |
|---|---|
| I. Fail-Open | ✅ Só UI. Triagem, motor e serviço não mudam. |
| II. Privacidade Local-First | ✅ Nenhum dado novo, nenhuma permissão, nenhuma rede. |
| III. Identidade Canônica | ✅ O Início exibe números com a **mesma** regra do Histórico, por causa da extração de `CallLogDisplay` (R5), sem segunda implementação. |
| IV. Motor Puro e Testado | ✅ Motor intocado. A lógica nova (rota → aba, resumos, exibição) é pura e testada na JVM. Schema Room inalterado. |
| V. Paridade de Localização | ✅ Strings novas nos 3 idiomas, listadas nos contratos. |
| VI. Monetização Honesta | ✅ A oferta continua dispensável e sem recurso novo anunciado. O card de plano do Início só leva à oferta. |
| Restrições técnicas | ✅ Nenhuma dependência nova. Camadas preservadas: `CallLogDisplay` em `domain/phone`, sem depender de `ui`. |

**Pós-design**: reavaliado após a Fase 1, sem violações.

## Project Structure

### Documentation (this feature)

```text
specs/007-tab-navigation/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── navigation-map.md
│   ├── home-dashboard.md
│   └── settings-categories.md
├── checklists/requirements.md
└── tasks.md                     # /speckit-tasks
```

### Source Code (repository root)

```text
app/src/main/java/com/lsp/callguard/
├── CallGuardApp.kt                          # usa CallGuardAppShell
├── domain/phone/CallLogDisplay.kt           # NOVO: canonicalOf / displayPhoneOf (extraído do histórico)
├── ui/navigation/
│   ├── AppTab.kt                            # NOVO: enum + tabFor(route)
│   ├── CallGuardAppShell.kt                 # NOVO: Scaffold + NavigationBar + TabReselection
│   ├── CallGuardNavHost.kt                  # grafo com abas, settings_graph, retornos por popBackStack
│   └── Routes.kt                            # SettingsGraph, SettingsCategoryPage
├── ui/screen/home/{HomeScreen,HomeRoute,HomeViewModel,HomeViewModelFactory}.kt  # painel + recentes
├── ui/screen/history/{CallHistoryScreen,CallHistoryRoute,CallHistoryViewModel}.kt  # sem seta; usa CallLogDisplay; reseleção
├── ui/screen/whitelist/{WhitelistScreen,WhitelistRoute}.kt                        # sem seta; reseleção
└── ui/screen/settings/
    ├── SettingsCategory.kt                  # NOVO
    ├── SettingsSummaries.kt                 # NOVO (puro)
    ├── SettingsListScreen.kt                # NOVO: lista + rodapé versão
    ├── SettingsCategoryScreen.kt            # NOVO: cabeçalho + conteúdo por categoria
    ├── SettingsRoute.kt                     # dividido: lista / página; lançadores e snackbars por página
    ├── SettingsScreen.kt                    # mantém os cards (internal) reaproveitados pelas páginas
    ├── SettingsViewModel.kt                 # + language; escopo settings_graph
    └── {BackupSection,AppearanceSection}.kt # inalterados

app/src/main/res/values{,-pt-rBR,-es-rES}/strings.xml   # 25 strings

app/src/test/java/com/lsp/callguard/
├── ui/navigation/AppTabTest.kt
├── ui/screen/settings/SettingsSummariesTest.kt
└── domain/phone/CallLogDisplayTest.kt
```

**Structure Decision**: segue as camadas e pastas atuais. As peças novas de navegação ficam em
`ui/navigation`, e as de Ajustes em `ui/screen/settings`. A exibição de números vai para
`domain/phone`, ao lado do `PhoneNumberDisplayFormatter`.

## Ordem de execução sugerida

1. **Base**:
   - `AppTab` + teste;
   - `CallLogDisplay` + teste (refatorando o Histórico sem mudar o comportamento);
   - strings.
2. **US1**: casca com a barra, grafo com abas e `settings_graph` (com a tela de Configurações atual
   como lista provisória), remoção das setas, reseleção e retornos.
3. **US2**: painel do Início.
4. **US3**: categorias, resumos e páginas. Divisão do `SettingsRoute`.
5. **US4**: retornos das tarefas (consentimento/importação, idioma) e validação completa
   ([quickstart.md](quickstart.md)).

## Complexity Tracking

Sem violações a justificar.
