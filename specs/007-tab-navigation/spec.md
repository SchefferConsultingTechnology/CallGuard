# Feature Specification: Navegação por Abas

**Feature Branch**: `007-tab-navigation`

**Created**: 2026-10-15

**Status**: Implemented

**Input**: User description: "Não é uma aplicação grande, mas o formato de ter que rolar muito na tela não
me agrada. Vamos seguir com o menu, com 4 abas."

## Contexto

Hoje o CallGuard organiza tudo em duas rolagens longas:

- **Início**: 7 cards empilhados. São eles proteção, histórico recente, plano, whitelist, contatos,
  "sem anúncios" e, às vezes, o aviso de mudança de plano. Para chegar à whitelist ou ao histórico,
  a pessoa rola e toca num botão dentro de um card.
- **Configurações**: 7 seções numa única tela. São elas conta, proteção, contatos, cópia de
  segurança, privacidade, app e sobre. Para chegar a "Aparência", é preciso rolar quase até o fim.

Esta feature troca esse formato por uma **barra de navegação inferior com 4 abas**: Início, Whitelist,
Histórico e Ajustes. Os destinos principais ficam a um toque e cada tela fica curta.

A marca (logo nas telas) fica fora do escopo e será tratada depois, na feature 006.

## Estrutura

```text
┌──────────── Abas (barra inferior sempre visível) ────────────┐
│ Início      painel: proteção, whitelist + plano, 3 últimas    │
│ Whitelist   lista de números permitidos (tela atual)          │
│ Histórico   chamadas analisadas (tela atual)                  │
│ Ajustes     lista de categorias → página de cada categoria    │
└───────────────────────────────────────────────────────────────┘
┌──────────── Abrem por cima, sem a barra ─────────────────────┐
│ primeiro uso (apresentação + oferta) · oferta de planos ·     │
│ ativação da proteção · consentimento e importação de contatos │
│ · termos · política de privacidade · seleção de idioma        │
└───────────────────────────────────────────────────────────────┘
```

**Categorias de Ajustes**, cada uma com uma linha de resumo do estado atual:

| Categoria | Conteúdo (cards atuais reaproveitados) | Resumo na lista (exemplo) |
|---|---|---|
| Plano e compras | plano atual, "Ver planos", restaurar compras, benefícios; seletor de plano simulado (só em desenvolvimento) | "Grátis" |
| Proteção de chamadas | bloquear desconhecidos, bloquear números privados | "Desconhecidos e privados bloqueados" |
| Contatos | usar contatos automaticamente | "Disponível no Plus" / "Ativado" |
| Cópia de segurança | exportar e importar arquivo protegido | "Arquivo protegido por senha" |
| Aparência e idioma | Sistema · Claro · Escuro; idioma | "Escuro · Português" |
| Privacidade e termos | privacidade em primeiro lugar (inclui "sem anúncios"), termos de uso, política de privacidade | "Sem anúncios, dados no aparelho" |

A versão do app aparece no rodapé da lista de Ajustes.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ir direto ao que preciso com um toque (Priority: P1)

Depois do primeiro uso, o app abre no Início com a barra de abas. Com um toque, a pessoa vai para
Whitelist, Histórico ou Ajustes, e cada aba lembra onde ela estava.

**Why this priority**: É a mudança que elimina a rolagem para chegar às áreas principais. É a base de
tudo o mais.

**Independent Test**: Abrir o app, tocar em cada aba e conferir a tela certa. Rolar o histórico, ir para
Whitelist, voltar ao Histórico e conferir que a posição se manteve. Usar o botão voltar a partir de
cada aba.

**Acceptance Scenarios**:

1. **Given** o primeiro uso já concluído, **When** o app abre, **Then** mostra o Início com a barra de 4
   abas e a aba Início marcada.
2. **Given** qualquer aba, **When** a pessoa toca em outra aba, **Then** a tela correspondente aparece
   sem animação de "nova tela" empilhada e a aba tocada fica marcada.
3. **Given** a pessoa rolou o Histórico, **When** vai para outra aba e volta, **Then** o Histórico está
   na mesma posição.
4. **Given** a aba Whitelist, Histórico ou Ajustes, **When** a pessoa usa o voltar do sistema, **Then**
   vai para o Início.
5. **Given** o Início, **When** a pessoa usa o voltar, **Then** o app fecha.
6. **Given** a pessoa já está numa aba, **When** toca de novo na mesma aba, **Then** a tela volta ao topo
   (e, em Ajustes, volta da página da categoria para a lista).
7. **Given** Whitelist e Histórico como abas, **When** exibidos, **Then** não têm seta de voltar.

---

### User Story 2 - Início que cabe numa tela (Priority: P1)

O Início vira um painel compacto, com estes itens:
- **Estado da proteção**: ativa ou com chamada para ativar.
- **Dois cards lado a lado**: whitelist (uso, ex.: "3 de 5") e plano (nível atual).
- **As 3 últimas chamadas analisadas**: número, permitida ou bloqueada, e hora.
- **Aviso de mudança de plano**, quando existir.

**Why this priority**: É a tela vista toda vez que o app abre. Hoje é a que mais exige rolagem.

**Independent Test**: Com proteção ativa, plano Grátis, 3 números e chamadas no histórico, abrir o
Início num celular de 6,1" com fonte padrão e conferir que tudo aparece sem rolar.

**Acceptance Scenarios**:

1. **Given** proteção ativa, **When** o Início abre, **Then** mostra o estado da proteção, os cards de
   whitelist e plano lado a lado e as 3 últimas chamadas, sem precisar rolar.
2. **Given** a proteção não configurada, **When** o Início abre, **Then** o card de proteção oferece
   ativá-la (abre a ativação por cima).
3. **Given** o card de whitelist ou o de plano, **When** a pessoa toca nele, **Then** vai,
   respectivamente, para a aba Whitelist ou para a oferta de planos.
4. **Given** uma das últimas chamadas, **When** a pessoa toca nela ou em "Ver tudo", **Then** vai para a
   aba Histórico.
5. **Given** nenhuma chamada analisada ainda, **When** o Início abre, **Then** a área de chamadas mostra
   uma mensagem curta de "nenhuma chamada ainda" no lugar da lista.
6. **Given** um aviso de mudança de plano pendente, **When** o Início abre, **Then** o aviso aparece no
   topo do painel até a pessoa tocar em "Entendi" (comportamento da feature 003).
7. **Given** o Início, **When** exibido, **Then** não tem os cards de contatos nem de "sem anúncios"
   (passam a Ajustes), nem botões "Gerenciar whitelist"/"Ver histórico".

---

### User Story 3 - Ajustes organizados por categoria (Priority: P2)

A aba Ajustes mostra uma lista curta das 6 categorias, cada uma com um resumo do estado atual. Tocar
numa categoria abre a página dela com os controles de hoje.

**Why this priority**: Resolve a rolagem longa das Configurações. Depende da barra de abas (US1).

**Independent Test**: Abrir Ajustes, conferir as 6 categorias e os resumos. Entrar em cada uma, alterar
algo (ex.: tema Escuro) e conferir que o resumo da lista muda ao voltar.

**Acceptance Scenarios**:

1. **Given** a aba Ajustes, **When** aberta, **Then** mostra as 6 categorias e a versão do app sem
   precisar rolar.
2. **Given** uma categoria, **When** a pessoa toca nela, **Then** abre a página da categoria com
   título, seta de voltar e a barra de abas ainda visível.
3. **Given** a página de uma categoria, **When** a pessoa usa a seta ou o voltar do sistema, **Then**
   volta para a lista de Ajustes.
4. **Given** a pessoa mudou um ajuste (ex.: bloquear números privados), **When** volta à lista,
   **Then** o resumo da categoria reflete a mudança.
5. **Given** "Privacidade e termos", **When** aberta, **Then** a garantia de "sem anúncios" aparece uma
   única vez no app.

---

### User Story 4 - Telas de tarefa abrem por cima e voltam para onde eu estava (Priority: P2)

Algumas telas são tarefas pontuais e abrem por cima das abas, sem a barra. A pessoa conclui ou cancela
e volta exatamente para a aba e a página de onde saiu. São elas:
- a oferta de planos;
- a ativação da proteção;
- o consentimento e a importação de contatos;
- os termos e a política de privacidade;
- a seleção de idioma.

**Why this priority**: Sem isso, concluir uma tarefa poderia jogar a pessoa no lugar errado ou duplicar
telas.

**Independent Test**: Partir de cada aba para cada tela de tarefa e conferir o retorno.

**Acceptance Scenarios**:

1. **Given** a aba Whitelist com o limite atingido, **When** a pessoa abre a oferta e volta, **Then**
   está de novo na Whitelist.
2. **Given** Ajustes → Contatos, **When** a pessoa aceita o consentimento e a importação termina,
   **Then** volta para Ajustes → Contatos com o ajuste ativado.
3. **Given** o Histórico com uma inclusão pendente por limite do plano (feature 004), **When** a pessoa
   assina pela oferta e volta, **Then** está no Histórico e a inclusão é concluída.
4. **Given** uma tela de tarefa aberta, **When** exibida, **Then** a barra de abas não aparece.
5. **Given** o primeiro uso (apresentação e oferta), **When** em andamento, **Then** não há barra de
   abas. Ao concluir, o app vai para o Início com a barra.

### Edge Cases

- **Abrir o app de novo**:
  - Pelo ícone, abre no Início.
  - Se o sistema encerrou o app em segundo plano e a pessoa volta pelos recentes, a última aba é
    restaurada. É o comportamento padrão do Android e evita perder o que estava sendo feito.
- **Girar a tela ou trocar o tema** (feature 006): a aba e a página atuais são mantidas.
- **Trocar o idioma** em Ajustes → Aparência e idioma: ao voltar da seleção de idioma, a pessoa está em
  Ajustes → Aparência e idioma, já no novo idioma.
- **Fonte do sistema grande (200%)**: os rótulos das abas não são cortados de forma ilegível. Se não
  couberem, ficam só os ícones na aba não selecionada, mantendo o nome para leitores de tela.
- **Plano muda enquanto a pessoa está em outra aba**: o aviso de mudança de plano aparece no Início na
  próxima vez que ele for aberto.
- **Whitelist vazia / histórico vazio**: as abas mostram os estados vazios atuais.
- **Ação principal da Whitelist** ("Adicionar número"): continua visível acima da barra de abas, sem ser
  coberta por ela.
- **Snackbars** (ex.: "Desfazer" no histórico, resultado do backup): aparecem acima da barra de abas.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Depois do primeiro uso, o app MUST exibir uma barra de navegação inferior com 4 abas,
  nesta ordem: Início, Whitelist, Histórico, Ajustes. Cada aba tem ícone e nome.
- **FR-002**: Tocar numa aba MUST mostrar a tela correspondente e marcar a aba. Cada aba MUST preservar
  seu estado (posição de rolagem, página aberta em Ajustes) ao trocar de aba.
- **FR-003**: O voltar do sistema MUST levar ao Início a partir de outra aba e fechar o app a partir do
  Início. Dentro de uma página de categoria de Ajustes, voltar MUST retornar à lista de Ajustes.
- **FR-004**: Tocar na aba já selecionada MUST levar a tela ao topo e, em Ajustes, de volta à lista.
- **FR-005**: O Início MUST mostrar, sem rolar num celular de 6,1" com fonte padrão e sem o aviso de
  mudança de plano (que é raro e temporário; com ele, rolar é aceitável):
  - o estado da proteção;
  - os cards de whitelist e plano lado a lado;
  - as 3 últimas chamadas analisadas.
  O aviso de mudança de plano MUST aparecer quando pendente.
- **FR-006**: O Início MUST NOT exibir os cards de contatos e de "sem anúncios" nem os botões de navegar
  para whitelist e histórico.
- **FR-007**: Os cards e as chamadas do Início MUST ser atalhos:
  - whitelist → aba Whitelist;
  - plano → oferta de planos;
  - chamada ou "Ver tudo" → aba Histórico.
- **FR-008**: Whitelist e Histórico MUST manter todas as funções atuais (incluindo adicionar, remover,
  liberar pelo histórico e limpar histórico), sem seta de voltar.
- **FR-009**: A aba Ajustes MUST listar as 6 categorias da tabela, cada uma com resumo do estado atual,
  e a versão do app. Cada categoria MUST abrir uma página com seus controles, título e voltar.
- **FR-010**: Todos os ajustes existentes hoje MUST continuar disponíveis em alguma categoria, sem perda
  de função. A garantia de "sem anúncios" MUST aparecer uma única vez.
- **FR-011**: As telas de tarefa listadas na US4 e o primeiro uso MUST abrir sem a barra de abas e, ao
  concluir ou cancelar, MUST voltar para a aba e a página de origem.
- **FR-012**: A ação principal da Whitelist e as mensagens temporárias MUST aparecer acima da barra de
  abas, sem serem cobertas.
- **FR-013**: A barra de abas MUST seguir o esquema de cores e os temas claro/escuro da feature 006.
- **FR-014**: Textos novos MUST existir em inglês, português do Brasil e espanhol. São eles:
  - os nomes das abas;
  - os nomes e resumos das categorias;
  - "Ver tudo";
  - "nenhuma chamada ainda".

### Key Entities

- **Aba**: Início, Whitelist, Histórico ou Ajustes, cada uma com ícone, nome e estado próprio.
- **Categoria de Ajustes**: nome, ícone, resumo do estado atual e página com os controles.
- **Tela de tarefa**: tela que abre por cima das abas e devolve a pessoa à origem.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Whitelist, Histórico e Ajustes são alcançados com 1 toque a partir de qualquer aba.
- **SC-002**: Qualquer ajuste é alcançado em no máximo 2 toques a partir de qualquer aba (aba Ajustes →
  categoria). Hoje, "Aparência" exige rolar a tela inteira de Configurações.
- **SC-003**: O Início (sem o aviso de mudança de plano) e a lista de Ajustes cabem sem rolagem num
  celular de 6,1" com fonte padrão, nos 3
  idiomas.
- **SC-004**: 100% das telas de tarefa, abertas de cada aba, voltam para a aba e a página de origem.
- **SC-005**: Nenhuma função existente é perdida. Todos os roteiros de validação das features 001–006
  que passam por Home, Whitelist, Histórico e Configurações continuam executáveis pelo novo caminho.

## Assumptions

- Barra inferior (e não menu lateral): 4 destinos cabem e ficam a um toque do polegar, que é o padrão
  do Material 3 para 3 a 5 destinos.
- Aberto pelo ícone, o app começa no Início. A última aba não é lembrada, exceto na restauração
  automática pelos recentes.
- As telas de Whitelist e Histórico mudam o mínimo: perdem a seta de voltar e passam a respeitar o
  espaço da barra.
- As páginas de Ajustes reaproveitam os cards atuais (alinhados recentemente). Não há novos ajustes
  nesta feature.
- A oferta de planos continua uma tela própria por cima (é uma tarefa de compra) e não vira aba.
- Logo e marca nas telas ficam para a retomada da feature 006.
- Depende da feature 006 (paleta e temas), implementada no mesmo branch e ainda sem commit.
  Recomenda-se commitar a 006 antes de implementar esta.
