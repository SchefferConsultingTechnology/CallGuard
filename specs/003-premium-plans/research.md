# Research: Planos Plus e Pro

**Feature**: 003-premium-plans | **Date**: 2026-10-14

APIs conferidas com `javap` no `billing-9.1.0` em uso no projeto.

## R1. Produtos na Play Console

- **Decision**:

| Produto | Tipo | Planos-base / ofertas |
|---|---|---|
| `callguard_plus` | assinatura | `monthly` (P1M); `annual` (P1Y) + oferta `annual-trial-7d` (fase grátis P7D, elegibilidade "novo cliente") |
| `callguard_plus_lifetime` | compra única (não consumível) | — |
| `callguard_pro` | assinatura | `monthly`; `annual` + `annual-trial-7d` |

  Preços BR (decisão 2026-10-14): Plus 6,90/49,90, vitalício 99,90; Pro 11,90/89,90. Demais países
  pela conversão da Play Console. O produto antigo `callguard_premium_monthly` nunca existiu na loja
  e é removido do código.
- **Rationale**: um produto de assinatura por nível com planos-base mensal/anual é o modelo atual do
  Play; facilita upgrade/downgrade entre níveis (R5). O vitalício precisa ser compra única (assinatura
  não tem "para sempre").
- **Alternatives considered**: um único produto com 4 planos-base (Plus/Pro misturados) — torna o
  nível uma convenção de nome de plano, mais frágil.

## R2. Elegibilidade ao teste grátis

- **Decision**: Exibir "7 dias grátis" somente se `subscriptionOfferDetails` do plano anual trouxer a
  oferta `annual-trial-7d` com uma fase de preço zero; comprar usando o `offerToken` dessa oferta
  quando presente, senão o do plano-base.
- **Rationale**: a Play só devolve ofertas para as quais a conta é elegível; o app não precisa
  guardar quem já usou o teste (sem backend).

## R3. Determinar o nível vigente (FR-005, FR-006)

- **Decision**: Função pura `resolveEntitlement(purchases: List<PurchaseSnapshot>): Entitlement`
  sobre um modelo independente da biblioteca (`productIds`, `state`, `isAutoRenewing`,
  `isAcknowledged`). Regras: compras `PURCHASED` contam; `PENDING` não libera nada mas sinaliza
  "pagamento em processamento"; nível = máximo entre os produtos válidos (Pro > Plus > Grátis);
  assinatura com `isAutoRenewing = false` ainda retornada = "cancelada, ativa até o fim".
- **Rationale**: testável em JVM (Princípio IV) e isola a biblioteca.
- **Limite conhecido**: sem servidor, o app não distingue *carência* de *ativo* (a Play devolve a
  compra nos dois casos) nem *suspensão/expiração/reembolso* entre si (a compra simplesmente não é
  devolvida). Tratamento:
  - carência / pagamento recusado → mensagens nativas da Play via `showInAppMessages`
    (categoria transacional), exibidas ao abrir o app;
  - suspensão, expiração e reembolso → nível cai para o que as compras restantes garantirem
    (ex.: Pro expira, Plus vitalício continua).

## R4. Cache local e modo offline (FR-012)

- **Decision**: `LicensePreferences` passa a guardar `tier` (string), `plan_state` e
  `pending_purchase` (booleano). Só é sobrescrito quando `queryPurchasesAsync` responde `OK`;
  erros de conexão mantêm o último valor. Migração: chave antiga `is_licensed = true` (só possível
  pelo mock de debug) → `PLUS`. `BillingClient` com `enableAutoServiceReconnection()`.
- **Rationale**: quem pagou não é rebaixado por estar offline; sem nova dependência.

## R5. Troca de plano (US4)

- **Decision**: entre assinaturas (`callguard_plus` ↔ `callguard_pro`, mensal ↔ anual) usar
  `SubscriptionUpdateParams` com o token da assinatura atual:
  - upgrade (Plus→Pro, mensal→anual) → `WITH_TIME_PRORATION` (vale na hora, crédito proporcional);
  - downgrade (Pro→Plus) → `DEFERRED` (vale no fim do período).
  Vitalício + Pro: compras independentes; o nível é o máximo (R3), então ao fim do Pro volta ao Plus.
- **Rationale**: modos recomendados pela Play para upgrade/downgrade; evita cobrança dupla.

## R6. Recursos por nível

- **Decision**: `PlanFeatures.of(tier)` no domínio: `whitelistLimit` (5 / ilimitado / ilimitado),
  `contactsAllowed` (não / sim / sim), `historyRetentionDays` (7 / 90 / 365), `driveBackup`
  (não / não / sim). Todos os pontos que hoje usam `isLicensed` passam a perguntar o recurso
  (motor de decisão, whitelist, consentimento de contatos, configurações).
- **Rationale**: uma fonte de verdade para limites; acrescentar nível no futuro não espalha `if`.

## R7. Retenção do histórico (FR-008)

- **Decision**: `CallDecisionLogDao.deleteOlderThan(cutoffMillis)` chamado (a) ao iniciar o app,
  (b) após gravar cada decisão na triagem e (c) quando o nível muda. Remover o limite fixo de 100
  itens da listagem (a retenção passa a controlar o volume).
- **Rationale**: cumpre a constituição (retenção definida) sem migração de schema (nova query, mesmo
  schema v2).

## R8. Pro fora de venda até a 005 (FR-010)

- **Decision**: constante `PlanCatalog.PRO_ON_SALE = false` no domínio; com `false`, a oferta mostra
  o Pro como "em breve", sem botão e sem consultar o produto na loja. A 005 troca para `true`.
- **Alternatives considered**: flag remota (exigiria backend ou Remote Config — rede extra proibida
  pela constituição).

## R9. Aviso de rebaixamento (US3-1, US3-5, Edge Cases)

- **Decision**: guardar `last_seen_tier`; quando o nível resolvido for menor, mostrar um aviso único
  na Home explicando o que mudou (whitelist mantida, contatos não liberados, histórico encurtado) e
  como voltar a assinar.

## R10. Restaurar e gerenciar (FR-011)

- **Decision**: "Restaurar compras" chama `queryPurchasesAsync` para assinaturas e compras únicas e
  mostra o resultado; "Gerenciar assinatura" abre
  `https://play.google.com/store/account/subscriptions?sku=<produto>&package=com.lsp.callguard`.

## R11. Testes

- **Decision**: JVM para `resolveEntitlement`, `PlanFeatures`, retenção (`cutoffFor`) e migração
  de `LicensePreferences`. Compras reais exigem o app numa trilha de teste da Play Console com
  contas de "license testers" — pré-requisito manual do responsável pelo app (quickstart §0).
