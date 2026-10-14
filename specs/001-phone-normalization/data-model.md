# Data Model: Normalização de Números de Telefone

**Feature**: 001-phone-normalization | **Date**: 2026-10-13

Nenhuma mudança de schema Room (permanece **versão 2**, ver research R8). Mudam o significado
garantido dos campos, os valores de enum persistidos como string e uma chave nova no DataStore.

## Conceitos de domínio

### Número canônico (`phoneE164: String`)

- Formato E.164: `+` seguido de 8–15 dígitos (ex.: `+5511987654321`).
- Produzido **exclusivamente** por `PhoneNormalizer` (contrato em
  [contracts/phone-normalizer.md](contracts/phone-normalizer.md)).
- Invariantes:
  - Celular brasileiro é sempre gravado com 9 dígitos de assinante (FR-014).
  - Dois textos que representam o mesmo telefone geram exatamente o mesmo valor.

### Número bruto (`phone: String?`)

- Texto como chegou (handle da chamada, campo digitado, campo da agenda). Só para exibição e
  diagnóstico; nunca usado como chave de comparação.

### Região padrão (`defaultRegion: String?`)

- Código ISO 3166-1 alfa-2 em maiúsculas (`"BR"`), suportado pela libphonenumber, ou `null`.
- Fonte: `RegionProvider` (chip → rede → locale → última região válida). A última região
  válida detectada é persistida no DataStore (`last_known_region`) para servir de fallback
  quando o aparelho fica sem chip/rede/locale (Princípio I: whitelist sempre reconhecida).

### Resultado da interpretação (`PhoneNormalizationResult`)

| Variante | Campos | Uso |
|---|---|---|
| `Valid` | `phoneE164`, `regionCode` | Chave canônica e região do número (formatação). |
| `Invalid` | `reason: PhoneNormalizationError` | Nunca decide bloqueio por si só (ver abaixo). |

`PhoneNormalizationError`:

| Valor | Termo na spec | Efeito na decisão |
|---|---|---|
| `EMPTY` | número oculto/privado | regra `blockPrivateNumbers` |
| `NOT_A_NUMBER`, `NO_DEFAULT_REGION`, `NOT_VALID` | "não interpretável" | tratado como desconhecido → `blockUnknown` |
| `INTERNAL_ERROR` | "falha inesperada" | **sempre permite** (Princípio I) |

(`MISSING_COUNTRY_CODE` e `INVALID_E164` deixam de existir.) No código, "não interpretável" =
`Invalid` com um dos três motivos da 2ª linha; os motivos de decisão correspondentes usam o
sufixo `UNPARSEABLE`.

## Entidades persistidas (Room v2 — sem alteração de colunas)

### `allowed_numbers` (whitelist)

| Campo | Tipo | Regra nova |
|---|---|---|
| `id` | TEXT PK | — |
| `label` | TEXT | — |
| `phoneE164` | TEXT UNIQUE | MUST ser canônico pela nova regra; entradas legadas são convertidas pelo renormalizador. |
| `createdAt` / `updatedAt` | INTEGER | `updatedAt` atualizado quando o renormalizador muda o número. |

**Conflito na conversão**: se a entrada legada convertida colidir com uma entrada já existente,
mantém-se a já canônica e remove-se a legada (o usuário não perde proteção; FR-011, SC-006).

### `device_contacts` (cache de contatos)

| Campo | Regra nova |
|---|---|
| `phoneE164` UNIQUE | Canônico pela nova regra; o cache inteiro é reconstruído pelo renormalizador. |

**Troca atômica**: o cache só é substituído por `DeviceContactDao.replaceAll` (`@Transaction`:
apaga e insere numa única transação), para que a triagem nunca encontre o cache vazio.

### `call_decision_logs` (histórico)

| Campo | Regra nova |
|---|---|
| `phone` | Bruto, como chegou (inalterado). |
| `normalizedPhone` | Canônico quando interpretável; `null` caso contrário. |
| `reason` | String de `CallDecisionReason` — novos valores abaixo. |

### `CallDecisionReason` (persistido como string)

| Valor | Produzido? | Significado |
|---|---|---|
| `ALLOWED_BY_WHITELIST` | sim | — |
| `ALLOWED_BY_CONTACTS` | sim | — |
| `ALLOWED_UNKNOWN_DISABLED` | sim | Interpretável, desconhecido, `blockUnknown=false`. |
| `BLOCKED_NOT_IN_WHITELIST` | sim | Interpretável, desconhecido, `blockUnknown=true`. |
| `BLOCKED_PRIVATE_NUMBER` | sim | — |
| `ALLOWED_PRIVATE_NUMBER_DISABLED` | sim | — |
| `ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED` | **novo** | Não interpretável, `blockUnknown=false`. |
| `BLOCKED_UNKNOWN_UNPARSEABLE` | **novo** | Não interpretável, `blockUnknown=true`. |
| `ALLOWED_NORMALIZATION_ERROR` | **novo** | Falha interna ao interpretar; sempre permite. |
| `INVALID_NUMBER` | **removido do enum** | Existe só como string em logs antigos; o histórico o mapeia pela string. |

## DataStore (preferências)

| Chave | Tipo | Default | Uso |
|---|---|---|---|
| `phone_normalization_version` | Int | `0` | Versão da regra de normalização já aplicada aos dados. Alvo desta feature: `1`. |
| `last_known_region` | String? | `null` | Última região válida detectada ao vivo (chip/rede/locale); fallback do `RegionProvider`. |

Ambas as chaves ficam em `PhoneDataPreferences` (`data/local/preferences`).

### Transições do renormalizador

```text
version < 1 ──► renormaliza whitelist ──► (consentimento && permissão?) ──sim──► reimporta cache
                                                     │                      (replaceAll atômico)
                                                     │                                │
                                                     └──não──► apaga o cache ─────────┤
                                                               (Princípio II)         ▼
                                                                         grava version = 1
Falha em qualquer etapa ──► não grava version, cache anterior preservado
                            (tenta de novo na próxima abertura)
```

- Idempotente: rodar duas vezes produz o mesmo resultado.
- Executa fora do caminho da triagem (Application, IO).
