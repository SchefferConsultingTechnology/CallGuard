# Research: Navegação por Abas

**Feature**: [spec.md](spec.md) · **Plan**: [plan.md](plan.md)

## Situação atual (levantamento)

- `CallGuardNavHost`: um único `NavHost` com 13 rotas planas (`Routes`). A Home navega para Whitelist,
  Histórico, Configurações etc. com `navigate(...)` simples. Todas as telas empilham e têm seta de voltar.
- O primeiro uso termina com `navigate(Home) { popUpTo(graph) { inclusive = true } }`.
- `ContactsImportRoute.onFinished` navega para Home (não volta para a origem).
- `SettingsRoute` concentra:
  - os lançadores de arquivo e os diálogos do backup;
  - as mensagens de restaurar compras;
  - o tema;
  - os toggles.
  Tudo fica numa `LazyColumn` com 7 seções.
- `HomeViewModel` não lê o histórico. Quem formata números (`formatter.formatOrRaw`) e interpreta
  registros antigos (`canonicalOf`) é o `CallHistoryViewModel`.
- 21 pontos usam `navigationBarsPadding`/`statusBarsPadding`/`contentWindowInsets` (edge-to-edge da 006).

## R1 — Estrutura de navegação

- **Decision**: manter **um** `NavHost`. As 4 abas são destinos de nível superior e Ajustes é um grafo
  aninhado:

  ```text
  root
  ├── home                          (aba Início, startDestination após o primeiro uso)
  ├── whitelist                     (aba)
  ├── call_history                  (aba)
  ├── settings_graph                (aba Ajustes, grafo aninhado)
  │   ├── settings                  (lista de categorias)
  │   └── settings/{category}       (página da categoria)
  └── tarefas: onboarding, paywall?firstRun=…, protection_setup, contacts_consent,
      contacts_import, terms, privacy_policy, language
  ```

  Trocar de aba usa o padrão oficial do Navigation Compose:
  `navigate(tab.route) { popUpTo(home) { saveState = true }; launchSingleTop = true; restoreState = true }`.
- **Rationale**:
  - Com `saveState`/`restoreState`, cada aba guarda a própria pilha e o estado salvo, incluindo a
    posição de rolagem (FR-002) e a página aberta em Ajustes.
  - Como `popUpTo(home)` deixa sempre o Início na base, o voltar de qualquer aba vai para o Início, e
    no Início fecha o app (FR-003), sem código extra.
  - As telas de tarefa continuam rotas comuns empilhadas sobre a aba atual. O `popBackStack()` devolve
    a pessoa à origem (FR-011).
- **Alternatives**:
  - Um `NavHost` por aba: duplicaria as telas de tarefa e complicaria o retorno.
  - Abas como estado local, sem navegação: perderia o voltar do sistema e o estado por aba.

## R1b — Transições

- **Decision**:
  - Entre duas abas: sem animação (`EnterTransition.None`/`ExitTransition.None`), decidido por `tabFor`
    nas rotas de origem e destino.
  - Telas de tarefa: fade de 150 ms.
- **Rationale**: o `NavHost` aplica por padrão um fade de cerca de 700 ms em toda navegação. Na troca
  de aba isso parece lento e dá a impressão de "nova tela empilhada", o que a spec quer evitar (US1-2).

## R2 — Quando mostrar a barra

- **Decision**: função pura `tabFor(route: String?): AppTab?` (`ui/navigation/AppTab.kt`) que devolve a
  aba de uma rota (`home`, `whitelist`, `call_history`, `settings`, `settings/{category}`) ou `null`
  para telas de tarefa. A barra aparece quando o resultado não é `null`. A aba marcada vem da mesma
  função.
- **Rationale**: uma regra só, testável na JVM (`AppTabTest`). Nenhuma tela de tarefa pode mostrar a
  barra por engano (US4-4).

## R3 — Toque na aba já selecionada (FR-004)

- **Decision**: `TabReselection`, um `MutableSharedFlow<AppTab>` criado na casca e entregue às telas
  das abas. Ao tocar na aba atual:
  - Ajustes numa página de categoria: `popBackStack("settings", inclusive = false)`.
  - Nos outros casos: emite a aba, e a tela que coleta faz `animateScrollToItem(0)` no seu
    `LazyListState`.
- **Rationale**: é o padrão do Android e mantém cada tela dona da sua rolagem.

## R4 — Casca (Scaffold) e espaços das barras do sistema

- **Decision**: `CallGuardAppShell`, um `Scaffold` externo com `bottomBar = NavigationBar` (visível
  segundo R2) e `contentWindowInsets = WindowInsets(0)`. O `NavHost` recebe
  `Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)`.
  - Quando a barra aparece, ela mesma trata a área do gesto de navegação. O `consumeWindowInsets`
    zera os `navigationBarsPadding()` das telas, evitando espaço duplo.
  - Quando a barra está oculta (telas de tarefa), `innerPadding` é zero e as telas seguem como hoje.
- **Rationale**: as telas internas já cuidam da barra de status (Scaffold próprio). Assim, a ação
  "Adicionar número" e as snackbars ficam acima da barra (FR-012) sem mexer em cada tela.
- **Verificação**: capturas com e sem barra, nos dois temas, conferindo que nada fica atrás da barra
  ou do gesto de navegação.

## R5 — Painel do Início (US2)

- **Decision**:
  - `HomeViewModel` passa a observar `CallDecisionLogRepository.observeLatestLogs()` e guarda
    `recentCalls: List<RecentCall>` com as 3 mais recentes já formatadas.
  - A formatação dos números sai de `CallHistoryViewModel` para uma função pura compartilhada,
    `domain/phone/CallLogDisplay.kt`: `displayPhoneOf(log, region, normalizer, formatter)`. Assim o
    Início e o Histórico mostram o mesmo número.
  - Layout do Início:
    - aviso de rebaixamento (quando houver);
    - card de proteção;
    - linha com 2 cards compactos (whitelist "3 de 5" + barra; plano "Grátis");
    - card "Últimas chamadas" com até 3 linhas + "Ver tudo".
  - Cabeçalho "CallGuard" + Configurações sai: a engrenagem perde sentido com a aba Ajustes.
  - Saem também `LockedContactsCard`, `PrivacyCard` e `CallHistoryCard`.
- **Rationale**: evita duplicar a regra de exibição (feature 001). Os itens somam cerca de 560 dp, o
  que cabe sem rolar em 6,1" (≈ 780 dp úteis acima da barra).
- **Alternatives**: repetir o `CallHistoryViewModel` dentro da Home (dois ViewModels de histórico,
  formatação duplicada).

## R6 — Ajustes por categoria (US3)

- **Decision**:
  - `SettingsCategory`: enum com `PLAN`, `PROTECTION`, `CONTACTS`, `BACKUP`, `APPEARANCE`, `PRIVACY`,
    cada uma com rota `settings/{key}`, ícone e título.
  - Um **único `SettingsViewModel`** com escopo no `settings_graph`
    (`navController.getBackStackEntry("settings_graph")`): a lista e as páginas compartilham o estado
    e os resumos atualizam ao voltar (US3-4).
  - Os resumos vêm de uma função pura `settingsSummaries(state): Map<SettingsCategory, Summary>`
    (`ui/screen/settings/SettingsSummaries.kt`). `Summary` é um id de string com argumentos, testável
    na JVM.
  - `SettingsUiState` ganha `language: AppLanguage?` para o resumo "Escuro · Português".
  - Os cards atuais vão para páginas:

    | Página | Cards / responsabilidades movidos de `SettingsScreen`/`SettingsRoute` |
    |---|---|
    | Plano e compras | `LicenseCard` (plano, Ver planos, restaurar compras + snackbars, benefícios, seletor debug) |
    | Proteção de chamadas | `ProtectionSettingsCard` |
    | Contatos | `ContactsSettingsCard` |
    | Cópia de segurança | `BackupCard` + lançadores de arquivo + diálogos + snackbars do backup |
    | Aparência e idioma | `AppearanceCard` + linha Idioma (abre `language`) |
    | Privacidade e termos | `PrivacySettingsCard` (privacidade, "sem anúncios", termos, política) |

  - `AppSettingsCard` (idioma + "sem anúncios") é desfeito: o idioma vai para Aparência e o "sem
    anúncios" já está em Privacidade (FR-010). `AppInfoCard` vira o rodapé da lista.
- **Rationale**: reaproveita os cards alinhados na 006 e só redistribui. Um ViewModel evita estados
  divergentes entre a lista e as páginas.

## R7 — Telas de tarefa e retorno (US4)

- **Decision**:
  - `ContactsImportRoute.onFinished` → `popBackStack()`. O consentimento já se remove da pilha ao
    abrir a importação, então volta para Ajustes → Contatos.
  - Paywall com `firstRun = false` → `popBackStack()` (já é assim).
  - Primeiro uso concluído → `navigate("home") { popUpTo(graph) { inclusive = true } }` (já é assim). A
    barra aparece porque `home` é aba.
  - A inclusão pendente do histórico (004) continua funcionando: a oferta é empilhada sobre a aba
    Histórico, e o `CallHistoryViewModel` sobrevive porque a entrada não sai da pilha.
- **Rationale**: as mudanças são mínimas e cada retorno passa a ser só `popBackStack`.

## R8 — Reabrir o app (edge case da spec)

- **Decision**: abrir pelo ícone (tarefa nova) começa no Início. Se o sistema encerrou o processo com o
  app em segundo plano e a pessoa volta pelos recentes, o Android restaura a última aba. Esse é o
  comportamento padrão e esperado, e não será forçado o Início nesse caso.
- **Rationale**: forçar o Início descartaria o que a pessoa estava fazendo (ex.: no meio de um backup).
  A spec foi ajustada para refletir isso.

## R9 — Rótulos com fonte grande

- **Decision**: `NavigationBarItem(alwaysShowLabel = true)` com `maxLines = 1` e
  `overflow = TextOverflow.Ellipsis`. Se em 200% algum rótulo for cortado, usar
  `alwaysShowLabel = false` só quando `LocalDensity.current.fontScale > 1.5` (só a aba atual mostra o
  nome). O `contentDescription` mantém o nome para leitores de tela.
- **Verificação**: quickstart com fonte 200% nos 3 idiomas ("Whitelist", "Histórico", "Historial").

## R10 — Testes

- **JVM**:
  - `AppTabTest`: mapeamento rota → aba e telas de tarefa → `null`.
  - `SettingsSummariesTest`: resumo de cada categoria para os estados relevantes (Grátis/Plus,
    combinações de bloqueio, contatos ligado/desligado/indisponível, tema × idioma).
  - `CallLogDisplayTest`: mesmos casos que hoje o Histórico cobre (canônico, legado sem canônico, oculto).
- **Manual** ([quickstart.md](quickstart.md)): navegação, retorno das tarefas, rolagem preservada,
  voltar, Início sem rolagem nos 3 idiomas e com fonte grande, temas, e regressões das features 001–006.
- Sem teste instrumentado de UI: o projeto não usa (só migração Room). A lógica decisória está nas
  funções puras acima.
