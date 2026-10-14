# Manual Validation: Normalização de Números de Telefone

**Purpose**: Gate de validação manual da constituição (mudanças em triagem, motor e normalizador)
**Feature**: [spec.md](../spec.md) · Roteiro: [quickstart.md](../quickstart.md)

## Ambiente

- **Data**: 2026-10-13
- **Dispositivo**: emulador `Pixel_7_Pro`, Android API 37, SIM `us` (simCountryIso)
- **Build**: `app-debug.apk` da branch `001-phone-normalization` (não commitada)
- **Papel de triagem**: concedido via `cmd role add-role-holder android.app.role.CALL_SCREENING`

## Resultados

- [x] **§1 Gates automatizados** — `testDebugUnitTest` (78 testes, 0 falhas), `lintDebug` (0 erros;
  só avisos pré-existentes), `assembleRelease` OK, `app/schemas/` inalterado.
- [x] **§2 Formato nacional (US1)** — whitelist `6505551212` (formato nacional EUA, aceito pelo
  cadastro); `blockUnknown` ligado:
  - `adb emu gsm call 6505551212` → permitida, `ALLOWED_BY_WHITELIST`
  - `adb emu gsm call 6505550000` → bloqueada, `BLOCKED_NOT_IN_WHITELIST`
  - `adb emu gsm call 12345` → bloqueada, `BLOCKED_UNKNOWN_UNPARSEABLE`
  - `blockUnknown` desligado + `12345` → permitida, `ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED` (SC-002)
- [ ] **§3 Formatos brasileiros em aparelho real com chip BR** — **PENDENTE**: requer aparelho
  físico com chip brasileiro. Cobertura atual apenas por testes unitários (contrato #1–#15,
  regressões do motor com `11…`, `011…`, `015 11…`, `1187654321`).
- [x] **§4 Contatos em formatos mistos (US2)** — contatos `(650) 555-0101`, `6505550101`,
  `+1 650-555-0101`, `*144`, `+351 912 345 678`, `650-555-0102`; premium simulado (debug) +
  consentimento → "3 números válidos" (triplicado deduplicado, `*144` ignorado). Chamada de
  `6505550102` → `ALLOWED_BY_CONTACTS`.
- [x] **§5 Atualização sobre dados antigos (FR-007, FR-011, FR-014)** — versão anterior
  (`development` @ `0a995b0`) com whitelist `+551187654321` (legado 8 dígitos) e
  `+5511987654321`; `adb install -r` da nova versão → whitelist com **uma** entrada
  `+55 11 98765-4321` (legada convertida e duplicata removida), sem ação do usuário.
- [x] **§6 Exibição e idiomas (US3, US4)**:
  - Whitelist exibe `(650) 555-1212` (nacional para a região do aparelho) e
    `+55 11 98765-4321` (internacional para outro país).
  - Histórico exibe números formatados, `12345` como chegou e os novos motivos traduzidos.
  - Diálogo de cadastro em pt-BR e es-ES: rótulo, placeholder pela região (`(201) 555-0123`),
    prévia "Será salvo como +1 202-555-0123" / "Se guardará como …", erro explicativo com botão
    desabilitado, duplicata (`650.555.1212`) não inserida.

## Achados fora do escopo (registrados para próximas features)

- O app sempre abre na tela de idioma (feature 002 planejada).
- ~~Trocar o idioma na tela inicial não aplica a mudança na sessão atual~~ — **corrigido em
  2026-10-14** (fora do Spec Kit, a pedido): `MainActivity` passou a `AppCompatActivity`, tema
  AppCompat e `locales_config`. Validado no emulador API 37 nos 3 idiomas (seleção, onboarding
  e Configurações; mesma sessão, registro no sistema e persistência). Android ≤ 12 não testado.
- "Confirmar" na tela de idioma aberta pelas Configurações leva ao onboarding em vez de voltar
  (fluxo de navegação, feature 002).
- Logs de triagem continuam com número de telefone (`Log.d`), pré-existente (feature 004).
