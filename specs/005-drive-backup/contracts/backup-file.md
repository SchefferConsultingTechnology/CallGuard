# Contract: Arquivo de backup

## `BackupCipher` (puro, JVM)

| Caso | Resultado |
|---|---|
| `decrypt(encrypt(x, "senha123"), "senha123")` | `x` |
| senha diferente | `BackupError.WrongPasswordOrCorrupted` |
| 1 byte alterado no texto cifrado | `BackupError.WrongPasswordOrCorrupted` |
| arquivo sem `CGBK` / curto demais | `BackupError.Unreadable` |
| dois `encrypt` da mesma entrada | bytes diferentes (sal e IV aleatórios) |

## `BackupCodec` (puro)

| Caso | Resultado |
|---|---|
| `decode(encode(data))` | `data` (com e sem histórico, com idioma nulo) |
| `formatVersion` maior que o suportado | `BackupError.NewerFormat` |

## `mergeWhitelist` (puro)

| Local | Arquivo | Resultado |
|---|---|---|
| — | `+5511987654321` "Mãe" | inserir |
| `+5511987654321` "Mae" (t=1) | mesmo número "Mãe" (t=2) | atualizar nome |
| `+5511987654321` "Mãe" (t=5) | mesmo número "Mae" (t=2) | nada |
| `+5511987654321` | `+551187654321` (8 dígitos, legado) | nada (mesmo número pela regra 001) |
| — | número não interpretável | ignorado |
