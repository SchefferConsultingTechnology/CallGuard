---

description: "Task list for 003-premium-plans"
---

# Tasks: Planos Plus e Pro

**Input**: Design documents from `specs/003-premium-plans/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/entitlements.md,
quickstart.md

**Tests**: INCLUÍDOS para as regras puras (nível, recursos, retenção, migração) e para o motor de
decisão (Princípio IV). Compra real validada com testadores de licença (quickstart §3).

## Format: `[ID] [P?] [Story] Description`

- Prefixos: `main/` = `app/src/main/java/com/lsp/callguard/`,
  `test/` = `app/src/test/java/com/lsp/callguard/`, `res/` = `app/src/main/res/`
- Gradle nesta máquina: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon
  -Porg.gradle.java.installations.auto-detect=false <tasks>`

---

## Phase 1: Setup

- [X] T001 Confirmar a linha de base: `./gradlew testDebugUnitTest assembleDebug` verde

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Nível no lugar da licença booleana, com comportamento Grátis/Plus equivalente ao
atual ao final da fase.

**⚠️ CRITICAL**: Nenhuma user story começa antes desta fase terminar.

- [X] T002 [P] Criar `enum class Tier { FREE, PLUS, PRO }` em `main/domain/model/Tier.kt`
  (ordem = hierarquia)
- [X] T003 [P] Criar `object PlanCatalog` em `main/domain/model/PlanCatalog.kt` com as constantes de
  `specs/003-premium-plans/contracts/entitlements.md` (`PLUS`, `PLUS_LIFETIME`, `PRO`,
  `BASE_MONTHLY`, `BASE_ANNUAL`, `OFFER_ANNUAL_TRIAL`, `PRO_ON_SALE = false`) e
  `fun tierOf(productId: String): Tier?`
- [X] T004 [P] Criar `test/domain/model/PlanFeaturesTest.kt` cobrindo a tabela de recursos do
  data-model para os 3 níveis
- [X] T005 Criar `data class PlanFeatures(whitelistLimit: Int?, contactsAllowed: Boolean, historyRetentionDays: Int, driveBackup: Boolean)`
  com `companion fun of(tier: Tier)` em `main/domain/model/PlanFeatures.kt`; remover
  `main/domain/model/PlanLimits.kt`. T004 deve passar
- [X] T006 [P] Criar `test/domain/billing/EntitlementResolverTest.kt` com as 10 linhas da tabela
  `resolveEntitlement` do contrato, mais `activeSubscriptionToken/Product` do nível mais alto
- [X] T007 Criar `main/domain/billing/PurchaseSnapshot.kt` (`productIds`, `state: PurchaseState { PURCHASED, PENDING, UNSPECIFIED }`,
  `isAutoRenewing`, `purchaseToken`) e `main/domain/billing/Entitlement.kt` (`Entitlement`,
  `EntitlementStatus { ACTIVE, CANCELED_ACTIVE, NONE }`, `fun resolveEntitlement(purchases)`).
  T006 deve passar
- [X] T008 [P] Criar `test/domain/billing/HistoryRetentionTest.kt` e
  `main/domain/billing/HistoryRetention.kt` com `fun historyCutoff(nowMillis: Long, tier: Tier): Long`
  (`now − PlanFeatures.of(tier).historyRetentionDays × 86_400_000`)
- [X] T009 Atualizar `main/domain/model/LicenseState.kt` para
  `LicenseState(tier: Tier = FREE, status: EntitlementStatus = NONE, hasPendingPurchase: Boolean = false)`
  com `val features get() = PlanFeatures.of(tier)` (remover `isLicensed` e `planName`)
- [X] T010 [P] Criar `test/data/local/preferences/LicensePreferencesTest.kt` (DataStore em
  `TemporaryFolder`): sem chaves → FREE/NONE; `is_licensed = true` legado → PLUS e chave legada
  removida; `save(entitlement)` → lido de volta; `lastSeenTier`/`setLastSeenTier`
- [X] T011 Reescrever `main/data/local/preferences/LicensePreferences.kt`: chaves `tier`,
  `plan_status`, `pending_purchase`, `last_seen_tier`; `licenseFlow` lê `tier` ou migra
  `is_licensed`/`plan_name` (remover as legadas no primeiro `save`); `suspend fun save(entitlement: Entitlement)`;
  `suspend fun setMockTier(tier: Tier)` (só chamado sob `BuildConfig.DEBUG`). T010 deve passar
- [X] T012 [P] Em `main/domain/engine/CallDecisionEngine.kt` trocar o parâmetro `isLicensed` por
  `contactsAllowed: Boolean` em `evaluate`/`shouldAllowCall`; atualizar
  `test/domain/engine/CallDecisionEngineTest.kt` (mesmos cenários, nomenclatura nova)
- [X] T013 Substituir todos os usos de `isLicensed`/`FREE_WHITELIST_LIMIT` pelo recurso
  equivalente de `license.features`:
  `main/telecom/screening/AppCallScreeningService.kt` (`contactsAllowed`),
  `main/ui/screen/whitelist/WhitelistViewModel.kt` (`limit = features.whitelistLimit`,
  `reachedLimit` quando limite não nulo e atingido), `main/ui/screen/whitelist/WhitelistScreen.kt`
  (remover a constante local `FREE_WHITELIST_LIMIT` e a classe não usada `WhitelistNumberUi`),
  `main/ui/screen/home/HomeViewModel.kt` e `main/ui/screen/home/HomeScreen.kt`,
  `main/ui/screen/settings/SettingsViewModel.kt` e `SettingsScreen.kt`,
  `main/ui/screen/consent/ContactsConsentViewModel.kt` (`contactsAllowed`),
  `main/MainActivity.kt` (`isLicensed = tier != FREE` nos sinais legados)
- [X] T014 Em `main/ui/screen/settings/SettingsScreen.kt`/`SettingsViewModel.kt` trocar o
  interruptor "modo premium fictício" (DEBUG) por um seletor Grátis/Plus/Pro chamando
  `setMockTier`
- [X] T015 Rodar `./gradlew testDebugUnitTest assembleDebug` — verde; comportamento Grátis/Plus
  igual ao atual

**Checkpoint**: modelo de níveis pronto e usado em todo o app.

---

## Phase 3: User Story 1 - Assinar o Plus e liberar os recursos (Priority: P1) 🎯 MVP

**Goal**: Plus mensal, anual e vitalício compráveis; recursos liberados na hora; restaurar compras.

**Independent Test**: com testador de licença, comprar as 3 opções do Plus (quickstart §3); sem
loja, seletor de nível em DEBUG (quickstart §2).

- [X] T016 [US1] Reescrever `main/data/billing/BillingRepository.kt`:
  `BillingClient` com `enableAutoServiceReconnection()` e `enablePendingPurchases` (one-time e
  prepaid); `queryProducts()` para `PLUS` (SUBS), `PLUS_LIFETIME` (INAPP) e `PRO` (SUBS, só se
  `PRO_ON_SALE`); expor `StateFlow<CatalogState>` (produtos ou indisponível);
  `refreshPurchases()` consultando SUBS **e** INAPP, convertendo para `PurchaseSnapshot`,
  chamando `resolveEntitlement` e `licensePreferences.save(...)` **somente** com `OK` em ambas;
  `onPurchasesUpdated` → reconhecer compras `PURCHASED` não reconhecidas e chamar
  `refreshPurchases()`; remover `PREMIUM_MONTHLY_PRODUCT_ID` e `isPremiumActive`
- [X] T017 [US1] Em `BillingRepository` adicionar
  `fun purchase(activity: Activity, option: PurchaseOption)` com
  `PurchaseOption(productId, basePlanId?, preferTrial)`: assinatura usa o `offerToken` da oferta
  `annual-trial-7d` quando `preferTrial` e disponível, senão o do plano-base; compra única usa
  `getOneTimePurchaseOfferDetailsList().first()`; sem troca de plano ainda (US4)
- [X] T018 [US1] Em `BillingRepository` adicionar `suspend fun restorePurchases(): RestoreResult`
  (refresh + resultado: restaurado nível X / nada encontrado / loja indisponível)
- [X] T019 [US1] Atualizar `main/ui/screen/paywall/PaywallViewModel.kt`,
  `PaywallViewModelFactory.kt` e `PaywallRoute.kt`: observar `licenseFlow` (em vez de
  `isPremiumActive`) e chamar `onPurchased` quando o nível subir durante a tela; expor
  `buy(activity, option)`
- [X] T020 [US1] Validar no emulador com seletor DEBUG (Plus libera 6º número e contatos) e, se a
  Play Console estiver pronta, compras de teste do Plus (quickstart §3)

**Checkpoint**: Plus vendável e funcional.

---

## Phase 4: User Story 2 - Oferta honesta comparando os planos (Priority: P1)

**Goal**: Oferta com Grátis/Plus/Pro, preços da loja, teste grátis, plano atual, loja
indisponível e Pro "em breve".

**Independent Test**: quickstart §2 nos 3 idiomas (sem loja) e §3 (com loja).

- [X] T021 [US2] Em `PaywallViewModel.kt` montar `PaywallUiState` do contrato a partir de
  `CatalogState` + `LicenseState`: preços `formattedPrice` (mensal/anual de cada nível, vitalício),
  `annualTrialAvailable` por nível (oferta `annual-trial-7d` presente), `proOnSale`,
  `storeUnavailable`, `pendingPurchase`, `currentTier`; `retry()` que reconsulta a loja
- [X] T022 [US2] Reescrever `main/ui/screen/paywall/PaywallScreen.kt`: tabela/cartões dos 3 níveis
  com os recursos da spec; "Sem anúncios e sem rastreamento" como comum a todos; seletor
  mensal/anual (e vitalício no Plus); selo "7 dias grátis" no anual quando disponível; plano atual
  marcado e sem botão de comprar o mesmo; Pro com selo "em breve" e sem botão quando
  `!proOnSale`; estado sem loja com "tentar de novo"; "Continuar com a versão gratuita" sempre
  visível; remover "Backup e restauração" e o item "Sem anúncios" como exclusivo
- [X] T023 [P] [US2] Strings nos 3 idiomas (`res/values`, `res/values-pt-rBR`,
  `res/values-es-rES`): nomes dos níveis, recursos, "mês"/"ano"/"compra única", "7 dias grátis",
  "Plano atual", "Em breve", "Loja indisponível, tente de novo", "Restaurar compras",
  "Gerenciar assinatura", textos legais curtos de renovação automática/cancelamento; remover
  `paywallscreen_body_item3*`/`item4*` obsoletos após `grep` confirmar ausência de uso
- [X] T024 [US2] Validar quickstart §2 (sem loja) nos 3 idiomas no emulador; §3 de preços quando a
  loja estiver configurada

**Checkpoint**: US1 + US2 = monetização Plus completa e honesta.

---

## Phase 5: User Story 3 - Perder ou cancelar o plano sem perder proteção (Priority: P2)

**Goal**: Estados tratados, aviso de rebaixamento, mensagens de pagamento da Play, retenção.

**Independent Test**: quickstart §4 (testador: cancelar/expirar) e seletor DEBUG
(Plus → Grátis com 12 números).

- [X] T025 [P] [US3] Em `main/data/local/dao/CallDecisionLogDao.kt` adicionar
  `@Query("DELETE FROM call_decision_logs WHERE createdAt < :cutoff") suspend fun deleteOlderThan(cutoff: Long)`
  e remover o limite fixo de 100 de `observeLatest` (manter parâmetro opcional sem default de
  corte); em `main/data/repository/CallDecisionLogRepository.kt` adicionar
  `suspend fun purge(tier: Tier, now: Long = System.currentTimeMillis())` usando `historyCutoff`
- [X] T026 [US3] Chamar `purge` em `main/CallGuardApplication.kt` (início, na coroutine de IO),
  em `main/telecom/screening/AppCallScreeningService.kt` (após `logDecision`, em `runCatching`,
  nunca afetando a resposta da chamada) e quando o nível muda em `BillingRepository.refreshPurchases`
- [X] T027 [US3] Em `main/ui/screen/home/HomeViewModel.kt`/`HomeScreen.kt`/`HomeRoute.kt`: aviso
  único de rebaixamento quando `tier < lastSeenTier` (whitelist mantida, contatos não liberados,
  histórico encurtado, botão "Ver planos"), gravando `setLastSeenTier(tier)` ao dispensar; indicar
  "pagamento em processamento" quando `hasPendingPurchase`; `CANCELED_ACTIVE` mostra "não
  renova" no cartão do plano
- [X] T028 [US3] Em `HomeRoute.kt`, ao entrar na Home, chamar
  `billingRepository.showPaymentMessages(activity)` (implementar em `BillingRepository` com
  `showInAppMessages` e `InAppMessageParams` categoria transacional; falha silenciosa)
- [X] T029 [P] [US3] Strings dos avisos (rebaixamento, pendente, não renova) nos 3 idiomas
- [X] T030 [US3] Validar: seletor DEBUG Plus (12 números) → Grátis: 12 continuam permitidos
  (`adb emu gsm call` de um deles), 13º bloqueado pelo limite, aviso exibido uma vez, histórico
  > 7 dias apagado; quickstart §4 quando houver testador

**Checkpoint**: rebaixamento seguro (Princípio I) e previsível.

---

## Phase 6: User Story 4 - Assinar o Pro e mudar de plano (Priority: P3)

**Goal**: Troca de plano pronta; Pro habilitável quando a 005 sair.

**Independent Test**: com `PRO_ON_SALE = true` num build local e testador: Plus mensal → Pro
anual; Pro → Plus (diferido); vitalício + Pro → volta a Plus.

- [X] T031 [US4] Em `BillingRepository.purchase`: se `Entitlement.activeSubscriptionToken` existe e
  o produto/plano escolhido difere, usar `SubscriptionUpdateParams` com `setOldPurchaseToken` e
  `setSubscriptionReplacementMode(WITH_TIME_PRORATION)` para upgrade (nível maior ou mensal→anual)
  e `DEFERRED` para downgrade; compra única (vitalício) nunca usa substituição
- [X] T032 [US4] Em `main/ui/screen/settings/SettingsScreen.kt`/`SettingsViewModel.kt`: cartão
  do plano atual com "Restaurar compras" (usa `restorePurchases` e mostra o resultado) e
  "Gerenciar assinatura" (abre a URL da Play da R10 com o produto vigente)
- [ ] T033 [US4] Validar troca de plano com `PRO_ON_SALE = true` apenas em build local de teste
  (não commitar a constante ligada)

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T034 [P] Atualizar termos de uso/política de privacidade (`terms_of_use_body`,
  `privacy_policy_body`) nos 3 idiomas: planos, renovação automática, cancelamento pela Play,
  retenção do histórico por nível
- [X] T035 [P] `grep -rn "isLicensed\|FREE_WHITELIST_LIMIT\|PREMIUM_MONTHLY\|isPremiumActive\|planName" app/src`
  sem resultados (exceto migração em `LicensePreferences`)
- [X] T036 Gates: `./gradlew testDebugUnitTest lintDebug assembleRelease`; `git diff --exit-code app/schemas/`
- [X] T037 Registrar resultados do quickstart em
  `specs/003-premium-plans/checklists/manual-validation.md` (indicar explicitamente o que dependeu
  de loja configurada e se foi feito)
- [X] T038 Atualizar `**Status**: Draft` → `Implemented` em `specs/003-premium-plans/spec.md`

---

## Dependencies & Execution Order

- Setup → Foundational → US1 → US2 → US3 → US4 → Polish
- US1 e US2 compartilham a oferta (`Paywall*`): US2 depois de US1
- US3 depende de US1 (`refreshPurchases`, estados); US4 depende de US1 (`purchase`)
- T013 toca muitos arquivos: fazer após T005, T009, T011, T012

### Parallel Opportunities

- Phase 2: T002, T003, T004, T006, T008, T010 em paralelo; T012 em paralelo com T005–T011
- US2: T023 em paralelo com T021–T022
- US3: T025 e T029 em paralelo
- Polish: T034 ∥ T035

## Parallel Example: Phase 2

```text
Task: "T002 Tier.kt"  ·  Task: "T003 PlanCatalog.kt"  ·  Task: "T004 PlanFeaturesTest"
Task: "T006 EntitlementResolverTest"  ·  Task: "T008 HistoryRetention + teste"  ·  Task: "T010 LicensePreferencesTest"
```

## Implementation Strategy

1. **MVP**: Phase 1–3 (Plus comprável) + Phase 4 (oferta honesta) — o que precisa ir junto à loja.
2. **Segurança**: Phase 5 antes de publicar (rebaixamento sem bloquear ninguém).
3. **Pro**: Phase 6 pronta e desligada até a 005.

## Notes

- Compras reais exigem a Play Console configurada (quickstart §0) — tarefas de validação registram
  o que não pôde ser testado.
- Schema Room inalterado; `PRO_ON_SALE` só vira `true` na 005.
