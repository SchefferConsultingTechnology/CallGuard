# Contract: Phone Normalization (domain/phone)

Contrato interno entre camadas. Tudo aqui é Kotlin/JVM puro, exceto `DeviceRegionProvider`.

## `RegionProvider`

```kotlin
fun interface RegionProvider {
    /** ISO 3166-1 alpha-2 uppercase supported by libphonenumber, or null. Never throws. */
    suspend fun defaultRegion(): String?
}
```

Implementação Android `DeviceRegionProvider(context, prefs: PhoneDataPreferences)`:
`simCountryIso` → `networkCountryIso` → `Locale.getDefault().country` → última região válida
persistida (`last_known_region`); filtra por regiões suportadas; persiste a região sempre que
uma fonte ao vivo produzir um valor diferente do salvo. Sem permissões de runtime. A ordem é
implementada na função pura
`resolveRegion(sim, network, locale, lastKnown, supported): String?`, testada em JVM.

`suspend` porque o fallback lê o DataStore. Os consumidores síncronos (prévia na UI) carregam a
região uma vez no `init` do ViewModel.

## `PhoneNormalizer`

```kotlin
class PhoneNormalizer(
    util: PhoneNumberUtil = PhoneNumberUtil.getInstance(),
    parse: (String, String?) -> Phonenumber.PhoneNumber = util::parse, // injetável para testes
) {
    fun normalize(input: String?, defaultRegion: String?): PhoneNormalizationResult
}
```

Comportamento obrigatório (cada linha vira caso de teste em `PhoneNormalizerTest`):

| # | `input` | `defaultRegion` | Resultado |
|---|---|---|---|
| 1 | `11987654321` | BR | `Valid(+5511987654321)` |
| 2 | `011987654321` | BR | `Valid(+5511987654321)` |
| 3 | `01511987654321` (operadora) | BR | `Valid(+5511987654321)` |
| 4 | `0xx11987654321` | BR | `Valid(+5511987654321)` |
| 5 | `(11) 98765-4321` | BR | `Valid(+5511987654321)` |
| 6 | `11.98765.4321` | BR | `Valid(+5511987654321)` |
| 7 | `+55 11 98765-4321` | BR | `Valid(+5511987654321)` |
| 8 | `5511987654321` | BR | `Valid(+5511987654321)` |
| 9 | `005511987654321` | BR | `Valid(+5511987654321)` (R4) |
| 10 | `(11) 8765-4321` | BR | `Valid(+5511987654321)` (R3, nono dígito) |
| 11 | `+551187654321` | qualquer | `Valid(+5511987654321)` (R3) |
| 12 | `(81) 8765-4321` | BR | `Valid(+5581987654321)` (R3) |
| 13 | `(11) 3333-4444` | BR | `Valid(+551133334444)` (fixo intacto) |
| 14 | `0800 123 4567` | BR | `Valid(+558001234567)` |
| 15 | `4004-1234` | BR | `Valid(+5540041234)` |
| 16 | `98765-4321` (sem DDD) | BR | `Invalid(NOT_VALID)` |
| 17 | `*144` | BR | `Invalid(NOT_VALID)` |
| 18 | `12345` | BR | `Invalid(NOT_VALID)` |
| 19 | `abc` | BR | `Invalid(NOT_A_NUMBER)` |
| 20 | `""` / `null` / `"   "` | BR | `Invalid(EMPTY)` |
| 21 | `11987654321` | `null` | `Invalid(NO_DEFAULT_REGION)` |
| 22 | `+5511987654321` | `null` | `Valid(+5511987654321)` |
| 23 | `+351 912 345 678` | BR | `Valid(+351912345678)` |
| 24 | `912345678` | PT | `Valid(+351912345678)` |
| 25 | `612 34 56 78` | ES | `Valid(+34612345678)` |
| 26 | `(650) 555-1212` | US | `Valid(+16505551212)` |
| 27 | `0015 41 44 668 18 00` | BR | `Valid(+41446681800)` (R4) |
| 28 | `+1 650 555 1212` | BR | `Valid(+16505551212)` |
| 29 | qualquer, com `parse` que lança `IllegalStateException` | BR | `Invalid(INTERNAL_ERROR)` |

Regras gerais:

- Nunca lança exceção. `NumberParseException` → `Invalid(NOT_VALID)`; qualquer outra exceção →
  `Invalid(INTERNAL_ERROR)`. `INTERNAL_ERROR` **não** significa "desconhecido": o motor sempre
  permite a chamada nesse caso (Princípio I).
- Idempotente: `normalize(valid.phoneE164, r)` devolve o mesmo `phoneE164` para qualquer `r`.
- Critério de validade: research R2. Ordem de tentativas: R4 → parse → R3.

## `PhoneNumberDisplayFormatter`

```kotlin
class PhoneNumberDisplayFormatter {
    /** Canonical → readable. National format if number region == userRegion, else international. */
    fun format(phoneE164: String, userRegion: String?): String
    /** Raw/unparseable → shown exactly as received (FR-010). */
    fun formatOrRaw(normalized: String?, raw: String?, userRegion: String?): String?
    /** Example number for the input placeholder, in national format of userRegion. */
    fun exampleFor(userRegion: String?): String?
}
```

| `phoneE164` | `userRegion` | Saída |
|---|---|---|
| `+5511987654321` | BR | `(11) 98765-4321` |
| `+5511987654321` | PT | `+55 11 98765-4321` |
| `+351912345678` | BR | `+351 912 345 678` |
| `+5511987654321` | `null` | `+55 11 98765-4321` |

## `CallDecisionEngine` (mudança de assinatura)

```kotlin
class CallDecisionEngine(
    allowedNumberDao: AllowedNumberDao,
    deviceContactDao: DeviceContactDao,
    normalizer: PhoneNormalizer,
)
suspend fun evaluate(
    phone: String?, settings: AppSettings, isLicensed: Boolean, defaultRegion: String?,
): CallDecision
```

Tabela de decisão (cada linha = teste em `CallDecisionEngineTest`):

| Entrada | Na whitelist | Contato + licença + toggle | `blockUnknown` | `blockPrivate` | Motivo |
|---|---|---|---|---|---|
| vazio/null | — | — | — | true | `BLOCKED_PRIVATE_NUMBER` |
| vazio/null | — | — | — | false | `ALLOWED_PRIVATE_NUMBER_DISABLED` |
| válido | sim | — | — | — | `ALLOWED_BY_WHITELIST` |
| válido | não | sim | — | — | `ALLOWED_BY_CONTACTS` |
| válido | não | não | true | — | `BLOCKED_NOT_IN_WHITELIST` |
| válido | não | não | false | — | `ALLOWED_UNKNOWN_DISABLED` |
| não interpretável | — | — | true | — | `BLOCKED_UNKNOWN_UNPARSEABLE` |
| não interpretável | — | — | false | — | `ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED` |
| erro interno (`INTERNAL_ERROR`) | — | — | qualquer | — | `ALLOWED_NORMALIZATION_ERROR` |

"Não interpretável" = `Invalid` com motivo `NOT_A_NUMBER`, `NO_DEFAULT_REGION` ou `NOT_VALID`
(`EMPTY` não chega aqui: entrada vazia segue as linhas de número privado).

Regressões obrigatórias: whitelist `+5511987654321` + chamada `11987654321`/`011…`/`015 11…`/
`1187654321` com região BR → `ALLOWED_BY_WHITELIST`.

## `PhoneDataRenormalizer` (data/migration)

```kotlin
class PhoneDataRenormalizer(/* daos, contacts repo, prefs, normalizer, regionProvider */) {
    /** Runs pending one-time renormalization. Idempotent; never throws. */
    suspend fun runIfNeeded(): Result<Unit>
}
```

Ver transições em [../data-model.md](../data-model.md#transições-do-renormalizador).
Requer em `AllowedNumberDao`: `suspend fun getAll(): List<AllowedNumberEntity>` e
`suspend fun updatePhone(id: String, phoneE164: String, updatedAt: Long)`.

Requer em `DeviceContactDao`:
`@Transaction suspend fun replaceAll(contacts: List<DeviceContactEntity>)` (apaga e insere numa
única transação — a triagem nunca enxerga o cache vazio; Princípio I). O cache só é trocado por
`replaceAll`; sem consentimento ou sem permissão de contatos o renormalizador chama `clear()`
(Princípio II).
