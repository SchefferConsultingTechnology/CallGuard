# Research: Normalização de Números de Telefone

**Feature**: 001-phone-normalization | **Date**: 2026-10-13

Experimentos executados com `libphonenumber 9.0.40` em JVM (OpenJDK 21) contra os formatos da
spec. Resultados resumidos em cada decisão.

## R1. Biblioteca de interpretação de números

- **Decision**: `com.googlecode.libphonenumber:libphonenumber:9.0.40` (Google, Java puro),
  declarada em `gradle/libs.versions.toml`.
- **Rationale**:
  - Java puro → o normalizador continua testável em JVM sem Robolectric (Princípio IV).
  - Metadados embarcados e offline (Princípio II); jar de ~360 KB.
  - Trata nativamente prefixo nacional `0`, código de operadora (`015 11…`), `0xx11…`,
    `55` sem `+`, 0800/4004 e números estrangeiros com `+`.
  - Parse "quente" medido em ~0,14 ms → irrelevante para o tempo de triagem (SC-005).
  - 9.0.41 saiu em 2026-10-07; fixamos 9.0.40 (mais madura) e atualizamos periodicamente,
    pois metadados de numeração mudam.
- **Alternatives considered**:
  - `android.telephony.PhoneNumberUtils.formatNumberToE164`: sem dependência nova, mas é API
    Android (não testável em JVM), o comportamento varia por versão do SO e não expõe
    validação/tipo nem formatação nacional controlada.
  - `io.michaelrocks:libphonenumber-android`: metadados como assets (menos memória), mas exige
    `Context` para carregar → testes passam a precisar de Robolectric. Ganho de memória não
    compensa para um app com uso pontual.
  - Manter regex próprio ampliado: reimplementar regras de numeração de cada país é inviável e
    frágil.

## R2. Critério de "válido" (quando um número vira canônico)

- **Decision**: O número é aceito como canônico se `isValidNumber` for verdadeiro. Exceção:
  entrada explicitamente internacional (começa com `+` ou foi reescrita como tal, ver R4) é
  aceita se `isPossibleNumber` for verdadeiro, mesmo que `isValidNumber` seja falso.
- **Rationale**: Validação estrita evita aceitar lixo vindo de formato nacional ambíguo (ex.:
  `987654321` sem DDD → possível mas inválido; não dá para adivinhar o DDD). Para entradas com
  `+`, metadados desatualizados não devem impedir o usuário de cadastrar um número estrangeiro
  recém-alocado. A mesma regra vale para todos os pontos de entrada (Princípio III), então
  whitelist e chamada recebida sempre convergem para a mesma chave.
- **Alternatives considered**: só `isPossibleNumber` (aceitaria `987654321` como
  `+55987654321`, chave errada); só `isValidNumber` (rejeitaria números novos com `+`).

## R3. Celular brasileiro antigo de 8 dígitos (FR-014)

- **Decision**: Regra própria aplicada após o parse, apenas para código de país 55:
  se o número nacional significativo tem 10 dígitos, o 3º dígito (primeiro do assinante) está
  em `6..9` e o número não é válido, insere `9` após o DDD e revalida; se o resultado for
  válido como `MOBILE`, usa-o.
- **Rationale**: Experimento: `(11) 8765-4321` → `+551187654321`, `valid=false`. Desde 2016
  todos os celulares brasileiros têm 9 dígitos começando com 9, e fixos começam com 2–5;
  portanto a conversão não colide com números fixos reais (`1133334444` permanece fixo e
  válido).
- **Alternatives considered**: comparação tolerante (casar com e sem 9 na hora da consulta) —
  exigiria duas chaves por número e quebraria a unicidade da identidade (Princípio III).

## R4. Prefixo internacional `00` digitado/salvo

- **Decision**: Pré-processamento antes do parse: se a entrada (só dígitos) começa com `00`,
  tentar também a variante `+` + resto; e, para região BR, a variante `+` + resto sem os 2
  dígitos de operadora (`0015 41…` → `+41…`). Usa-se a primeira variante que for válida pela
  regra R2; se nenhuma, cai no parse original.
- **Rationale**: Experimento: `005511987654321` com região BR foi interpretado como
  `+11987654321` (errado) porque no Brasil o prefixo internacional é `00` + operadora. Agenda
  com `0055…` é comum em quem copiou números do exterior.
- **Alternatives considered**: ignorar o caso (gera chave errada silenciosamente — pior que
  rejeitar).

## R5. Região padrão (FR-002)

- **Decision**: `TelephonyManager.simCountryIso` → `TelephonyManager.networkCountryIso` →
  `Locale.getDefault().country` → última região válida persistida (`last_known_region` no
  DataStore); valor em maiúsculas e aceito apenas se estiver em
  `PhoneNumberUtil.getSupportedRegions()`; senão `null` (só números com `+` são reconhecidos).
  A região detectada ao vivo é persistida sempre que mudar. A ordem fica numa função pura
  (`resolveRegion`) testada em JVM; `RegionProvider.defaultRegion()` é `suspend` por causa da
  leitura do DataStore.
- **Rationale**: Nenhum desses exige permissão de runtime (Princípio II). O chip vem primeiro
  para que, em roaming, números nacionais do país de origem continuem corretos (Edge Cases). O
  fallback persistido garante que um número da whitelist ligando em formato nacional continue
  reconhecido com o aparelho temporariamente sem chip/rede (Princípio I).
- **Alternatives considered**:
  - Região por `PhoneAccountHandle` da chamada (dual SIM com chips de países diferentes) —
    caso raro; registrado como melhoria futura.
  - Casar números nacionais com a whitelist por sufixo quando não há região — ambíguo entre
    países e quebra a identidade única (Princípio III).
  - `SharedPreferences` síncrono para o fallback — evitaria `suspend`, mas foge do padrão
    DataStore do projeto.

## R6. Onde vive cada responsabilidade

- **Decision**:
  - `domain/phone`: `PhoneNormalizer` (classe, Java puro + libphonenumber),
    `PhoneNumberDisplayFormatter`, interface `RegionProvider`.
  - `data/telephony/DeviceRegionProvider`: implementação Android de `RegionProvider`.
  - `data/migration/PhoneDataRenormalizer`: reprocessamento único da whitelist e do cache de
    contatos (FR-007, FR-011, FR-014), controlado por versão no DataStore.
  - `data/local/preferences/PhoneDataPreferences`: versão da normalização e última região.
  - `core/phone/PhoneServices`: montagem manual das dependências (sem framework de DI).
- **Rationale**: Mantém o domínio puro e testável; isola o acesso ao `TelephonyManager`.
- **Alternatives considered**: `PhoneNormalizer` continuar `object` com região global
  mutável — esconde dependência e dificulta testes.

## R7. Decisão de chamada para números não interpretáveis (FR-004, FR-005)

- **Decision**: Novos motivos `BLOCKED_UNKNOWN_UNPARSEABLE` e
  `ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED`; o caminho não interpretável segue `blockUnknown`.
  Falha interna do normalizador (`INTERNAL_ERROR`, qualquer exceção que não seja
  `NumberParseException`) gera `ALLOWED_NORMALIZATION_ERROR` e sempre permite.
  `INVALID_NUMBER` é **removido** do enum; logs antigos continuam exibidos porque o histórico
  mapeia a string persistida.
- **Rationale**: Corrige a violação do Princípio I (inclusive para bugs/falhas da própria lib);
  mantém o diagnóstico (FR-005) sem quebrar o histórico já gravado. Remover o valor legado do
  enum mantém o Princípio IV (todo motivo do enum tem teste que o produz).
- **Alternatives considered**: tratar `INTERNAL_ERROR` como desconhecido (bloquearia por falha
  da lib); deixar a exceção subir até o fail-open do serviço (funciona, mas some do histórico e
  depende de um `catch` distante do motor).

## R8. Persistência — precisa mudar o schema?

- **Decision**: Não. Room permanece na versão 2. `allowed_numbers.phoneE164` e
  `device_contacts.phoneE164` já guardam E.164; `call_decision_logs` já tem `phone` (bruto) e
  `normalizedPhone` (canônico). A conversão de dados (R3) é feita por código no
  `PhoneDataRenormalizer`, não por `Migration` SQL.
- **Rationale**: Evita migração de schema (menor risco ao Princípio IV); a lógica do nono
  dígito não é expressável de forma segura em SQL.

## R9. Desempenho no caminho de triagem

- **Decision**: Pré-aquecer `PhoneNumberUtil.getInstance()` e o parse da região padrão em
  `CallGuardApplication.onCreate` numa coroutine de IO. O serviço de triagem continua consultando
  apenas o Room (Princípio I).
- **Rationale**: O primeiro acesso carrega os metadados da região; aquecer evita que isso caia
  na primeira chamada após o processo ser criado.
