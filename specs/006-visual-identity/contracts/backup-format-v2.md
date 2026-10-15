# Contract: Formato do backup v2

Extensão do formato v1 da feature 005 (`BackupCodec`, `DataOutputStream`, dentro do envelope "CGBK"
criptografado — envelope inalterado).

```text
int     formatVersion        = 2
long    exportedAt
int     whitelistCount; repeat { UTF label; UTF phoneE164; long updatedAt }
boolean blockUnknown
boolean blockPrivateNumbers
nullUTF language
int     historyCount;   repeat { UTF id; nullUTF phone; nullUTF normalizedPhone; boolean allowed; UTF reason; long createdAt }
nullUTF themeMode            ← novo em v2 ("system" | "light" | "dark")
```

`nullUTF` = `boolean presente` + `UTF` quando presente (igual ao v1).

## Regras de leitura

| Arquivo | App v2 (esta feature) | App v1 (feature 005) |
|---|---|---|
| v1 | lê; `themeMode = null` → não altera o tema | lê |
| v2 | lê; aplica `themeMode` se for valor conhecido | recusa: "atualize o app" (`NewerFormat`) |
| > v2 | recusa: `NewerFormat` | recusa |

Valor de `themeMode` desconhecido → ignorado (tema não muda), sem recusar o arquivo.

## Testes

- ida e volta v2 com cada `ThemeMode` e com `null`;
- bytes v1 gravados pelo codificador antigo (fixture no teste) decodificam com `themeMode = null`;
- arquivo v3 → `NewerFormat`.
