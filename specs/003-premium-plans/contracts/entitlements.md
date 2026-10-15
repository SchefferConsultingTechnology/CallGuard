# Contract: Níveis, direitos e compra

## `resolveEntitlement(purchases: List<PurchaseSnapshot>): Entitlement` (puro)

Cada linha vira teste em `EntitlementResolverTest`.

| # | Compras retornadas pela loja | tier | status | pending |
|---|---|---|---|---|
| 1 | nenhuma | FREE | NONE | false |
| 2 | `callguard_plus` PURCHASED renovando | PLUS | ACTIVE | false |
| 3 | `callguard_plus` PURCHASED **não** renovando | PLUS | CANCELED_ACTIVE | false |
| 4 | `callguard_plus_lifetime` PURCHASED | PLUS | ACTIVE | false |
| 5 | `callguard_pro` PURCHASED renovando | PRO | ACTIVE | false |
| 6 | lifetime + `callguard_pro` | PRO | ACTIVE | false |
| 7 | `callguard_plus` PENDING | FREE | NONE | true |
| 8 | lifetime + `callguard_pro` PENDING | PLUS | ACTIVE | true |
| 9 | produto desconhecido PURCHASED | FREE | NONE | false |
| 10 | `callguard_plus` UNSPECIFIED | FREE | NONE | false |

Também expõe `activeSubscriptionToken`/`Product` da assinatura de maior nível (para R5).

## `PlanFeatures.of(tier)` (puro)

Tabela do [data-model](../data-model.md#planfeatures-domínio-derivado-do-tier); teste 1:1.

## `historyCutoff(nowMillis, tier): Long` (puro)

`now − days(tier) × 86_400_000`; teste para os 3 níveis.

## `PlanCatalog`

```kotlin
object PlanCatalog {
    const val PLUS = "callguard_plus"
    const val PLUS_LIFETIME = "callguard_plus_lifetime"
    const val PRO = "callguard_pro"
    const val BASE_MONTHLY = "monthly"
    const val BASE_ANNUAL = "annual"
    const val OFFER_ANNUAL_TRIAL = "annual-trial-7d"
    const val PRO_ON_SALE = false   // true quando a 005 for publicada
}
```

## `BillingRepository` (mudanças)

| Operação | Comportamento |
|---|---|
| conexão | `enableAutoServiceReconnection()`; ao conectar: consultar produtos e compras |
| `queryProducts()` | `callguard_plus` + `callguard_plus_lifetime` (+ `callguard_pro` se `PRO_ON_SALE`) |
| `refreshPurchases()` | SUBS + INAPP; só em `OK` → `resolveEntitlement` → grava `LicensePreferences` |
| `purchase(activity, option)` | option = (produto, plano-base, usa teste?) ; se já há assinatura de outro nível/plano → `SubscriptionUpdateParams` (upgrade `WITH_TIME_PRORATION`, downgrade `DEFERRED`) |
| reconhecimento | toda compra `PURCHASED` não reconhecida é reconhecida (`acknowledgePurchase`) |
| `showPaymentMessages(activity)` | `showInAppMessages` categoria transacional, ao abrir a Home |
| `manageSubscriptionUrl(product)` | URL de gestão da Play (R10) |

## `PaywallUiState`

| Campo | Uso |
|---|---|
| `currentTier`, `status` | indicar plano atual; não oferecer o mesmo |
| `plusMonthly`, `plusAnnual`, `plusLifetime`, `proMonthly`, `proAnnual` | `formattedPrice` da loja ou `null` (sem resposta) |
| `annualTrialAvailable` (por nível) | exibir "7 dias grátis" (R2) |
| `proOnSale` | `PlanCatalog.PRO_ON_SALE` |
| `storeUnavailable` | mensagem "tente de novo" (US2-3) |
| `pendingPurchase` | "pagamento em processamento" |
