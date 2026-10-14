# Implementation Plan: Normalização de Números de Telefone

**Branch**: `001-phone-normalization` | **Date**: 2026-10-13 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-phone-normalization/spec.md`

## Summary

Substituir o normalizador atual (regex que exige `+`) por um `PhoneNormalizer` baseado na
libphonenumber, que interpreta formatos nacionais usando a região do aparelho e gera uma única
chave E.164 para whitelist, contatos, cadastro manual e chamadas recebidas. O motor de decisão
deixa de bloquear números não interpretáveis por si só (passam a seguir `blockUnknown`). Um
renormalizador único converte dados existentes (incluindo celulares BR de 8 dígitos) sem mudar o
schema Room. A UI ganha prévia no cadastro e exibição formatada.

## Technical Context

**Language/Version**: Kotlin 2.3.20, JVM target 11

**Primary Dependencies**: Jetpack Compose (BOM 2026.03.01), Room 2.8.4, DataStore Preferences
1.1.7, Play Billing 9.1.0; **nova**: `com.googlecode.libphonenumber:libphonenumber:9.0.40`

**Storage**: Room v2 (sem mudança de schema; novo `@Transaction replaceAll` no DAO de
contatos) + DataStore (novas chaves `phone_normalization_version` e `last_known_region`)

**Testing**: JUnit 4 + kotlinx-coroutines-test (JVM, fakes de DAO); androidTest de migração
existente inalterado; validação manual com `adb emu gsm call`

**Target Platform**: Android minSdk 26, target/compileSdk 36 (triagem real em API 29+)

**Project Type**: mobile-app (módulo único `:app`)

**Performance Goals**: decisão de chamada < 1 s em aparelho intermediário (SC-005); parse
medido ~0,14 ms após aquecimento

**Constraints**: offline; sem novas permissões de runtime; sem I/O de agenda na triagem;
fail-open; tamanho do APK +~360 KB

**Scale/Scope**: agenda de até alguns milhares de contatos; whitelist pequena (5 grátis,
ilimitada Premium); ~12 arquivos de produção tocados, 3 idiomas

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Avaliação pré-pesquisa | Pós-design |
|---|---|---|
| I. Fail-Open | Feature **corrige** a violação atual (inválido → bloqueio). Plano: inválido segue `blockUnknown`; normalizador nunca lança. | ✅ Tabela de decisão no contrato; falha interna (`INTERNAL_ERROR`) sempre permite; troca do cache de contatos atômica (`@Transaction`); fallback de última região conhecida; renormalizador fora da triagem; aquecimento em IO. |
| II. Privacidade | Lib offline, sem rede, sem permissões novas (região via `TelephonyManager` sem permissão). | ✅ Nenhum log novo com telefone (limpeza dos existentes fica na 004); renormalizador apaga o cache de contatos sem consentimento/permissão. |
| III. Identidade Canônica | Objetivo central da feature. | ✅ Único `PhoneNormalizer` usado em todos os pontos de entrada; E.164 única chave; FR-014 sem chave dupla. |
| IV. Motor Puro e Testado | Normalizador e formatter em JVM puro; motor recebe normalizador por construtor. | ✅ Testes 1:1 com tabelas do contrato; regressão do bug de bloqueio; `INVALID_NUMBER` sai do enum (todo motivo restante tem teste); ordem de região testada via função pura; schema Room inalterado (sem migração nova). |
| V. Localização | Novas strings/ajuda/erros. | ✅ Todas em en, pt-BR, es; placeholder de exemplo vem da região (sem string fixa `+5549…`). |
| VI. Monetização | Contatos continuam Premium no motor. | ✅ Reprocessamento do cache independe de licença (só consentimento + permissão); whitelist sempre permitida. |
| Restrições técnicas | Nova dependência exige justificativa. | ✅ Exceção prevista na constituição; versão em `libs.versions.toml`. |

**Resultado**: PASS, sem violações a justificar.

## Project Structure

### Documentation (this feature)

```text
specs/001-phone-normalization/
├── spec.md
├── plan.md              # este arquivo
├── research.md          # Fase 0
├── data-model.md        # Fase 1
├── quickstart.md        # Fase 1
├── contracts/
│   └── phone-normalizer.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Fase 2 (/speckit-tasks)
```

### Source Code (repository root)

```text
gradle/libs.versions.toml                          # + libphonenumber
app/build.gradle.kts                               # + implementation(libs.libphonenumber)

app/src/main/java/com/lsp/callguard/
├── CallGuardApplication.kt                        # aquecimento + PhoneDataRenormalizer.runIfNeeded()
├── core/phone/PhoneServices.kt                    # NOVO: DI manual (normalizer, formatter, região)
├── domain/
│   ├── phone/
│   │   ├── PhoneNormalizer.kt                     # REESCRITO: object regex → classe libphonenumber
│   │   ├── PhoneNumberDisplayFormatter.kt         # NOVO
│   │   └── RegionProvider.kt                      # NOVO (interface suspend)
│   └── engine/
│       ├── CallDecision.kt                        # + 3 motivos; INVALID_NUMBER removido
│       └── CallDecisionEngine.kt                  # normalizador injetado; inválido → blockUnknown;
│                                                  # INTERNAL_ERROR → permite
├── data/
│   ├── telephony/DeviceRegionProvider.kt          # NOVO (+ função pura resolveRegion)
│   ├── migration/PhoneDataRenormalizer.kt         # NOVO
│   ├── local/dao/AllowedNumberDao.kt              # + getAll(), updatePhone()
│   ├── local/dao/DeviceContactDao.kt              # + @Transaction replaceAll()
│   ├── local/preferences/PhoneDataPreferences.kt  # NOVO: phone_normalization_version, last_known_region
│   └── repository/
│       ├── ContactsRepository.kt                  # usa normalizador + região; mapeamento puro
│       ├── DeviceContactCacheRepository.kt        # replaceAll delega ao DAO transacional
│       └── CallDecisionLogRepository.kt           # usa normalizador + região
├── telecom/screening/AppCallScreeningService.kt   # passa região ao motor
└── ui/screen/
    ├── whitelist/{WhitelistViewModel,WhitelistScreen,WhitelistViewModelFactory}.kt
    │                                              # prévia, validação no VM, exibição formatada
    ├── history/{CallHistoryViewModel,CallHistoryViewModelFactory,CallHistoryScreen}.kt
    │                                              # exibição + novos motivos
    └── contacts/ContactsImportRoute.kt            # injeta RegionProvider no ContactsRepository

app/src/main/res/values{,-pt-rBR,-es-rES}/strings.xml   # ajuda, prévia, erros, motivos,
                                                        # contacts_import_description

app/src/test/java/com/lsp/callguard/
├── domain/phone/PhoneNormalizerTest.kt            # REESCRITO (29 casos do contrato)
├── domain/phone/PhoneNumberDisplayFormatterTest.kt  # NOVO
├── domain/engine/CallDecisionEngineTest.kt        # tabela de decisão (9 linhas) + regressões
├── data/telephony/RegionResolutionTest.kt         # NOVO
├── data/repository/DeviceContactMappingTest.kt    # NOVO
├── data/migration/PhoneDataRenormalizerTest.kt    # NOVO
├── ui/screen/whitelist/WhitelistViewModelTest.kt  # NOVO
└── fakes/{FakeAllowedNumberDao,FakeDeviceContactDao}.kt  # + getAll/updatePhone/replaceAll
```

**Structure Decision**: Módulo Android único (`:app`) com camadas por pacote
(`ui` → `domain` ← `data`, `telecom` orquestra), conforme a constituição. Os novos artefatos de
domínio ficam em `domain/phone`; o acesso ao Android para região fica em `data/telephony`; a
conversão de dados legados em `data/migration`.

## Fases de entrega (alinhadas às user stories)

1. **Fundação**: dependência, `RegionProvider`/`DeviceRegionProvider`/`PhoneDataPreferences`,
   `PhoneNormalizer`, formatter + testes do contrato; migração de todos os chamadores para a
   nova API (projeto compilando). (bloqueia tudo)
2. **US1 (P1)**: motor + serviço + motivos + strings de histórico; regressões. → MVP liberável.
3. **US2 (P2)**: `ContactsRepository` + `PhoneDataRenormalizer` (whitelist + cache) + Application.
4. **US3 (P3)**: cadastro manual com prévia e placeholder por região; validação no ViewModel.
5. **US4 (P3)**: `PhoneNumberDisplayFormatter` em whitelist e histórico (a tela de importação
   de contatos só mostra contagem, não lista números).
6. **Polimento**: textos de ajuda nos 3 idiomas, quickstart manual, gates.

## Riscos

| Risco | Mitigação |
|---|---|
| Metadados da lib desatualizam (novas faixas) | R2 aceita `+` possível; atualizar a lib a cada release. |
| Região errada (chip estrangeiro, sem chip) | Ordem chip → rede → locale → última região conhecida; sem nenhuma, só `+`; fail-open. |
| Renormalizador interrompido no meio | Idempotente; versão gravada só no fim. |
| Triagem durante a troca do cache de contatos | `replaceAll` em uma única transação Room. |
| Falha/bug da lib de números | `INTERNAL_ERROR` → chamada sempre permitida. |
| Conversão do nono dígito em número não-celular | Só aplica com 10 dígitos, assinante 6–9 e resultado `MOBILE` válido (R3). |

## Complexity Tracking

Sem violações da constituição a justificar.
