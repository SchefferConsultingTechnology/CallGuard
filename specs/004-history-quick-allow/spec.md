# Feature Specification: Liberar Número a Partir do Histórico

**Feature Branch**: `004-history-quick-allow`

**Created**: 2026-10-14

**Status**: Draft

**Input**: User description: "Na lista de histórico de números bloqueados, deve ter uma ação no
número que permita adicioná-lo à whitelist."

## Contexto

Quando o CallGuard bloqueia alguém que a pessoa queria ouvir (um novo médico, a escola, um
entregador), hoje ela precisa anotar o número, abrir a whitelist e digitá-lo. O histórico já mostra
o número bloqueado; esta feature permite liberá-lo dali com um toque.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Liberar um número bloqueado pelo histórico (Priority: P1)

No histórico, a pessoa toca na ação "Adicionar à whitelist" de uma chamada bloqueada, confirma (com
um nome opcional) e as próximas chamadas desse número passam a tocar.

**Why this priority**: É o pedido central e corrige um bloqueio indesejado no momento em que a
pessoa percebe.

**Independent Test**: Simular uma chamada bloqueada, usar a ação no histórico e repetir a chamada:
ela deve tocar.

**Acceptance Scenarios**:

1. **Given** uma chamada bloqueada de `(11) 98765-4321` no histórico, **When** a pessoa toca em
   "Adicionar à whitelist" e confirma, **Then** o número entra na whitelist e a próxima chamada
   dele é permitida.
2. **Given** a confirmação aberta, **When** a pessoa informa um nome ("Clínica"), **Then** a
   entrada na whitelist usa esse nome; sem nome, usa o rótulo padrão do app.
3. **Given** o número foi adicionado, **When** a pessoa volta ao histórico, **Then** todas as
   chamadas desse número aparecem marcadas como "na whitelist" e a ação não é mais oferecida para
   elas.
4. **Given** a pessoa adicionou por engano, **When** toca em "Desfazer" no aviso de confirmação,
   **Then** o número sai da whitelist.

---

### User Story 2 - Respeitar o limite do plano Grátis (Priority: P2)

No plano Grátis com a whitelist cheia (5 números), a ação leva à oferta do Plus em vez de falhar
em silêncio.

**Why this priority**: Mantém a regra dos planos (feature 003) e é um momento natural de upgrade.

**Independent Test**: No Grátis com 5 números, usar a ação: a oferta aparece; após assinar, a
inclusão é concluída.

**Acceptance Scenarios**:

1. **Given** plano Grátis com 5 números, **When** a pessoa usa a ação, **Then** vê a explicação do
   limite e a opção de conhecer o Plus.
2. **Given** a pessoa assinou o Plus a partir desse ponto, **When** volta ao histórico, **Then**
   pode concluir a inclusão sem redigitar nada.

### Edge Cases

- **Número oculto/privado**: não há o que liberar; a ação não aparece.
- **Número não interpretável** (ex.: `12345`, `*144`): a ação não aparece; ao tocar no item, a
  pessoa vê por que não é possível liberá-lo.
- **Número já na whitelist** (adicionado por outro caminho): a ação não aparece e o item indica
  "na whitelist".
- **Chamadas permitidas no histórico**: a ação não aparece (o pedido é para bloqueadas). Quem foi
  liberado só por "bloquear desconhecidos" desligado continua podendo ser adicionado pela
  whitelist normal.
- **Mesmo número várias vezes no histórico**: liberar uma vez vale para todas as ocorrências.
- **Histórico antigo com formato diferente**: o número é reconhecido pela mesma regra de
  identificação da feature 001, independentemente de como foi gravado.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Cada chamada bloqueada no histórico cujo número possa ser identificado MUST oferecer
  a ação "Adicionar à whitelist".
- **FR-002**: A ação MUST pedir confirmação com campo de nome opcional e mostrar o número no
  formato legível.
- **FR-003**: Após confirmar, o número MUST ser incluído na whitelist pela mesma regra de
  identificação usada no cadastro manual, e chamadas seguintes dele MUST ser permitidas.
- **FR-004**: O sistema MUST oferecer "Desfazer" logo após a inclusão.
- **FR-005**: Itens do histórico cujo número está na whitelist MUST indicar isso e MUST NOT
  oferecer a ação.
- **FR-006**: A ação MUST NOT aparecer para números ocultos, não interpretáveis ou chamadas
  permitidas.
- **FR-007**: No plano Grátis com a whitelist no limite, a ação MUST levar à explicação do limite e
  à oferta do Plus, preservando o número escolhido para concluir depois do upgrade.
- **FR-008**: Textos novos MUST existir em inglês, português do Brasil e espanhol.

### Key Entities

- **Entrada do histórico**: número como chegou, número identificado (quando houver), decisão e
  motivo; passa a exibir se o número está na whitelist.
- **Entrada da whitelist**: nome e número identificado (existente).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Liberar um número bloqueado leva no máximo 2 toques a partir do histórico.
- **SC-002**: 100% das chamadas seguintes de um número liberado pelo histórico são permitidas.
- **SC-003**: Nenhum item de número oculto ou não interpretável oferece a ação.

## Assumptions

- Escopo restrito a chamadas **bloqueadas**, como pedido; ampliar para permitidas fica para depois.
- O limite e a oferta dependem dos planos da feature 003; se a 004 for entregue antes, vale o
  limite atual (5 números no Grátis) e a oferta atual.
