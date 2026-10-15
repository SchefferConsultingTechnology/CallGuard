# Data Model: Backup

## BackupData (domínio)

| Campo | Tipo |
|---|---|
| `formatVersion` | Int (atual: 1) |
| `exportedAt` | Long (ms) |
| `whitelist` | List<BackupEntry(label, phoneE164, updatedAt)> |
| `settings` | BackupSettings(blockUnknown, blockPrivateNumbers, language: String?) |
| `history` | List<BackupLog(id, phone?, normalizedPhone?, allowed, reason, createdAt)> (vazia se não incluída) |

## Arquivo `.cgbk`

`"CGBK"` · envelope v1 · sal(16) · IV(12) · AES-GCM(`BackupData` serializado)

## ImportResult

`Imported(added, updated, historyAdded)` · `WrongPasswordOrCorrupted` · `NewerFormat` · `Unreadable`

Sem mudança de schema Room.
