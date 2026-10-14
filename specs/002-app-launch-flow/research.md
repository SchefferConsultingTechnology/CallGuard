# Research: Fluxo de Abertura e Idioma

**Feature**: 002-app-launch-flow | **Date**: 2026-10-14

Base: leitura do código atual (`CallGuardNavHost`, `MainActivity`, `OnboardingScreen`,
`LanguageSelection*`, `PaywallRoute`) e da correção de idioma por app já aplicada
(`MainActivity : AppCompatActivity`, `LocaleManagerHelper`, `locales_config`).

## R1. Onde guardar o progresso do primeiro uso

- **Decision**: Nova chave `onboarding_progress` (string do enum `OnboardingProgress`:
  `NOT_STARTED`, `ONBOARDING_DONE`, `COMPLETED`) no DataStore de preferências já existente
  (`app_preferences`), encapsulada em `OnboardingPreferences`.
- **Rationale**: Mesmo mecanismo das demais preferências; local (Princípio II); um enum em vez de
  dois booleanos evita estados impossíveis e cobre "fechou o app na oferta" (US2-5).
- **Alternatives considered**: booleano `has_completed_onboarding` (não distingue a retomada na
  oferta); Room (excessivo para um valor).

## R2. Decidir a primeira tela sem "piscar" outra tela

- **Decision**: Resolver o destino inicial em `MainActivity.onCreate`, na coroutine que já
  existe antes de `setContent`, e passá-lo para `CallGuardNavHost(startDestination = …)`.
- **Rationale**: A `MainActivity` já adia `setContent` até ler o idioma salvo; resolver a rota no
  mesmo ponto evita compor uma tela errada e depois navegar. Sem dependência nova.
- **Alternatives considered**: navegar a partir de uma tela "splash" vazia (exibe frame extra e
  polui a pilha); `core-splashscreen` (dependência nova sem necessidade real — a leitura do
  DataStore leva milissegundos).

## R3. Usuários da versão anterior (FR-010)

- **Decision**: Função pura `resolveStartDestination(progress, legacy)`:
  - `progress == COMPLETED` → Home; `ONBOARDING_DONE` → Paywall (primeiro uso);
    `NOT_STARTED` → Onboarding;
  - `progress == null` (chave ausente) → se `legacy.hasSavedLanguage || legacy.hasWhitelistEntries
    || legacy.isLicensed` → `COMPLETED`/Home, senão `NOT_STARTED`/Onboarding.
  O resultado da derivação é **gravado** imediatamente, para que toques posteriores no idioma
  (que salvam `LanguagePreferences`) nunca sejam confundidos com sinal de versão antiga.
- **Rationale**: Sinais disponíveis sem migração de schema; a gravação imediata fecha a ambiguidade
  "instalação nova que já escolheu idioma no onboarding".
- **Alternatives considered**: comparar `versionCode` da instalação anterior (Android não expõe de
  forma confiável; `firstInstallTime == lastUpdateTime` falha em reinstalações).

## R4. Falha ao ler o estado (Edge Case)

- **Decision**: Qualquer exceção ao ler o progresso → tratar como `progress == null` (R3), sem
  gravar; se os sinais legados também falharem → Onboarding.
- **Rationale**: Nunca impede abrir o app; no pior caso a pessoa revê a apresentação uma vez.

## R5. Idioma inicial na apresentação (FR-005)

- **Decision**: `AppLanguage.forLocale(locale)` puro: idioma `pt` → `PT_BR`, `es` → `ES_ES`,
  `en` → `EN_US`, demais → `null`. Seleção inicial do onboarding =
  `LocaleManagerHelper.currentLanguage() ?: AppLanguage.forLocale(Locale.getDefault()) ?: EN_US`.
  Nada é aplicado/gravado até a pessoa tocar num idioma; sem toque, o app segue o idioma do
  sistema (que, para idiomas não suportados, já cai nos textos em inglês da pasta padrão).
  O botão "Continuar" fica sempre habilitado.
- **Rationale**: Mostra o idioma real da tela marcado, sem forçar per-app locale em quem não
  escolheu nada.
- **Alternatives considered**: aplicar automaticamente o idioma do aparelho (gravaria uma escolha
  que a pessoa não fez e "congelaria" o idioma se ela mudar o do sistema depois).

## R6. Paywall em dois contextos (FR-008, FR-009)

- **Decision**: Uma rota com argumento opcional: `paywall?firstRun={firstRun}` (default
  `false`).
  - `firstRun = true` (vindo da apresentação ou como tela inicial com `ONBOARDING_DONE`):
    "continuar grátis" ou compra concluída → grava `COMPLETED` e navega para Home com
    `popUpTo(graph) { inclusive = true }`.
  - `firstRun = false` (atalhos internos): "continuar grátis" ou compra → `popBackStack()`.
  - A apresentação navega para o paywall **sem** remover a si mesma (voltar retorna a ela —
    US2-6).
- **Rationale**: Mantém uma única tela de oferta; elimina a Home duplicada.
- **Alternatives considered**: duas rotas distintas (duplica wiring); type-safe navigation
  (refactor maior que o escopo).

## R7. Tela de idioma só nas Configurações (FR-004, FR-006, FR-007)

- **Decision**: `LanguageSelectionViewModel.onLanguageSelected` passa a gravar
  (`LanguagePreferences`) e aplicar (`LocaleManagerHelper.applyLanguage`) na hora; `onConfirm`
  é removido. O botão inferior vira "Concluir" (`done`) e faz `popBackStack()`; o "voltar" do
  sistema faz o mesmo. O texto "Você pode alterar o idioma depois nas configurações" sai da tela
  (perdeu o sentido). A rota deixa de ser destino inicial.
- **Rationale**: Mesmo comportamento da apresentação; a seleção já marcada vem de
  `currentLanguage()` (feito na correção anterior).

## R8. Preservar a tela em recriações (FR-011)

- **Decision**: Confiar na restauração de estado do `NavController` (`rememberNavController` é
  salvo no `SavedStateRegistry`); `startDestination` recalculado na recriação não altera a pilha
  restaurada. Validar no quickstart trocando idioma na apresentação, na oferta e na tela de idioma.
- **Rationale**: Já funciona hoje para a troca de idioma na apresentação (verificado em
  2026-10-14).

## R9. Testes

- **Decision**: Unitários JVM para `resolveStartDestination` (todas as combinações),
  `AppLanguage.forLocale` e `OnboardingPreferences` (DataStore em arquivo temporário). Navegação
  validada ponta a ponta no emulador por script `adb`/`uiautomator` (quickstart), como na 001.
- **Rationale**: A lógica de decisão fica pura e coberta; o comportamento de pilha/recriação só é
  confiável em aparelho.
- **Alternatives considered**: testes instrumentados de Compose com `TestNavHostController`
  (custo alto de setup agora; podem vir depois).
