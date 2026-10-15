# Implementation Plan: Planos Plus e Pro

**Branch**: `003-premium-plans` | **Date**: 2026-10-14 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/003-premium-plans/spec.md`

## Summary

Trocar a licença booleana por um **nível** (`FREE`/`PLUS`/`PRO`) derivado das compras da Play por
uma função pura, com recursos por nível centralizados em `PlanFeatures`. Três produtos na Play
(`callguard_plus` com planos mensal/anual + teste de 7 dias, `callguard_plus_lifetime` compra única,
`callguard_pro` mensal/anual), troca de plano com os modos de substituição da Play, mensagens
nativas de pagamento, retenção do histórico por nível e uma oferta reescrita que compara os três
níveis com preços da loja. O Pro fica "em breve" até a feature 005.

## Technical Context

**Language/Version**: Kotlin 2.3.20, JVM target 11

**Primary Dependencies**: Google Play Billing 9.1.0 (APIs conferidas: `getOneTimePurchaseOfferDetailsList`,
`SubscriptionUpdateParams` + `ReplacementMode`, `showInAppMessages`, `enableAutoServiceReconnection`),
Compose/Material 3, DataStore, Room — nenhuma dependência nova

**Storage**: DataStore (`tier`, `plan_status`, `pending_purchase`, `last_seen_tier`; migração de
`is_licensed`); Room v2 inalterado (nova query de retenção)

**Testing**: JUnit 4 + coroutines-test (JVM); compras reais com testadores de licença na Play Console

**Target Platform**: Android minSdk 26 / target 36 (exclusivamente Android — constituição v2.0.0)

**Project Type**: mobile-app (módulo único `:app`)

**Performance Goals**: recursos liberados ≤ 5 s após confirmação da loja (SC-001)

**Constraints**: sem backend (validação só no aparelho); offline mantém último nível; Pro fora de
venda até a 005; nenhum número da whitelist perde proteção ao rebaixar

**Scale/Scope**: ~15 arquivos de produção (billing, licença, domínio de planos, motor, whitelist,
consentimento, configurações, oferta, home, histórico), 4 arquivos de teste, strings nos 3 idiomas

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Avaliação | Pós-design |
|---|---|---|
| I. Fail-Open | Rebaixar não pode bloquear números da whitelist. | ✅ Motor continua permitindo whitelist sem olhar nível; só `contactsAllowed` muda. |
| II. Privacidade (v2.0.0) | Rede apenas Play Billing. | ✅ Sem rede nova; retenção de histórico definida por nível (7/90/365). |
| III. Identidade de números | Não afetado. | ✅ |
| IV. Motor puro e testado | `isLicensed` → `contactsAllowed` no motor. | ✅ `resolveEntitlement`, `PlanFeatures`, `historyCutoff` puros e testados; testes do motor atualizados; schema inalterado. |
| V. Localização | Oferta e termos novos. | ✅ 3 idiomas; preços vêm formatados da loja. |
| VI. Monetização honesta (v2.0.0) | Níveis, licença como nível, oferta sem recursos inexistentes. | ✅ Remove "Backup e restauração" e "Sem anúncios" como exclusivos; Pro "em breve"; "continuar grátis" mantido; Play é a fonte da verdade, DataStore é cache; mock só em DEBUG. |
| Restrições | Sem backend, sem dependência nova. | ✅ Mensagens de pagamento via Play (`showInAppMessages`) em vez de servidor. |

**Resultado**: PASS. Limitação aceita e documentada (R3): sem servidor, carência/suspensão não são
distinguíveis no app — mitigado pelas mensagens nativas da Play.

## Project Structure

### Documentation (this feature)

```text
specs/003-premium-plans/
├── spec.md · plan.md · research.md · data-model.md · quickstart.md
├── contracts/entitlements.md
├── checklists/requirements.md
└── tasks.md             # /speckit-tasks
```

### Source Code (repository root)

```text
app/src/main/java/com/lsp/callguard/
├── domain/model/
│   ├── Tier.kt                    # NOVO enum FREE < PLUS < PRO
│   ├── PlanFeatures.kt            # NOVO (substitui PlanLimits.FREE_WHITELIST_LIMIT)
│   ├── PlanCatalog.kt             # NOVO ids de produto/plano/oferta, PRO_ON_SALE
│   ├── LicenseState.kt            # tier, status, pending, features
│   └── PlanLimits.kt              # REMOVIDO
├── domain/billing/
│   ├── PurchaseSnapshot.kt        # NOVO espelho neutro de Purchase
│   ├── Entitlement.kt             # NOVO + resolveEntitlement()
│   └── HistoryRetention.kt        # NOVO historyCutoff()
├── data/billing/BillingRepository.kt        # 3 produtos, SUBS+INAPP, upgrade/downgrade, mensagens
├── data/local/preferences/LicensePreferences.kt  # tier/status/pending/last_seen + migração
├── data/local/dao/CallDecisionLogDao.kt     # deleteOlderThan; sem limite fixo de 100
├── data/repository/CallDecisionLogRepository.kt  # purge(tier)
├── domain/engine/CallDecisionEngine.kt      # isLicensed → contactsAllowed
├── telecom/screening/AppCallScreeningService.kt  # features do nível; purge após gravar
├── CallGuardApplication.kt / MainActivity.kt     # purge no início; legado: tier != FREE
└── ui/screen/
    ├── paywall/{PaywallScreen,PaywallViewModel,PaywallViewModelFactory,PaywallRoute}.kt  # 3 níveis
    ├── home/{HomeViewModel,HomeScreen,HomeRoute}.kt        # aviso de rebaixamento, mensagens Play
    ├── settings/{SettingsViewModel,SettingsScreen}.kt      # plano atual, restaurar, gerenciar, mock de nível (DEBUG)
    ├── whitelist/WhitelistViewModel.kt                     # limite por nível
    └── consent/ContactsConsentViewModel.kt                 # contactsAllowed

app/src/main/res/values{,-pt-rBR,-es-rES}/strings.xml        # oferta, estados, avisos, termos

app/src/test/java/com/lsp/callguard/
├── domain/billing/EntitlementResolverTest.kt     # 10 linhas do contrato
├── domain/model/PlanFeaturesTest.kt
├── domain/billing/HistoryRetentionTest.kt
├── data/local/preferences/LicensePreferencesTest.kt   # migração is_licensed
└── domain/engine/CallDecisionEngineTest.kt       # contactsAllowed
```

**Structure Decision**: módulo único `:app`; regras de plano no domínio (`domain/model`,
`domain/billing`), integração com a Play isolada em `data/billing`.

## Fases de entrega

1. **Fundação**: `Tier`, `PlanFeatures`, `PlanCatalog`, `PurchaseSnapshot`, `resolveEntitlement`,
   `historyCutoff`, `LicensePreferences` com migração + testes; substituir `isLicensed` por recursos
   em todo o app (comportamento Grátis/Plus equivalente ao atual).
2. **US1 (P1)**: `BillingRepository` com os 3 produtos, compra de Plus mensal/anual/vitalício,
   reconhecimento, restaurar compras.
3. **US2 (P1)**: oferta com 3 níveis, preços da loja, teste grátis, plano atual, loja indisponível,
   Pro "em breve".
4. **US3 (P2)**: estados (pendente, cancelado-ativo), mensagens da Play, aviso de rebaixamento,
   retenção do histórico.
5. **US4 (P3)**: troca de plano (upgrade/downgrade) — código pronto, habilitado com `PRO_ON_SALE`.
6. **Polimento**: termos de assinatura nos 3 idiomas, mock de nível em DEBUG, gates, quickstart.

## Riscos

| Risco | Mitigação |
|---|---|
| Sem servidor, fraude/compras falsas | Aceito (constituição); reconhecimento obrigatório evita reembolso automático em 3 dias. |
| Testes reais dependem da Play Console | Quickstart §0; §2 cobre o que dá sem loja; mock de nível em DEBUG. |
| Carência/suspensão indistinguíveis | Mensagens nativas da Play (R3). |
| Rebaixar apaga histórico | Aviso explícito (R9); retenção é regra da constituição. |

## Complexity Tracking

Sem violações a justificar.
