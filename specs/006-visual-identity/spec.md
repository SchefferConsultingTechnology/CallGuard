# Feature Specification: Identidade Visual e Temas Claro/Escuro

**Feature Branch**: `006-visual-identity`

**Created**: 2026-10-15

**Status**: Implemented (1.7.3)

**Input**: User description: "A imagem em destaque é a escolhida como logo da aplicação. Desenvolver um
esquema de cores para a aplicação: fundo, cards, botões, botões desabilitados, fonte de textos, textos
destacados, etc. Possibilidade de tema claro e escuro."

## Contexto

O CallGuard hoje usa o ícone padrão de projeto Android e uma paleta azul genérica, só em tema claro.
Foi escolhida a logo da prancha de identidade ([assets/logo-board.png](assets/logo-board.png)), na
variação **1 · Azul Confiança**:

- **Símbolo**: "G" hexagonal em fita, azul com gradiente para ciano, sobre fundo navy.
- **Wordmark**: "Call" em branco/tinta + "Guard" em azul.
- **Tagline**: "Privacidade em cada chamada".
- **Ícone da Google Play**: somente o símbolo, sem texto, sobre fundo navy.

Esta feature leva essa identidade para o app: ícone, abertura, um esquema de cores completo derivado
da logo e os temas claro e escuro, que a pessoa pode escolher.

## Esquema de cores

Valores de referência. Todas as combinações de texto foram conferidas: contraste mínimo de 4,5:1
(WCAG AA).

**Marca**

| Cor | Valor | Uso |
|---|---|---|
| Navy | `#0B1F3B` | fundo do ícone, texto principal no tema claro |
| Azul Confiança | `#2563EB` | botões primários, switches ligados, "Guard"; no tema escuro vira `#5B9BFA` (mais claro, para ser legível sobre o navy) |
| Ciano | `#22D3EE` | segunda cor do gradiente do símbolo; detalhes decorativos (nunca texto) |

**Papéis de cor por tema**

| Papel | Tema claro | Tema escuro | Contraste do texto |
|---|---|---|---|
| Fundo da tela | `#F5F7FB` | `#071426` | — |
| Card / superfície | `#FFFFFF` | `#0F2747` | — |
| Borda do card | `#DCE3EE` | `#1F3A5F` | — |
| Texto principal | `#0B1F3B` | `#E6EDF7` | 15,4 / 12,7 |
| Texto secundário | `#4A5A75` | `#9DB0CC` | 7,0 / 6,8 |
| Texto destacado / link | `#2563EB` | `#5B9BFA` | 5,2 / 5,4 |
| Botão primário (fundo / texto) | `#2563EB` / `#FFFFFF` | `#5B9BFA` / `#0B1F3B` | 5,2 / 5,9 |
| Botão tonal (fundo / texto) | `#DBE6FE` / `#0B1F3B` | `#1E3A66` / `#DBE6FE` | 13,2 / 9,1 |
| Botão contornado (borda / texto) | `#7A879C` / texto destacado | `#6F84A3` / texto destacado | borda 3,6 / 3,9 · texto 5,2 / 5,4 |
| Botão desabilitado | texto principal a 38% sobre texto principal a 12% | idem | não exigido (WCAG isenta desabilitados) |
| Switch ligado / desligado | azul `#2563EB` / cinza `#7A879C` | azul `#5B9BFA` / cinza `#6F84A3` | 3,6 / 3,9 (desligado) |
| Permitido (sucesso) | `#15803D` | `#4ADE80` | 5,0 / 8,6 |
| Bloqueado (erro) | `#DC2626` | `#F87171` | 4,8 / 5,4 |
| Aviso | `#B45309` | `#FBBF24` | 5,0 / 9,0 |

Texto sobre fundos tingidos (selos de bloqueado, permitido e aviso) usa uma versão mais escura da
mesma cor no tema claro (`#991B1B`, `#14532D`, `#78350F`) e mais clara no escuro.

No tema escuro, o card fica pouco diferente do fundo (1,2:1); por isso o card **sempre tem borda**,
como já é o padrão do app.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reconhecer o CallGuard pelo ícone e pela abertura (Priority: P1)

A pessoa instala o app e vê o símbolo do CallGuard na tela inicial do celular e na lista de apps. Ao
abrir pela primeira vez, a apresentação mostra a logo completa e a tagline.

**Why this priority**: O ícone é o primeiro contato com o app (na loja e no celular). O ícone padrão
passa a impressão de app inacabado e é exigido um ícone próprio para publicar.

**Independent Test**: Instalar o app em aparelhos com formatos de ícone diferentes (redondo,
quadrado arredondado, gota) e conferir o símbolo inteiro, centralizado e sem cortes; abrir pela
primeira vez e conferir a logo na apresentação.

**Acceptance Scenarios**:

1. **Given** o app instalado, **When** a pessoa olha a tela inicial do celular, **Then** vê o símbolo
   do CallGuard sobre fundo navy, sem texto, inteiro em qualquer formato de ícone do aparelho.
2. **Given** o aparelho usa ícones temáticos (monocromáticos), **When** a pessoa ativa essa opção,
   **Then** o CallGuard aparece com o contorno do símbolo na cor do tema do sistema.
3. **Given** o primeiro uso, **When** a apresentação abre, **Then** mostra o símbolo, o nome
   "CallGuard" ("Guard" em azul) e a tagline no idioma escolhido.
4. **Given** o app sendo aberto, **When** a tela de carregamento do sistema aparece, **Then** mostra o
   símbolo sobre o fundo do tema em uso, sem flash branco no tema escuro.

---

### User Story 2 - Usar o app com cores da marca e legíveis (Priority: P1)

Todas as telas (apresentação, oferta, Home, whitelist, histórico, Configurações e diálogos) usam o
mesmo esquema de cores: fundo, cards com borda, botões primários, tonais, contornados e
desabilitados, textos e cores de estado.

**Why this priority**: Dá identidade e consistência ao app. Sem ele, o tema escuro (US3) não tem
como existir.

**Independent Test**: Percorrer todas as telas no tema claro e conferir cada elemento contra a tabela
de papéis de cor; medir o contraste dos textos.

**Acceptance Scenarios**:

1. **Given** qualquer tela, **When** a pessoa a vê, **Then** fundo, cards, bordas e textos seguem os
   papéis da tabela.
2. **Given** uma ação principal (ex.: "Ver planos", "Ativar proteção"), **When** exibida, **Then** é
   um botão primário azul (texto branco no tema claro, navy no escuro).
3. **Given** um botão indisponível (ex.: "Salvar" com campo vazio), **When** exibido, **Then** fica
   visivelmente apagado e é claramente diferente do botão ativo.
4. **Given** o histórico de chamadas, **When** a pessoa vê uma chamada bloqueada e uma permitida,
   **Then** cada uma usa a cor de estado correspondente e também um ícone ou texto (a cor nunca é o
   único sinal).
5. **Given** um aviso (ex.: limite do plano, pagamento pendente), **When** exibido, **Then** usa a cor
   de aviso.

---

### User Story 3 - Escolher tema claro, escuro ou do sistema (Priority: P2)

Em Configurações, a pessoa escolhe **Claro**, **Escuro** ou **Seguir o sistema** (padrão). A escolha
vale na hora e continua valendo nas próximas aberturas.

**Why this priority**: Conforto à noite e preferência pessoal. Depende do esquema de cores (US2).

**Independent Test**: Alternar as três opções, conferir a mudança imediata em todas as telas,
fechar e reabrir o app e conferir que a escolha foi mantida; com "Seguir o sistema", trocar o tema do
celular e conferir que o app acompanha.

**Acceptance Scenarios**:

1. **Given** a primeira instalação, **When** a pessoa abre o app, **Then** o tema segue o do sistema.
2. **Given** a pessoa escolhe "Escuro", **When** confirma, **Then** o app muda na hora, sem
   reiniciar e sem perder o que estava na tela.
3. **Given** a escolha "Escuro", **When** a pessoa fecha e reabre o app, **Then** continua escuro,
   mesmo que o celular esteja no tema claro.
4. **Given** "Seguir o sistema", **When** o celular troca de tema (ex.: modo noturno agendado),
   **Then** o app acompanha.
5. **Given** o tema escuro, **When** a pessoa percorre todas as telas e diálogos, **Then** nenhum
   texto fica ilegível e nenhum elemento fica com cor do tema claro.

### Edge Cases

- **Cores dinâmicas do sistema (Material You)**: o app **não** usa as cores do papel de parede; mantém
  sempre as cores da marca, para ser reconhecível.
- **Tamanho de fonte grande do sistema**: o esquema de cores e os cards continuam legíveis com a
  fonte do sistema em 200%.
- **Barra de status e de navegação**: acompanham o tema (ícones escuros no claro, claros no escuro).
- **Telas da loja (compra) e permissões do sistema**: seguem o tema do sistema, fora do controle do
  app; aceitável.
- **Backup e restauração (005)**: a escolha de tema faz parte das configurações e entra no backup.
- **Abertura no Android 8 a 11**: o sistema desenha a tela de abertura antes de o app ler a
  preferência, então ela segue o tema do celular. Se a pessoa escolheu "Escuro" e o celular está
  claro, a abertura aparece clara e o app abre escuro. No Android 12 ou superior, a abertura segue a
  escolha da pessoa.
- **Cor como único sinal**: estados (bloqueado/permitido, ligado/desligado) sempre têm também texto
  ou ícone, para quem não distingue cores.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O ícone do app MUST ser o símbolo da logo escolhida, sem texto, sobre fundo navy, e
  MUST aparecer inteiro em qualquer formato de ícone do aparelho.
- **FR-002**: O app MUST oferecer uma versão monocromática do ícone para os ícones temáticos do
  sistema.
- **FR-003**: A tela de carregamento na abertura MUST mostrar o símbolo sobre o fundo do tema em uso.
- **FR-004**: A apresentação do primeiro uso MUST exibir a logo completa (símbolo, "Call" + "Guard"
  em azul) e a tagline traduzida.
- **FR-005**: Todas as telas e diálogos MUST usar exclusivamente os papéis de cor definidos neste
  esquema; nenhuma tela pode ter cores fixas fora dele.
- **FR-006**: Todo texto MUST ter contraste de pelo menos 4,5:1 com o fundo onde aparece, em ambos os
  temas; ícones e bordas de componentes interativos, pelo menos 3:1.
- **FR-007**: Botões MUST seguir quatro níveis visuais: primário (ação principal), tonal (ação
  secundária de destaque), contornado (ação alternativa) e desabilitado (claramente apagado).
- **FR-008**: Estados de chamada e avisos MUST usar as cores de sucesso, erro e aviso, sempre
  acompanhadas de texto ou ícone.
- **FR-009**: O app MUST ter tema claro e tema escuro completos.
- **FR-010**: Em Configurações, a pessoa MUST poder escolher Claro, Escuro ou Seguir o sistema; o
  padrão é Seguir o sistema.
- **FR-011**: A troca de tema MUST valer na hora, sem reiniciar o app, e MUST persistir entre
  aberturas.
- **FR-012**: O app MUST NOT usar as cores dinâmicas do papel de parede.
- **FR-013**: Barras de status e navegação MUST acompanhar o tema em uso.
- **FR-014**: A escolha de tema MUST entrar no backup das configurações (feature 005).
- **FR-015**: Textos novos (opções de tema e tagline) MUST existir em inglês, português do Brasil e
  espanhol.

### Key Entities

- **Esquema de cores**: conjunto de papéis (fundo, superfície, borda, textos, botões, estados) com um
  valor para o tema claro e outro para o escuro.
- **Preferência de tema**: Claro, Escuro ou Seguir o sistema; guardada no aparelho e incluída no
  backup.
- **Recursos da marca**: símbolo (colorido e monocromático), logo completa e tagline.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos pares texto/fundo do app atingem contraste 4,5:1 nos dois temas.
- **SC-002**: O ícone aparece inteiro, sem cortes, nos formatos redondo, quadrado arredondado e gota.
- **SC-003**: A troca de tema é visível em menos de 1 segundo, sem reiniciar o app.
- **SC-004**: Em 100% das telas e diálogos, nenhum elemento do tema escuro mantém cor do tema claro
  (verificado por captura de tela de cada uma).
- **SC-005**: A pessoa troca o tema em até 3 toques a partir das Configurações.
- **SC-006**: Nenhuma tela usa cor fora do esquema definido (verificável por revisão).

## Assumptions

- A logo e o símbolo serão redesenhados como arte vetorial a partir da prancha escolhida; a prancha é
  referência visual, não o arquivo final. O ícone de 512 px para a ficha da Google Play é gerado do
  mesmo vetor (a publicação na loja fica para a etapa da Play Console).
- O gradiente azul→ciano aparece só no símbolo; botões e textos usam cores sólidas, por legibilidade.
- A tipografia continua a do sistema; esta feature trata de cores e marca, não de fonte própria.
- Formas (cards 22dp, botões 14dp) e layout definidos no alinhamento recente de Home e Configurações
  continuam valendo.
- As variações 2–4 da prancha (verde, roxo, cinza) não entram no app; não haverá seletor de cor de
  destaque.
