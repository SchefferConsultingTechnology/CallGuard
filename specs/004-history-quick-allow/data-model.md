# Data Model: Liberar Número a Partir do Histórico

Sem mudança de schema. Nova query no DAO da whitelist: `DELETE FROM allowed_numbers WHERE phoneE164 = :phoneE164`.

## Estado da tela de histórico (acréscimos)

| Campo | Significado |
|---|---|
| `actions: Map<logId, QuickAllowAction>` | `ADD`, `IN_WHITELIST` ou `NONE` por item (R1) |
| `canonicalPhones: Map<logId, String?>` | número identificado usado na inclusão |
| `pendingAddition` (interno ao ViewModel) | número + nome aguardando upgrade (R3) |

## Eventos

| Evento | Quando |
|---|---|
| `Added(phoneE164)` | inclusão feita → aviso com "Desfazer" |
| `LimitReached` | plano no limite → diálogo com "Ver planos" |
| `AddedAfterUpgrade(phoneE164)` | pendência concluída após upgrade |
