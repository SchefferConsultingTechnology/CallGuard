# Feature Specification: Planos Plus e Pro

**Feature Branch**: `003-premium-plans`

**Created**: 2026-10-14

**Status**: Draft

**Input**: User description: "Níveis Grátis / Plus / Pro com os valores recomendados (Plus R$ 6,90/mês,
R$ 49,90/ano, R$ 99,90 vitalício; Pro R$ 11,90/mês, R$ 89,90/ano), teste grátis de 7 dias no anual,
oferta honesta e estados de assinatura tratados."

## Contexto

Hoje o CallGuard tem um único "Premium" que não pode ser comprado (o produto não existe na loja),
uma licença do tipo sim/não e uma oferta que anuncia um recurso inexistente ("Backup e
restauração") e apresenta como exclusivo algo que todos têm ("Sem anúncios"). Esta feature cria os
níveis decididos em 2026-10-14 e torna a compra real e honesta (constituição v2.0.0, Princípio VI).
O backup no Google Drive, exclusivo do Pro, é a feature 005; até ela existir o Pro não é vendido.

## Planos

| Recurso | Grátis | Plus | Pro |
|---|---|---|---|
| Bloquear desconhecidos e números ocultos | ✅ | ✅ | ✅ |
| Whitelist manual | até 5 números | ilimitada | ilimitada |
| Contatos da agenda como permitidos | — | ✅ | ✅ |
| Histórico de chamadas analisadas | últimos 7 dias | últimos 90 dias | últimos 365 dias |
| Backup automático no Google Drive e vários aparelhos | — | — | ✅ (feature 005) |
| Sem anúncios e sem rastreamento | ✅ | ✅ | ✅ |

| Plano | Mensal | Anual | Vitalício |
|---|---|---|---|
| Plus | R$ 6,90 | R$ 49,90 (7 dias grátis) | R$ 99,90 (compra única) |
| Pro | R$ 11,90 | R$ 89,90 (7 dias grátis) | — |

Preços de referência no Brasil; em outros países a loja converte e exibe na moeda local.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Assinar o Plus e liberar os recursos (Priority: P1)

Uma pessoa no plano Grátis atinge o limite de 5 números ou quer liberar a agenda, abre a oferta,
escolhe Plus (mensal, anual ou vitalício), paga pela loja do Google e os recursos do Plus ficam
ativos na hora.

**Why this priority**: É a primeira fonte de receita e destrava os recursos mais pedidos.

**Independent Test**: Com conta de teste da loja, comprar cada uma das três opções do Plus e
verificar whitelist ilimitada, contatos liberados e histórico de 90 dias.

**Acceptance Scenarios**:

1. **Given** plano Grátis com 5 números na whitelist, **When** a pessoa assina o Plus mensal,
   **Then** pode adicionar o 6º número sem ver a oferta.
2. **Given** a pessoa assinou o Plus anual, **When** abre a oferta de novo, **Then** vê seu plano
   atual indicado e não é convidada a comprar o mesmo plano.
3. **Given** a pessoa comprou o Plus vitalício, **When** passa um ano, **Then** o Plus continua
   ativo sem cobrança.
4. **Given** a pessoa assinou em um aparelho, **When** instala o app em outro com a mesma conta
   Google, **Then** o plano é reconhecido automaticamente (ou com "Restaurar compras").

---

### User Story 2 - Oferta honesta comparando os planos (Priority: P1)

A oferta mostra os três níveis lado a lado, com o que cada um inclui e os preços reais da loja na
moeda da pessoa, destaca o anual com 7 dias grátis e nunca anuncia algo que não existe.

**Why this priority**: Sem uma oferta clara e verdadeira a compra não acontece e a constituição é
violada.

**Independent Test**: Abrir a oferta nos 3 idiomas e conferir planos, preços vindos da loja,
indicação do teste grátis e ausência de recursos inexistentes.

**Acceptance Scenarios**:

1. **Given** a oferta aberta, **When** a pessoa a lê, **Then** vê Grátis, Plus e Pro com os
   recursos da tabela de planos e "Sem anúncios" indicado como comum a todos.
2. **Given** o backup no Drive ainda não está disponível, **When** a oferta é exibida, **Then** o
   Pro não aparece à venda (ou aparece como "em breve", sem botão de compra).
3. **Given** a loja não respondeu (sem internet), **When** a oferta é aberta, **Then** a pessoa vê
   os planos sem preços, uma mensagem para tentar de novo e pode continuar no Grátis.
4. **Given** a pessoa nunca usou o teste grátis, **When** vê o plano anual, **Then** ele indica
   "7 dias grátis"; **Given** já usou, **Then** essa indicação não aparece.

---

### User Story 3 - Perder ou cancelar o plano sem perder proteção (Priority: P2)

Quando a assinatura é cancelada, expira, fica com pagamento pendente ou é reembolsada, o app volta
ao Grátis de forma previsível: nenhum número já cadastrado deixa de ser protegido e a pessoa é
avisada do que mudou.

**Why this priority**: Evita bloquear quem a pessoa confia (Princípio I) e reduz reclamações.

**Independent Test**: Com assinatura de teste, simular cancelamento, falha de pagamento e
reembolso e conferir o comportamento do app em cada estado.

**Acceptance Scenarios**:

1. **Given** Plus com 12 números na whitelist, **When** o plano termina, **Then** os 12 números
   continuam permitidos, mas novos números só podem ser adicionados quando houver menos de 5.
2. **Given** pagamento recusado em período de carência da loja, **When** a pessoa abre o app,
   **Then** os recursos continuam ativos e um aviso pede para atualizar a forma de pagamento.
3. **Given** a assinatura está suspensa por falta de pagamento, **When** a pessoa abre o app,
   **Then** o app se comporta como Grátis e mostra como reativar.
4. **Given** compra com pagamento pendente (ex.: boleto), **When** ainda não foi confirmada,
   **Then** o app informa "pagamento em processamento" e só libera os recursos após confirmação.
5. **Given** o plano terminou, **When** a pessoa tinha contatos liberados, **Then** os contatos
   deixam de ser liberados automaticamente e o app explica o motivo.

---

### User Story 4 - Assinar o Pro e mudar de plano (Priority: P3)

Quando o backup no Drive (005) existir, a pessoa pode assinar o Pro, migrar do Plus para o Pro, do
mensal para o anual, ou voltar para um plano menor, com a loja cuidando do valor proporcional.

**Why this priority**: Depende da 005 para ter valor real.

**Independent Test**: Com conta de teste, migrar Plus mensal → Pro anual e Pro → Plus.

**Acceptance Scenarios**:

1. **Given** Plus mensal ativo, **When** a pessoa assina o Pro anual, **Then** o Plus é
   substituído (sem duas cobranças) e os recursos do Pro ficam ativos.
2. **Given** Plus vitalício, **When** a pessoa assina o Pro, **Then** passa a ter o Pro enquanto a
   assinatura durar e, ao terminar, volta ao Plus vitalício (nunca ao Grátis).
3. **Given** Pro ativo, **When** a pessoa troca para o Plus, **Then** a mudança vale ao fim do
   período já pago.

### Edge Cases

- **Mais de uma compra ativa** (ex.: Plus vitalício + Pro): vale o nível mais alto.
- **Relógio do aparelho errado**: o estado do plano vem da loja, não do relógio local.
- **Sem internet ao abrir o app**: usa o último estado conhecido; quem pagou não é rebaixado
  apenas por estar offline.
- **Histórico além da retenção do plano** (ex.: ao voltar ao Grátis): registros mais antigos que a
  retenção do plano atual são apagados; o aviso de rebaixamento informa isso.
- **Reembolso do vitalício**: o Plus é removido e o app volta ao Grátis (regras da US3).
- **Modo de testes do desenvolvedor**: o seletor de plano fictício existe apenas em builds de
  desenvolvimento e permite escolher Grátis, Plus ou Pro.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST oferecer os planos Grátis, Plus e Pro com os recursos da tabela de
  planos desta spec.
- **FR-002**: O Plus MUST ser vendido como assinatura mensal, assinatura anual e compra única
  vitalícia; o Pro como assinatura mensal e anual.
- **FR-003**: Os planos anuais MUST oferecer 7 dias grátis a quem ainda não usou o teste.
- **FR-004**: Preços MUST ser exibidos como informados pela loja, na moeda e formato da pessoa;
  o app MUST NOT exibir preços fixos no próprio texto.
- **FR-005**: O plano vigente MUST ser determinado pelas compras da conta na loja; com várias
  compras válidas, vale o nível mais alto.
- **FR-006**: O app MUST tratar os estados: ativo, carência, suspenso, pendente, cancelado (ativo
  até o fim do período), expirado e reembolsado, com o comportamento da US3.
- **FR-007**: Ao perder um nível, números já cadastrados na whitelist MUST continuar permitidos;
  novas inclusões seguem o limite do nível atual.
- **FR-008**: O histórico MUST respeitar a retenção do nível atual (7/90/365 dias), apagando
  registros mais antigos.
- **FR-009**: A oferta MUST mostrar os três níveis, indicar o plano atual e MUST NOT anunciar
  recursos inexistentes nem apresentar como exclusivo algo disponível em níveis inferiores.
- **FR-010**: O Pro MUST permanecer indisponível para compra até a feature 005 (backup) estar
  publicada.
- **FR-011**: A pessoa MUST poder restaurar compras manualmente e abrir a gestão de assinaturas da
  loja a partir do app.
- **FR-012**: Sem resposta da loja, o app MUST manter o último plano conhecido e permitir seguir no
  Grátis a partir da oferta.
- **FR-013**: A oferta MUST continuar dispensável ("continuar grátis") e não bloquear funções
  gratuitas.
- **FR-014**: Textos novos, inclusive termos de assinatura, MUST existir em inglês, português do
  Brasil e espanhol.

### Key Entities

- **Nível (Tier)**: Grátis, Plus ou Pro; define limites e recursos.
- **Produto da loja**: Plus (assinatura com planos mensal/anual), Plus vitalício (compra única),
  Pro (assinatura com planos mensal/anual).
- **Direito (Entitlement)**: nível vigente + estado (ativo, carência, suspenso, pendente,
  cancelado, expirado) + validade; derivado das compras da loja e guardado localmente como cache.
- **Política de retenção**: dias de histórico por nível.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das compras de teste (6 combinações de plano) liberam os recursos certos em até
  5 segundos após a confirmação da loja.
- **SC-002**: Nenhum número da whitelist deixa de ser permitido após cancelamento, expiração ou
  reembolso, em todos os cenários da US3.
- **SC-003**: A oferta não contém nenhum recurso inexistente e exibe preços da loja nos 3 idiomas.
- **SC-004**: Uma pessoa entende qual plano escolher e conclui a compra em até 3 toques a partir da
  oferta.
- **SC-005**: Reinstalar o app com a mesma conta recupera o plano em 100% dos casos de teste.

## Assumptions

- Retenção do Pro em 365 dias (e não "ilimitado", como na proposta inicial) para cumprir a regra da
  constituição de que o histórico tem retenção definida.
- Sem servidor de verificação de compras (constituição v2.0.0): a validação é a da própria loja no
  aparelho; o risco de fraude é aceito.
- Os produtos e preços são cadastrados no Play Console pelo responsável do app; esta feature
  define os identificadores e o comportamento.
- Exportar e importar dados manualmente é tratado na feature 005.
