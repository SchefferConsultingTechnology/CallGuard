# Data Model: Planos Plus e Pro

**Feature**: 003-premium-plans | **Date**: 2026-10-14

Sem mudança de schema Room (nova query apenas). Mudanças no DataStore de licença.

## Tier (enum, domínio)

`FREE < PLUS < PRO` (ordem = hierarquia; "máximo" escolhe o maior).

## PlanFeatures (domínio, derivado do Tier)

| Campo | FREE | PLUS | PRO |
|---|---|---|---|
| `whitelistLimit` | 5 | `null` (ilimitado) | `null` |
| `contactsAllowed` | false | true | true |
| `historyRetentionDays` | 7 | 90 | 365 |
| `driveBackup` | false | false | true |

## PurchaseSnapshot (domínio, espelho neutro de `Purchase`)

| Campo | Tipo |
|---|---|
| `productIds` | List<String> |
| `state` | `PURCHASED` / `PENDING` / `UNSPECIFIED` |
| `isAutoRenewing` | Boolean (false para compra única) |
| `purchaseToken` | String |

## Entitlement (domínio)

| Campo | Significado |
|---|---|
| `tier` | nível vigente |
| `status` | `ACTIVE`, `CANCELED_ACTIVE` (não renova, ativo até o fim), `NONE` |
| `hasPendingPurchase` | há compra aguardando pagamento |
| `activeSubscriptionToken` | token da assinatura vigente (para troca de plano), se houver |
| `activeSubscriptionProduct` | `callguard_plus` / `callguard_pro`, se houver |
| `hasLifetime` | possui `callguard_plus_lifetime` |

Regra de mapeamento produto → nível: `callguard_pro` → PRO; `callguard_plus`,
`callguard_plus_lifetime` → PLUS.

## LicensePreferences (DataStore `app_preferences`)

| Chave | Antes | Agora |
|---|---|---|
| `is_licensed` (bool) | fonte da licença | lida só para migração (true → `PLUS`), depois removida |
| `plan_name` (string) | "Free"/"Premium" | removida |
| `tier` (string) | — | `FREE`/`PLUS`/`PRO` (cache do último resultado `OK` da loja) |
| `plan_status` (string) | — | `ACTIVE`/`CANCELED_ACTIVE`/`NONE` |
| `pending_purchase` (bool) | — | compra pendente |
| `last_seen_tier` (string) | — | para detectar rebaixamento (aviso único) |

`LicenseState` exposto ao app: `tier`, `status`, `hasPendingPurchase`, `features: PlanFeatures`.

## CallDecisionLog — retenção

- Nova query: `DELETE FROM call_decision_logs WHERE createdAt < :cutoff`.
- `cutoff = agora − historyRetentionDays(tier) × 24 h`.

## Transições de nível

```text
FREE ──compra Plus/vitalício──► PLUS ──upgrade (WITH_TIME_PRORATION)──► PRO
 ▲                               ▲  ◄──downgrade (DEFERRED, fim do período)──┘
 └──expira/reembolso/suspensão───┘   (Pro expira com vitalício → PLUS; sem vitalício → FREE)
```
