---

description: "Task list for 001-phone-normalization"
---

# Tasks: Normalização de Números de Telefone

**Input**: Design documents from `specs/001-phone-normalization/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/phone-normalizer.md,
quickstart.md

**Tests**: INCLUÍDOS — exigidos pela constituição (Princípio IV: todo `CallDecisionReason`
testado, regressão para todo bug do motor/normalização). Escrever os testes antes e vê-los falhar.

**Organization**: Tarefas agrupadas por user story para implementação e teste independentes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependência pendente)
- **[Story]**: User story da spec (US1–US4)
- Caminhos relativos à raiz do repositório. Prefixos abreviados:
  - `main/` = `app/src/main/java/com/lsp/callguard/`
  - `test/` = `app/src/test/java/com/lsp/callguard/`
  - `res/` = `app/src/main/res/`

---

## Phase 1: Setup

**Purpose**: Adicionar a dependência de interpretação de números (research R1).

- [X] T001 Adicionar `libphonenumber = "9.0.40"` em `[versions]` e
  `libphonenumber = { group = "com.googlecode.libphonenumber", name = "libphonenumber", version.ref = "libphonenumber" }`
  em `[libraries]` de `gradle/libs.versions.toml`; adicionar `implementation(libs.libphonenumber)`
  no bloco `dependencies` de `app/build.gradle.kts`
- [X] T002 Rodar `./gradlew :app:assembleDebug` e confirmar que compila com a nova dependência

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Novo normalizador, formatter e região — base de todas as stories (Princípio III).

**⚠️ CRITICAL**: Nenhuma user story começa antes desta fase terminar.

- [X] T003 [P] Criar `fun interface RegionProvider { suspend fun defaultRegion(): String? }` com
  KDoc ("ISO 3166-1 alpha-2 uppercase supported by libphonenumber, or null; never throws") em
  `main/domain/phone/RegionProvider.kt`
- [X] T004 [P] Criar `class PhoneDataPreferences(dataStore)` em
  `main/data/local/preferences/PhoneDataPreferences.kt` com
  `intPreferencesKey("phone_normalization_version")` (default 0) e
  `stringPreferencesKey("last_known_region")` (default null), expondo
  `suspend fun phoneNormalizationVersion(): Int`, `suspend fun setPhoneNormalizationVersion(v: Int)`,
  `suspend fun lastKnownRegion(): String?` e `suspend fun setLastKnownRegion(region: String)`
- [X] T005 [P] Criar `test/data/telephony/RegionResolutionTest.kt` para a função pura de T006,
  com `supported = setOf("BR", "PT", "US", "ES")`: chip `"br"` → `BR` (maiúsculas); roaming
  (chip `br`, rede `pt`) → `BR`; sem chip, rede `pt` → `PT`; sem chip/rede, locale `ES` → `ES`;
  só `lastKnown = "BR"` → `BR`; nada disponível → `null`; chip `"xx"` (não suportado) → cai para
  a próxima fonte; strings vazias tratadas como ausentes
- [X] T006 Criar `main/data/telephony/DeviceRegionProvider.kt` com
  `internal fun resolveRegion(sim: String?, network: String?, locale: String?, lastKnown: String?, supported: Set<String>): String?`
  (pura; ordem chip → rede → locale → lastKnown; `uppercase(Locale.ROOT)`; só valores em
  `supported`) e `class DeviceRegionProvider(context, prefs: PhoneDataPreferences) : RegionProvider`
  que lê `TelephonyManager.simCountryIso`, `networkCountryIso`, `Locale.getDefault().country` e
  `prefs.lastKnownRegion()`, chama `resolveRegion` com
  `PhoneNumberUtil.getInstance().supportedRegions`, persiste via `setLastKnownRegion` quando uma
  fonte ao vivo (chip/rede/locale) produziu uma região diferente da salva, e retorna `null` em
  qualquer exceção. Sem permissões novas (research R5). T005 deve passar
- [X] T007 [P] Reescrever `test/domain/phone/PhoneNormalizerTest.kt` cobrindo 1:1 os 28 casos
  da tabela `PhoneNormalizer` em `specs/001-phone-normalization/contracts/phone-normalizer.md`,
  mais: idempotência (`normalize(valid.phoneE164, r)` igual para r = BR, PT, null); "nunca lança"
  (entradas aleatórias/longas); e `PhoneNormalizer(parse = { _, _ -> throw IllegalStateException() })`
  → `Invalid(INTERNAL_ERROR)` (o construtor expõe
  `parse: (String, String?) -> Phonenumber.PhoneNumber`, com default delegando a
  `PhoneNumberUtil.parse`, para permitir o teste sem biblioteca de mock). Os testes DEVEM falhar
  antes de T008
- [X] T008 Reescrever `main/domain/phone/PhoneNormalizer.kt` de `object` regex para
  `class PhoneNormalizer` (util e `parse` injetáveis, ver T007) com
  `fun normalize(input: String?, defaultRegion: String?): PhoneNormalizationResult`:
  - vazio/blank → `Invalid(EMPTY)`; sem nenhum dígito → `Invalid(NOT_A_NUMBER)`;
  - R4: se os dígitos começam com `00`, tentar antes `"+" + resto` e, para região BR,
    `"+" + resto.drop(2)` (operadora); usar a primeira variante válida por R2;
  - parse com `defaultRegion`; sem `+` e `defaultRegion == null` → `Invalid(NO_DEFAULT_REGION)`;
  - R2: aceito se `isValidNumber`, ou se a entrada é internacional (`+`/variante R4) e
    `isPossibleNumber`; senão `Invalid(NOT_VALID)`;
  - R3: se `countryCode == 55`, NSN com 10 dígitos, 3º dígito em 6..9 e não válido → inserir `9`
    após o DDD e aceitar se válido e `MOBILE`;
  - `NumberParseException` → `Invalid(NOT_VALID)`; qualquer outra exceção →
    `Invalid(INTERNAL_ERROR)`.
  Atualizar `PhoneNormalizationResult.Valid(phoneE164: String, regionCode: String?)` e
  `PhoneNormalizationError { EMPTY, NOT_A_NUMBER, NO_DEFAULT_REGION, NOT_VALID, INTERNAL_ERROR }`;
  remover `isValidE164`. T007 deve passar
- [X] T009 [P] Criar `test/domain/phone/PhoneNumberDisplayFormatterTest.kt` com a tabela
  `PhoneNumberDisplayFormatter` do contrato, mais: `formatOrRaw(null, "12345", "BR") == "12345"`,
  `formatOrRaw(null, null, "BR") == null`, `exampleFor("BR")` não nulo e em formato nacional,
  `exampleFor(null) == null`
- [X] T010 Criar `main/domain/phone/PhoneNumberDisplayFormatter.kt` com `format(phoneE164,
  userRegion)` (NATIONAL se `regionCodeForNumber == userRegion`, senão INTERNATIONAL; em erro
  devolve a entrada), `formatOrRaw(normalized, raw, userRegion)` e `exampleFor(userRegion)` via
  `getExampleNumberForType(region, MOBILE)` em NATIONAL. T009 deve passar
- [X] T011 Criar `object PhoneServices` em `main/core/phone/PhoneServices.kt` com
  `val normalizer by lazy { PhoneNormalizer() }`, `val formatter by lazy { PhoneNumberDisplayFormatter() }`
  e `fun regionProvider(context: Context): RegionProvider = DeviceRegionProvider(context.applicationContext, PhoneDataPreferences(context.applicationContext.appPreferencesDataStore))`
  (DI manual, mesmo estilo dos `*ViewModelFactory` existentes)
- [X] T012 Migrar todos os chamadores para a nova API **sem mudar o comportamento de decisão**,
  para o projeto compilar:
  - `main/domain/engine/CallDecisionEngine.kt`: receber `normalizer: PhoneNormalizer` no
    construtor e `defaultRegion: String?` em `evaluate`/`shouldAllowCall`; manter o ramo
    `Invalid` devolvendo `INVALID_NUMBER` por enquanto (muda em T016);
  - `main/telecom/screening/AppCallScreeningService.kt`: obter
    `PhoneServices.regionProvider(applicationContext).defaultRegion()` em `evaluateCall`,
    construir o motor com `PhoneServices.normalizer` e passar a região a `evaluate` e
    `logDecision`; manter o fail-open existente intacto;
  - `main/data/repository/CallDecisionLogRepository.kt`: `logDecision(originalPhone, decision, defaultRegion: String?)`;
  - `main/data/repository/ContactsRepository.kt`: receber `RegionProvider` no construtor e
    chamar `defaultRegion()` uma vez por importação;
  - `main/ui/screen/contacts/ContactsImportRoute.kt`: passar `PhoneServices.regionProvider(context)`;
  - `main/ui/screen/whitelist/WhitelistScreen.kt`: temporariamente obter a região com
    `produceState<String?>(null) { value = PhoneServices.regionProvider(context).defaultRegion() }`
    (refeito em US3);
  - `test/domain/engine/CallDecisionEngineTest.kt`: só ajuste de assinatura
    (`normalizer = PhoneNormalizer()`, `defaultRegion = "BR"`)
- [X] T013 Rodar `./gradlew testDebugUnitTest` — tudo verde (o teste antigo
  `invalid number is blocked` continua passando até T014 substituí-lo)

**Checkpoint**: normalizador, formatter e região prontos e testados; projeto compila.

---

## Phase 3: User Story 1 - Chamada em formato nacional é reconhecida (Priority: P1) 🎯 MVP

**Goal**: Chamadas de números da whitelist são permitidas em qualquer formato; número não
interpretável nunca é bloqueado por si só; falha interna sempre permite (Princípio I).

**Independent Test**: Whitelist `+5511987654321`; chamadas `11987654321`, `011987654321`,
`01511987654321`, `+55 11 98765-4321` com região BR → `ALLOWED_BY_WHITELIST`; `12345` com
`blockUnknown=false` → permitida (quickstart §2).

### Tests for User Story 1 ⚠️

- [X] T014 [US1] Atualizar `test/domain/engine/CallDecisionEngineTest.kt`: substituir
  `invalid number is blocked` por dois testes (`BLOCKED_UNKNOWN_UNPARSEABLE` com
  `blockUnknown=true`, `ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED` com `blockUnknown=false`); cobrir
  as 9 linhas da tabela de decisão do contrato, incluindo `ALLOWED_NORMALIZATION_ERROR` com
  `blockUnknown=true` (normalizador com `parse` que lança, ver T007); adicionar regressões:
  whitelist `+5511987654321` + chamadas `11987654321`, `011987654321`, `01511987654321` e
  `1187654321` (8 dígitos, FR-014) → `ALLOWED_BY_WHITELIST`; contato `+5511987654321` +
  licença + toggle + chamada `011987654321` → `ALLOWED_BY_CONTACTS`; `defaultRegion = null` +
  chamada `11987654321` + `blockUnknown=false` → permitida. Devem falhar antes de T015–T016

### Implementation for User Story 1

- [X] T015 [US1] Em `main/domain/engine/CallDecision.kt` adicionar
  `ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED`, `BLOCKED_UNKNOWN_UNPARSEABLE` e
  `ALLOWED_NORMALIZATION_ERROR`; **remover** `INVALID_NUMBER` (nenhum código o produz; logs
  antigos continuam renderizados pela string persistida, ver T018)
- [X] T016 [US1] Em `main/domain/engine/CallDecisionEngine.kt`, ramo `Invalid`:
  `INTERNAL_ERROR` → `CallDecision(true, ALLOWED_NORMALIZATION_ERROR)`; demais motivos →
  `blockUnknown ? BLOCKED_UNKNOWN_UNPARSEABLE : ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED`.
  T014 deve passar
- [X] T017 [P] [US1] Adicionar `reason_unparseable_blocked` ("unrecognized number — unknown
  calls blocked"), `reason_unparseable_allowed` ("unrecognized number — unknown call blocking
  disabled") e `reason_normalization_error` ("could not read the number — allowed for safety")
  em `res/values/strings.xml`, `res/values-pt-rBR/strings.xml` e `res/values-es-rES/strings.xml`
  (traduções reais em pt-BR e es)
- [X] T018 [US1] No mapeamento de motivo em `main/ui/screen/history/CallHistoryScreen.kt`
  (bloco `when` por volta da linha 400) adicionar os três novos motivos usando as strings de
  T017; manter o ramo `"INVALID_NUMBER" ->` com o comentário `// legacy rows written before 001`
- [X] T019 [US1] Em `main/CallGuardApplication.kt` aquecer a lib numa coroutine
  `CoroutineScope(SupervisorJob() + Dispatchers.IO)`: chamar
  `PhoneServices.normalizer.normalize("0", PhoneServices.regionProvider(this).defaultRegion())`
  dentro de `runCatching` (research R9)
- [X] T020 [US1] Rodar `./gradlew testDebugUnitTest` e executar quickstart §2 no emulador
  (`adb emu gsm call 6505551212` etc.)

**Checkpoint**: MVP — bloqueios indevidos corrigidos; liberável sozinho.

---

## Phase 4: User Story 2 - Contatos em qualquer formato (Priority: P2)

**Goal**: Importar contatos em formato nacional, sem duplicatas; reprocessar dados existentes
uma vez após a atualização (FR-006, FR-007, FR-011, FR-014), sem janela de cache vazio.

**Independent Test**: Agenda com formatos mistos → 3 números distintos (quickstart §4);
atualização sobre dados antigos → whitelist convertida e sem duplicata (quickstart §5).

### Tests for User Story 2 ⚠️

- [X] T021 [P] [US2] Em `test/fakes/FakeAllowedNumberDao.kt` implementar `getAll()` e
  `updatePhone(id, phoneE164, updatedAt)`; em `test/fakes/FakeDeviceContactDao.kt` sobrescrever
  `replaceAll(contacts)` com uma única atribuição de `state` e contar invocações de `clear()`
  isolado
- [X] T022 [P] [US2] Criar `test/data/repository/DeviceContactMappingTest.kt` para a função pura
  de T026: linhas `(id, nome, número)` com `(11) 98765-4321`, `11987654321`,
  `+55 11 98765-4321`, `(11) 8765-4322`, `*144`, `+351912345678`, nome vazio → 3 contatos;
  duplicatas eliminadas por `phoneE164`; `*144` ignorado; nome vazio usa o fallback
- [X] T023 [P] [US2] Criar `test/data/migration/PhoneDataRenormalizerTest.kt` com
  `FakeAllowedNumberDao`, `FakeDeviceContactDao` e fakes em memória para versão/consentimento/
  permissão/carregador de contatos. Casos:
  (a) `+551187654321` vira `+5511987654321` com `updatedAt` novo;
  (b) `+551187654321` e `+5511987654321` coexistindo → resta só a já canônica;
  (c) entradas já canônicas intactas;
  (d) consentimento + permissão → cache substituído pelo carregador via `replaceAll`;
  (e) sem consentimento ou sem permissão → cache **apagado** (Princípio II), versão gravada;
  (f) versão já 1 → nada acontece;
  (g) carregador lança exceção → versão não gravada, `Result.failure`, nada lança, cache
  anterior preservado;
  (h) rodar duas vezes → mesmo estado;
  (i) a troca do cache usa `replaceAll` (nunca `clear()` seguido de `insertAll` separados)

### Implementation for User Story 2

- [X] T024 [US2] Em `main/data/local/dao/AllowedNumberDao.kt` adicionar
  `@Query("SELECT * FROM allowed_numbers") suspend fun getAll(): List<AllowedNumberEntity>` e
  `@Query("UPDATE allowed_numbers SET phoneE164 = :phoneE164, updatedAt = :updatedAt WHERE id = :id") suspend fun updatePhone(id: String, phoneE164: String, updatedAt: Long)`
  (sem mudança de schema; confirmar `app/schemas/` inalterado)
- [X] T025 [US2] Em `main/data/local/dao/DeviceContactDao.kt` adicionar
  `@Transaction suspend fun replaceAll(contacts: List<DeviceContactEntity>) { clear(); insertAll(contacts) }`
  (método default de interface; se o KSP do Room recusar, converter o DAO para `abstract class`)
  e fazer `main/data/repository/DeviceContactCacheRepository.kt` `replaceAll` delegar a ele —
  elimina a janela de cache vazio durante a triagem (Princípio I)
- [X] T026 [US2] Em `main/data/repository/ContactsRepository.kt` extrair a conversão de linhas
  do cursor para `internal fun mapToDeviceContacts(rows: List<RawContactRow>, normalizer: PhoneNormalizer, defaultRegion: String?, fallbackName: String): List<DeviceContact>`
  (top-level, pura, `distinctBy { it.phoneE164 }`), com `data class RawContactRow(id, displayName, rawPhone)`;
  `getDeviceContacts()` passa a ler o cursor para `RawContactRow` e delegar. T022 deve passar
- [X] T027 [US2] Criar `main/data/migration/PhoneDataRenormalizer.kt` conforme contrato e
  diagrama em `data-model.md`: dependências `AllowedNumberDao`, `DeviceContactDao`,
  `PhoneDataPreferences`, `SettingsPreferences` (consentimento), `hasContactsPermission: () -> Boolean`,
  `loadDeviceContacts: suspend () -> List<DeviceContact>`, `PhoneNormalizer`, `RegionProvider`;
  `TARGET_VERSION = 1`; `suspend fun runIfNeeded(): Result<Unit>` idempotente, nunca lança,
  grava versão só ao final; conflito (b) resolve com `deleteById` da legada; sem
  consentimento/permissão → `deviceContactDao.clear()`; com ambos → `deviceContactDao.replaceAll`.
  T023 deve passar
- [X] T028 [US2] Em `main/CallGuardApplication.kt`, na mesma coroutine de IO de T019 (após o
  aquecimento), montar e chamar `PhoneDataRenormalizer.runIfNeeded()`; permissão via
  `ContextCompat.checkSelfPermission(this, READ_CONTACTS) == PERMISSION_GRANTED`; carregador via
  `ContactsRepository(this, PhoneServices.regionProvider(this))::getDeviceContacts`
- [X] T029 [US2] Rodar `./gradlew testDebugUnitTest` e executar quickstart §4 e §5

**Checkpoint**: US1 + US2 funcionam de forma independente.

---

## Phase 5: User Story 3 - Cadastro manual no formato que o usuário conhece (Priority: P3)

**Goal**: Whitelist aceita formato nacional, com prévia, placeholder pela região, duplicata pela
forma canônica e erro explicativo (FR-008, FR-009, FR-013).

**Independent Test**: Digitar `11 98765-4321` → prévia `+55 11 98765-4321`; `(11) 98765-4321`
com `+5511987654321` já salvo → "já existe"; `123` → erro e botão desabilitado.

### Tests for User Story 3 ⚠️

- [X] T030 [P] [US3] Criar `test/ui/screen/whitelist/WhitelistViewModelTest.kt` usando
  `Dispatchers.setMain(StandardTestDispatcher())`, `FakeAllowedNumberDao`, `LicensePreferences`
  sobre `PreferenceDataStoreFactory.create { tempFolder.newFile("test.preferences_pb") }`
  (`@get:Rule val tempFolder = TemporaryFolder()`) e `RegionProvider { "BR" }`; após
  `advanceUntilIdle()` (região carregada no `init`):
  `previewFor("11 98765-4321")` → `PhoneInputPreview.Valid(display = "+55 11 98765-4321")`;
  `previewFor("123")` → `Invalid`; `previewFor("")` → `Empty`;
  `addNumber("Mãe", "(11) 98765-4321")` com `+5511987654321` existente emite
  `NumberAlreadyExists`; `addNumber("x", "123")` emite `InvalidNumber` e não insere

### Implementation for User Story 3

- [X] T031 [US3] Em `main/ui/screen/whitelist/WhitelistViewModel.kt`: receber
  `normalizer: PhoneNormalizer`, `formatter: PhoneNumberDisplayFormatter`,
  `regionProvider: RegionProvider`; carregar a região uma vez no `init` para um
  `MutableStateFlow<String?>`; criar `sealed interface PhoneInputPreview { Empty; Invalid; data class Valid(val phoneE164: String, val display: String) }`
  e `fun previewFor(input: String): PhoneInputPreview` síncrono usando a região em cache
  (display = formato INTERNATIONAL); expor `phoneExample: StateFlow<String?>` via
  `formatter.exampleFor(region)`; `addNumber(label, rawInput)` normaliza internamente, emite novo
  `WhitelistEvent.InvalidNumber` se inválido. T030 deve passar
- [X] T032 [US3] Em `main/ui/screen/whitelist/WhitelistViewModelFactory.kt` injetar
  `PhoneServices.normalizer`, `PhoneServices.formatter` e `PhoneServices.regionProvider(context)`
- [X] T033 [US3] Em `main/ui/screen/whitelist/WhitelistScreen.kt`: remover a função
  `isValidE164` (regex), o `produceState` provisório de T012 e todo uso direto de
  `PhoneNormalizer`/`PhoneServices` no `AddWhitelistNumberDialog`; o diálogo recebe
  `preview: (String) -> PhoneInputPreview` e `phoneExample: String?`; label `phone_input_label`;
  placeholder = `phoneExample`; com `Valid` mostrar `supportingText`
  `stringResource(R.string.phone_preview, preview.display)`; com `Invalid` e campo não vazio,
  `isError` e `phone_invalid_help`; `onConfirm(label, rawInput)`; tratar
  `WhitelistEvent.InvalidNumber` com a mesma mensagem
- [X] T034 [P] [US3] Nos três `strings.xml` (`res/values`, `res/values-pt-rBR`,
  `res/values-es-rES`): adicionar `phone_input_label` ("Phone number"/"Telefone"/"Teléfono"),
  `phone_preview` ("Will be saved as %1$s"/"Será salvo como %1$s"/"Se guardará como %1$s"),
  `phone_invalid_help` (explicar que pode digitar com DDD, ou com + e código do país para
  números do exterior); reescrever `contacts_import_description` sem mencionar "formato
  internacional" (ex.: pt-BR "Estamos procurando números válidos na sua agenda."); remover
  `phone_international_format`, `phone_example` e `phone_e164_help` após confirmar com
  `grep -rn` que não há outros usos
- [X] T035 [US3] Rodar `./gradlew testDebugUnitTest lintDebug` e validar o diálogo
  manualmente nos 3 idiomas

**Checkpoint**: US3 funciona sobre US1/US2 sem alterá-las.

---

## Phase 6: User Story 4 - Números exibidos em formato legível (Priority: P3)

**Goal**: Whitelist e histórico mostram números formatados (FR-010).

**Independent Test**: Whitelist com nacional e estrangeiro; histórico com número não
interpretável → exibido como chegou (quickstart §6).

### Implementation for User Story 4

- [X] T036 [P] [US4] Em `main/ui/screen/whitelist/WhitelistViewModel.kt` mapear itens para
  `WhitelistItemUi(id, label, phoneE164, displayPhone)` usando `formatter.format(phoneE164,
  region)` (combinando com o `StateFlow` da região de T031); em
  `main/ui/screen/whitelist/WhitelistScreen.kt` (por volta da linha 439) exibir `displayPhone`
  no lugar de `item.phoneE164`
- [X] T037 [P] [US4] Em `main/ui/screen/history/CallHistoryViewModel.kt` receber
  `PhoneNumberDisplayFormatter` e `RegionProvider`, combinar os logs com
  `flow { emit(regionProvider.defaultRegion()) }` e expor `displayPhone` por log via
  `formatter.formatOrRaw(log.normalizedPhone, log.phone, region)`; atualizar
  `main/ui/screen/history/CallHistoryViewModelFactory.kt` para injetar `PhoneServices`; em
  `main/ui/screen/history/CallHistoryScreen.kt` (por volta da linha 323) usar `displayPhone ?:
  stringResource(R.string.private_number)`
- [X] T038 [US4] Adicionar em `test/domain/phone/PhoneNumberDisplayFormatterTest.kt` o caso de
  log legado (`normalizedPhone = null`, `phone = "11987654321"`) → exibido como chegou; rodar
  `./gradlew testDebugUnitTest`

**Checkpoint**: Todas as stories completas.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T039 [P] Rodar `grep -rn "PhoneNormalizer\.\|isValidE164\|MISSING_COUNTRY_CODE\|INVALID_E164\|CallDecisionReason.INVALID_NUMBER" app/src`
  e remover resquícios da API antiga
- [X] T040 [P] Conferir que nenhum novo `Log.*` com número de telefone foi introduzido
  (`git diff development -- app/src/main | grep -n "Log\."`) — Princípio II
- [X] T041 Rodar os gates: `./gradlew testDebugUnitTest lintDebug assembleRelease` e
  `git diff --exit-code app/schemas/`
- [ ] T042 Executar o quickstart completo (§1–§6), incluindo §3 em aparelho real com chip BR, e
  registrar os resultados em `specs/001-phone-normalization/checklists/manual-validation.md`
  (data, aparelho, versão do Android, resultado por seção) — gate de validação manual da
  constituição
- [X] T043 Atualizar `Status: Draft` → `Status: Implemented` em
  `specs/001-phone-normalization/spec.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** → **Foundational (Phase 2)** → stories → **Polish (Phase 7)**
- **US1 (P1)**: só depende da Phase 2
- **US2 (P2)**: depende da Phase 2; T028 edita o mesmo arquivo que T019 (US1) → fazer depois
  de T019 ou coordenar
- **US3 (P3)**: depende da Phase 2 (formatter `exampleFor`); independente de US1/US2
- **US4 (P3)**: depende da Phase 2; T036 edita arquivos de US3 (`WhitelistViewModel`,
  `WhitelistScreen`) → fazer depois de T031–T033

### Within Phase 2

- T003, T004 → T006 (DeviceRegionProvider usa ambos); T005 antes de T006
- T007 → T008; T009 → T010
- T006, T008, T010 → T011 → T012 → T013

### Within Each User Story

- Testes primeiro, falhando → implementação → teste verde → validação do checkpoint

### Parallel Opportunities

- Phase 2: T003, T004, T005, T007, T009 em paralelo; depois T006 ∥ T008 ∥ T010
- US1: T017 em paralelo com T015–T016
- US2: T021, T022, T023 em paralelo
- US3: T030 ∥ T034
- US4: T036 ∥ T037
- Polish: T039 ∥ T040
- Com mais de uma pessoa: após a Phase 2, US1 e US3 podem andar em paralelo

---

## Parallel Example: Phase 2

```text
Task: "T003 RegionProvider em main/domain/phone/RegionProvider.kt"
Task: "T004 PhoneDataPreferences em main/data/local/preferences/PhoneDataPreferences.kt"
Task: "T005 RegionResolutionTest"
Task: "T007 PhoneNormalizerTest (28 casos do contrato + INTERNAL_ERROR)"
Task: "T009 PhoneNumberDisplayFormatterTest"
```

## Parallel Example: User Story 2

```text
Task: "T021 Fakes: getAll/updatePhone/replaceAll"
Task: "T022 DeviceContactMappingTest"
Task: "T023 PhoneDataRenormalizerTest (casos a–i)"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 + Phase 2
2. Phase 3 (US1)
3. **PARAR e VALIDAR**: quickstart §2 (+ §3 em aparelho BR)
4. Liberável: elimina o bloqueio indevido de familiares

### Incremental Delivery

1. Setup + Foundational → base pronta
2. US1 → validar → release (MVP)
3. US2 → validar §4/§5 → release (Premium útil no Brasil)
4. US3 + US4 → validar §6 → release

---

## Notes

- [P] = arquivos diferentes, sem dependências pendentes
- Commit por tarefa ou grupo lógico (Conventional Commits: `feat:`, `fix:`, `test:`)
- Schema Room permanece v2 — qualquer diff em `app/schemas/` é erro
