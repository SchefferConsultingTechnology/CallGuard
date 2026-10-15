# Contract: Liberar pelo histórico

## `quickAllowAction(allowed, canonical, inWhitelist)` (puro)

| # | decisão | canônico | na whitelist | Resultado |
|---|---|---|---|---|
| 1 | bloqueada | `+5511987654321` | não | `ADD` |
| 2 | bloqueada | `+5511987654321` | sim | `IN_WHITELIST` |
| 3 | permitida | `+5511987654321` | sim | `IN_WHITELIST` |
| 4 | permitida | `+5511987654321` | não | `NONE` |
| 5 | bloqueada | `null` (oculto ou não interpretável) | — | `NONE` |

## `CallHistoryViewModel`

| Operação | Comportamento |
|---|---|
| `addToWhitelist(phoneE164, label)` | no limite → guarda pendência + `LimitReached`; senão inclui + `Added` (já existente = sem efeito, item vira `IN_WHITELIST`) |
| `undoAdd(phoneE164)` | remove o número da whitelist |
| upgrade com pendência | inclui sozinho + `AddedAfterUpgrade` |
