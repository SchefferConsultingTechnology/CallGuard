# Research: Liberar Número a Partir do Histórico

**Feature**: 004-history-quick-allow | **Date**: 2026-10-15

## R1. Quando a ação aparece (FR-001, FR-005, FR-006)

- **Decision**: Função pura `quickAllowAction(allowed: Boolean, canonical: String?, inWhitelist: Set<String>)`
  → `ADD` | `IN_WHITELIST` | `NONE`. `canonical` = `normalizedPhone` gravado ou, para registros
  antigos sem ele, `PhoneNormalizer.normalize(phone, região)` (regra única da 001).
  - número na whitelist → `IN_WHITELIST` (para qualquer decisão, mesmo bloqueio antigo);
  - bloqueado + canônico → `ADD`; demais (permitido, oculto, não interpretável) → `NONE`.
- **Rationale**: testável em JVM; a mesma identidade da triagem garante que liberar pelo histórico
  realmente libera a próxima chamada (Princípio III).

## R2. Inclusão e "Desfazer" (FR-003, FR-004)

- **Decision**: Reusar `WhitelistRepository.addNumber(label, phoneE164)`; "Desfazer" chama o novo
  `WhitelistRepository.deleteByPhone(phoneE164)` (`DELETE … WHERE phoneE164 = :phone`, sem mudança de
  schema). Nome vazio usa o rótulo padrão existente.
- **Alternatives considered**: devolver o id na inclusão (muda o contrato usado pela whitelist).

## R3. Limite do Grátis (FR-007)

- **Decision**: O ViewModel do histórico observa a licença (feature 003) e a contagem da whitelist;
  no limite, guarda a inclusão pendente (número + nome) e emite um evento que abre o diálogo de
  limite com "Ver planos" (`Routes.Paywall.create(firstRun = false)`). Ao voltar com um nível que
  comporte a inclusão, ela é concluída sozinha e a pessoa recebe a confirmação.
- **Rationale**: o ViewModel da tela sobrevive à ida e volta da oferta (continua na pilha).

## R4. Testes

- **Decision**: JVM para `quickAllowAction` e para o ViewModel (fakes de DAO, DataStore temporário):
  incluir, desfazer, limite com pendência, conclusão após upgrade. Ponta a ponta no emulador com
  `adb emu gsm call`.
