# Research: Identidade Visual e Temas Claro/Escuro

**Feature**: [spec.md](spec.md) · **Plan**: [plan.md](plan.md)

## Situação atual (levantamento)

- `core/theme/Theme.kt` define só `lightColorScheme` com 8 papéis; `Color.kt` tem 8 cores soltas.
- As telas não usam cores fixas: todas passam por `MaterialTheme.colorScheme` (primary 52×,
  onSurfaceVariant 48×, outlineVariant 23×, onSurface 23×, surface 19×, background 12×…). Trocar o
  esquema muda o app inteiro sem tocar nas telas, exceto onde falta um papel.
- **Lacuna**: o histórico pinta permitido e bloqueado com o mesmo `primary` (só o ícone
  Verified/Block muda). Não existem papéis de sucesso/aviso.
- `res/values/themes.xml`: `Theme.AppCompat.Light.NoActionBar`, sem variante noturna; `colors.xml`
  ainda tem as cores do template (purple/teal), sem uso.
- Ícone: adaptive icon padrão do template (`ic_launcher_foreground/background.xml`) + `.webp`
  legados em `mipmap-*dpi`. Com `minSdk 26`, todo aparelho usa o adaptive icon; os `.webp` não são
  usados.
- Sem `enableEdgeToEdge()`. Com `targetSdk 36`, o Android 15+ já força edge-to-edge; a cor dos
  ícones da barra de status vem do tema XML claro, o que quebraria no tema escuro.
- `CallGuardApplication.onCreate` também roda quando o **serviço de triagem** acorda o processo.

## R1 — Onde vive o esquema de cores

- **Decision**: paleta em `core/theme/CallGuardPalette.kt` como valores ARGB `Long` puros (sem
  Compose), um objeto para claro e outro para escuro. `Theme.kt` converte para `lightColorScheme` /
  `darkColorScheme` preenchendo **todos** os papéis do Material 3 usados pelos componentes.
  Papéis que o Material 3 não tem (sucesso, aviso) vão num `CallGuardExtendedColors` exposto por
  `CompositionLocal` (`CallGuardTheme.extendedColors`).
- **Rationale**: valores puros permitem um teste JVM que calcula o contraste WCAG de cada par
  texto/fundo (SC-001) sem emulador. Preencher todos os papéis evita que um componente (diálogo,
  snackbar, switch, menu) caia no roxo padrão do Material.
- **Alternatives**: cores em `colors.xml` (não testáveis na JVM, duplicariam o tema); só
  `colorScheme` sem cores estendidas (bloqueado/permitido continuariam iguais, falha FR-008).

## R2 — Mapeamento papéis da spec → Material 3

| Papel da spec | Material 3 | Claro | Escuro |
|---|---|---|---|
| Fundo da tela | `background`, `surfaceContainerLowest`* | `#F5F7FB` | `#071426` |
| Card / superfície | `surface`, `surfaceContainer*` | `#FFFFFF` | `#0F2747` |
| Superfície alternativa | `surfaceVariant` | `#EEF2F8` | `#16304F` |
| Borda do card | `outlineVariant` | `#DCE3EE` | `#1F3A5F` |
| Borda de controle (switch off, campo) | `outline` | `#7A879C` | `#6F84A3` |
| Texto principal | `onBackground`, `onSurface` | `#0B1F3B` | `#E6EDF7` |
| Texto secundário | `onSurfaceVariant` | `#4A5A75` | `#9DB0CC` |
| Botão primário / switch on / texto destacado / link | `primary` / `onPrimary` | `#2563EB` / `#FFFFFF` | `#5B9BFA` / `#0B1F3B` |
| Botão tonal | `secondaryContainer` / `onSecondaryContainer` | `#DBE6FE` / `#0B1F3B` | `#1E3A66` / `#DBE6FE` |
| Destaque (chip, ícone em círculo) | `primaryContainer` / `onPrimaryContainer` | `#DBE6FE` / `#0B1F3B` | `#1E3A66` / `#DBE6FE` |
| Acento secundário | `secondary` = `primary`; `tertiary` = ciano `#22D3EE` só decorativo | — | — |
| Bloqueado (erro) | `error` · `errorContainer` / `onErrorContainer` | `#DC2626` · `#FEE2E2` / `#991B1B` | `#F87171` · `#3B1219` / `#FECACA` |
| Permitido (sucesso) | `extended.success` · `successContainer` / `onSuccessContainer` | `#15803D` · `#DCFCE7` / `#14532D` | `#4ADE80` · `#0E2E1D` / `#BBF7D0` |
| Aviso | `extended.warning` · `warningContainer` / `onWarningContainer` | `#B45309` · `#FEF3C7` / `#78350F` | `#FBBF24` · `#33260A` / `#FDE68A` |
| Diálogo / menu / snackbar | `surfaceContainerHigh`, `inverseSurface` | `#FFFFFF`, `#0B1F3B` | `#132C4F`, `#E6EDF7` |

\* Os `surfaceContainer*` recebem o valor de `surface` (cards planos) para que diálogos e menus não
ganhem a tonalidade rosada/cinza padrão.

- Desabilitado: o Material 3 já desenha `onSurface` a 38% sobre `onSurface` a 12% — mantido.
- **Primário no tema escuro**: `#2563EB` sobre o card escuro `#0F2747` dá só 2,9:1 e sobre diálogos
  2,7:1 — ilegível como texto/ícone. Por isso o primário escuro é `#5B9BFA` (card 5,4 · diálogo 5,0 ·
  fundo 6,6) com texto do botão em navy `#0B1F3B` (5,9), padrão do Material 3 para tema escuro.
  Com isso, as ~50 ocorrências de `colorScheme.primary` como texto, link ou ícone continuam válidas
  nos dois temas sem trocar papel nas telas.
- **Texto sobre fundos tingidos** (chips, avisos, selos): usa sempre o `on…Container` do papel —
  vermelho `#DC2626` sobre `#FEE2E2` daria só 3,95:1.
- `primary.copy(alpha = 0.12f)` (círculo atrás de ícones) passa a `primaryContainer`, que tem
  contraste definido nos dois temas.

## R3 — Cores dinâmicas (Material You)

- **Decision**: não usar `dynamicLightColorScheme`/`dynamicDarkColorScheme` (FR-012).
- **Rationale**: identidade de marca reconhecível; contrastes verificados só valem para a paleta fixa.

## R4 — Aplicar a escolha de tema (claro/escuro/sistema)

- **Decision**:
  - `ThemeMode { SYSTEM, LIGHT, DARK }` no `domain/model`, guardado no DataStore
    (`theme_mode`, padrão `SYSTEM`) por `AppearancePreferences`.
  - A escolha é aplicada com `AppCompatDelegate.setDefaultNightMode(...)` abaixo do Android 12; no
    Android 12+ (API 31), **só** com `UiModeManager.setApplicationNightMode(...)`, que o sistema guarda
    e usa na tela de abertura (chamar os dois recriaria a tela duas vezes).
  - `CallGuardTheme(darkTheme = isSystemInDarkTheme())`: com o AppCompat ajustando a configuração,
    `isSystemInDarkTheme()` já reflete a escolha da pessoa.
  - Na abertura, `MainActivity` lê a preferência **antes** de `super.onCreate` (leitura curta do
    DataStore com limite de 300 ms; se falhar, `SYSTEM`) para não recriar a tela logo ao abrir.
- **Rationale**: o AppCompat já é usado para o idioma por app; o mesmo mecanismo cobre recursos XML
  (tela de abertura, `values-night`), o Compose e as barras do sistema. A troca recria a Activity,
  mas o estado salvo (rota da navegação, `rememberSaveable`) é restaurado — atende "sem reiniciar o
  app e sem perder o que estava na tela".
- **Não ler no `Application.onCreate`**: esse método também roda quando o serviço de triagem acorda
  o processo; bloqueio ali atrasaria a resposta da chamada (constituição I).
- **Alternatives**: só no Compose (sem recriar) — rejeitado: a tela de abertura, os diálogos do
  sistema e as barras não acompanhariam a escolha; `SharedPreferences` espelho para leitura
  síncrona — desnecessário com a leitura curta antes de `super.onCreate`.

## R5 — Barras do sistema

- **Decision**: chamar `enableEdgeToEdge()` em `MainActivity` (padrão `SystemBarStyle.auto`), que
  escolhe ícones claros/escuros conforme a configuração noturna. Conferir que as telas já tratam
  `WindowInsets` (Scaffold com `contentWindowInsets`); ajustar as que não tratam.
- **Rationale**: o Android 15+ já força edge-to-edge com `targetSdk 36`; tornar explícito unifica
  API 26–34 e resolve a cor dos ícones no tema escuro (FR-013).

## R6 — Tela de abertura (splash)

- **Decision**: `androidx.core:core-splashscreen` com `Theme.CallGuard.Starting`
  (`windowSplashScreenBackground` = fundo do tema, `windowSplashScreenAnimatedIcon` = símbolo direto
  sobre o fundo, sem círculo de fundo,
  `postSplashScreenTheme` = `Theme.CallGuard`), com `values-night`. `installSplashScreen()` em
  `MainActivity` mantém a abertura enquanto a rota inicial é decidida (`setKeepOnScreenCondition`),
  o que também elimina a tela em branco atual antes do `setContent`.
- **Rationale**: no Android 12+ o sistema sempre mostra uma abertura com o ícone; sem configurar,
  aparece o fundo branco padrão (flash branco no tema escuro, falha US1-4). A biblioteca dá o
  mesmo resultado no API 26–30. Dependência pequena do AndroidX, justificada no Complexity Tracking.
- **Alternatives**: só atributos `android:windowSplashScreen*` em `values-v31` — API 26–30 ficariam
  com tela em branco.

## R7 — Ícone do app

- **Decision**:
  - Redesenhar o símbolo como **SVG** próprio (`specs/006-visual-identity/assets/callguard-symbol.svg`):
    hexágono em fita formando o "G", gradiente `#2563EB → #22D3EE`, dentro da zona segura de 66 dp
    do adaptive icon (108 dp).
  - Converter para vector drawables: `ic_launcher_foreground.xml` (gradiente via `aapt:attr`,
    suportado no API 24+), `ic_launcher_background.xml` (navy `#0B1F3B`) e
    `ic_launcher_monochrome.xml` (silhueta sólida) para ícones temáticos (FR-002).
  - `ic_launcher_round` usa o mesmo adaptive icon; os `.webp` legados são removidos (nunca usados com
    `minSdk 26`).
  - O PNG 512×512 da ficha da Play é exportado do mesmo SVG e guardado em `assets/`; o envio à loja
    fica para a etapa da Play Console.
- **Rationale**: vetor não perde nitidez em nenhuma densidade e é editável; a prancha é imagem
  rasterizada (com sombras e brilho) e não pode ser usada como ícone.
- **Risco**: fidelidade do redesenho. Mitigação: **ponto de aprovação** — o SVG é mostrado à pessoa
  responsável (no app e ao lado da prancha) antes de gerar os recursos finais.
- **Alternatives**: recortar a prancha em PNG (borrado, com fundo e sombra embutidos); ferramenta
  Image Asset do Android Studio (exige SVG do mesmo jeito).

## R8 — Logo completa na apresentação

- **Decision**: componente `BrandLogo` em `ui/components/`: símbolo (`painterResource` do mesmo
  vetor do ícone, sem fundo) + wordmark em texto ("Call" em `onBackground`, "Guard" em `primary`:
  4,8:1 no claro e 6,6:1 no escuro) + tagline traduzida (`onSurfaceVariant`, caixa
  alta, espaçamento de letras). Substitui o ícone `PhoneLocked` do topo do `OnboardingScreen`.
- **Rationale**: texto real é traduzível, acessível a leitores de tela e acompanha o tema; um PNG
  da logo teria que existir em duas cores e três idiomas.
- O nome "CallGuard" vem de `app_name` (não traduzido); só a tagline é nova nos três idiomas.

## R9 — Tema no backup (FR-014)

- **Decision**: `BACKUP_FORMAT_VERSION = 2`. `BackupSettings` ganha `themeMode: String?`
  (`"system" | "light" | "dark"`), gravado **no fim** do arquivo, depois do histórico. Leitura de um
  arquivo v1 → `themeMode = null` (mantém a escolha atual do aparelho). Valor desconhecido → ignorado.
- **Rationale**: arquivos já exportados (v1) continuam importando; acrescentar no fim evita mudar
  a posição dos campos existentes.
- **Consequência**: um arquivo v2 importado num app antigo (v1) é recusado com "atualize o app" — é o
  comportamento já especificado na 005.

## R10 — Seletor na tela de Configurações

- **Decision**: novo card "Aparência" na seção "Aplicativo" de Configurações, logo após "Idioma",
  com três opções em `SingleChoiceSegmentedButtonRow` (Sistema · Claro · Escuro), aplicadas ao tocar.
  1 toque a partir das Configurações (SC-005: ≤ 3).
- **Rationale**: três opções curtas cabem numa linha; segue o padrão de "aplica na hora" decidido
  para o idioma (feature 002).
- **Alternatives**: tela separada como a de idioma (2 toques a mais, sem ganho); switch "modo
  escuro" (não cobre "seguir o sistema").

## R11 — Verificação

- **Teste JVM** `CallGuardPaletteContrastTest`: para cada par texto/fundo da tabela R2, nos dois
  temas, contraste ≥ 4,5 (ícones/bordas de controle ≥ 3,0). Falha se alguém mudar uma cor para um
  valor ilegível.
- **Teste JVM** `ThemeModeTest`: conversão de/para texto guardado; valor desconhecido → `SYSTEM`.
- **Teste JVM** `BackupCodecTest`: ida e volta v2 com tema; arquivo v1 ainda lido (tema `null`).
- **Manual**: capturas de todas as telas e diálogos nos dois temas (SC-004), ícone em 3 formatos de
  máscara (SC-002), ícone temático, abertura sem flash, troca de tema mantendo a tela.
