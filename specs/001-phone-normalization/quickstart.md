# Quickstart: validar a Normalização de Números

**Feature**: 001-phone-normalization

## Pré-requisitos

- JDK 21+, Android SDK, `adb` no PATH.
- Emulador com API 29+ (ex.: Pixel, API 36) **ou** aparelho real com chip brasileiro.
- App instalado e definido como app de triagem de chamadas (Tela "Proteção" → conceder papel).

## 1. Gates automatizados (constituição)

```bash
./gradlew testDebugUnitTest     # PhoneNormalizerTest, PhoneNumberDisplayFormatterTest,
                                # CallDecisionEngineTest, PhoneDataRenormalizerTest
./gradlew lintDebug
./gradlew assembleRelease
git diff --exit-code app/schemas/   # schema Room v2 inalterado (research R8)
```

Esperado: todos verdes; as tabelas de
[contracts/phone-normalizer.md](contracts/phone-normalizer.md) cobertas 1:1 por testes
(SC-001: ≥ 20 formatos BR + ≥ 5 internacionais).

## 2. Chamada em formato nacional (US1) — emulador

O SIM do emulador é dos EUA (`simCountryIso = us`), então o teste ponta a ponta usa formato
nacional americano:

1. Whitelist → adicionar `(650) 555-1212`. Esperado: prévia `+1 650-555-1212`, salva.
2. Configurações → "bloquear desconhecidos" **ligado**.
3. `adb emu gsm call 6505551212` → chamada **toca**; Histórico: "permitido pela whitelist".
4. `adb emu gsm call 6505550000` → chamada **rejeitada**; Histórico: "bloqueado — não está na
   whitelist".
5. `adb emu gsm call 12345` → **rejeitada** como desconhecido não interpretável.
6. Desligar "bloquear desconhecidos" e repetir o passo 5 → chamada **toca** (SC-002).

## 3. Formatos brasileiros — aparelho real com chip BR

Repetir o roteiro 2 com whitelist `11 98765-4321` (número de teste próprio) e ligar a partir de
outro aparelho, inclusive via código de operadora (`015 11 …`). Esperado: sempre "permitido
pela whitelist". Registrar o resultado no checklist da feature (gate de validação manual).

## 4. Contatos em formatos mistos (US2)

1. Criar contatos: `(11) 98765-4321`, `11987654321`, `+55 11 98765-4321` (mesmo número),
   `(11) 8765-4322` (8 dígitos), `*144`, um número estrangeiro com `+`.
2. Consentimento → importar. Esperado: 3 números distintos importados (o triplicado vira 1, o
   de 8 dígitos vira `+55 11 98765-4322`, `*144` ignorado).

## 5. Atualização sobre dados antigos (FR-007, FR-011, FR-014)

1. Instalar a versão anterior (`master`), cadastrar `+551187654321` e `+5511987654321`,
   importar contatos.
2. Instalar a nova versão por cima (`adb install -r`), abrir o app.
3. Esperado: whitelist com **uma** entrada `(11) 98765-4321` (legada convertida e duplicata
   removida); cache de contatos reprocessado; nenhuma ação do usuário.
4. Abrir de novo: nada muda (idempotente; `phone_normalization_version = 1`).

## 6. Exibição (US4) e idiomas (Princípio V)

- Whitelist/histórico mostram `(11) 98765-4321` para números do país e `+351 912 345 678` para
  estrangeiros; não interpretáveis aparecem como chegaram.
- Trocar o idioma para pt-BR, en e es: textos de ajuda, prévia e erros traduzidos.
