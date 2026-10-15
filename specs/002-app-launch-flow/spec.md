# Feature Specification: Fluxo de Abertura e Idioma

**Feature Branch**: `002-app-launch-flow`

**Created**: 2026-10-14

**Status**: Implemented

**Input**: User description: "002, fluxo de abertura e idioma. O app sempre abre na tela de seleção
de idioma; o primeiro uso pergunta o idioma duas vezes; a tela de idioma aberta pelas
Configurações leva ao onboarding em vez de voltar. Decisão: o onboarding vira a primeira tela do
primeiro uso; a tela de seleção isolada fica só para Configurações → Idioma, aplica na hora e
volta; após concluir o primeiro uso (onboarding + escolha no paywall), as próximas aberturas vão
direto para a Home."

## Contexto

Hoje, **toda** abertura do CallGuard repete o mesmo caminho: seleção de idioma → apresentação
(onboarding) → oferta do Premium (paywall) → tela principal (Home). Isso acontece porque o app
não guarda que o primeiro uso já foi concluído. Além disso:

1. No primeiro uso o idioma é perguntado **duas vezes**: numa tela só com a lista de idiomas
   (que aplica a escolha apenas ao tocar em "Confirmar") e, logo em seguida, na apresentação,
   que tem uma seleção melhor (a tela muda de idioma no momento do toque).
2. Quem abre **Configurações → Idioma** e confirma é levado para a apresentação do app, em vez de
   voltar às Configurações.
3. A oferta do Premium aberta a partir da Home ("Ver Premium"), ao escolher "Continuar grátis",
   empilha uma nova Home em vez de voltar.

O problema de o idioma escolhido não ser aplicado já foi corrigido; esta feature trata do fluxo.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Abrir o app vai direto para a Home depois do primeiro uso (Priority: P1)

Uma pessoa que já passou pela apresentação e escolheu entre assinar ou continuar grátis abre o
CallGuard e cai direto na tela principal, sem rever idioma, apresentação ou oferta.

**Why this priority**: É o incômodo diário: cada abertura obriga a atravessar três telas para
chegar à única que importa. Corrigir isso sozinho já muda a percepção do app.

**Independent Test**: Concluir o primeiro uso, fechar o app completamente e reabrir: a Home
aparece imediatamente. Repetir após reiniciar o aparelho.

**Acceptance Scenarios**:

1. **Given** o primeiro uso foi concluído (escolha feita na oferta do Premium), **When** a
   pessoa abre o app, **Then** a Home é a primeira tela exibida.
2. **Given** o primeiro uso foi concluído, **When** a pessoa reinicia o aparelho e abre o app,
   **Then** a Home é a primeira tela exibida.
3. **Given** a pessoa está na Home aberta diretamente, **When** toca em "voltar" do sistema,
   **Then** o app é fechado (não aparecem as telas do primeiro uso).
4. **Given** o primeiro uso foi concluído no idioma espanhol, **When** o app é reaberto,
   **Then** a Home aparece em espanhol.

---

### User Story 2 - Primeiro uso pergunta o idioma uma única vez (Priority: P1)

Na primeira abertura, a pessoa vê direto a apresentação do CallGuard, que já mostra o idioma do
aparelho selecionado (quando suportado) e permite trocá-lo com um toque, mudando a tela na hora.
Não existe mais a tela anterior só com a lista de idiomas.

**Why this priority**: É a primeira impressão do app; perguntar a mesma coisa duas vezes passa
sensação de descuido. Tem o mesmo peso da US1 e as duas juntas formam o fluxo de abertura.

**Independent Test**: Instalar o app do zero e abri-lo: a primeira tela é a apresentação, com um
idioma já marcado; tocar em outro idioma traduz a tela na hora; seguir até a Home sem nova
pergunta sobre idioma.

**Acceptance Scenarios**:

1. **Given** o app acabou de ser instalado e o aparelho está em português do Brasil, **When** a
   pessoa abre o app, **Then** a primeira tela é a apresentação, em português, com "Português"
   marcado.
2. **Given** o aparelho está num idioma não suportado (ex.: francês), **When** a pessoa abre o
   app pela primeira vez, **Then** a apresentação aparece em inglês com "English" marcado.
3. **Given** a apresentação está aberta, **When** a pessoa toca em "Español", **Then** a tela
   passa a espanhol imediatamente e a escolha fica salva.
4. **Given** a pessoa está na apresentação, **When** toca em "Continuar", **Then** vê a oferta do
   Premium, sem nenhuma pergunta adicional sobre idioma.
5. **Given** a pessoa fechou o app na oferta do Premium sem escolher, **When** reabre o app,
   **Then** volta para a oferta do Premium (a apresentação não se repete).
6. **Given** a pessoa chegou à oferta do Premium vindo da apresentação **na mesma sessão**,
   **When** toca em "voltar" do sistema, **Then** retorna à apresentação. (Se o app foi reaberto
   direto na oferta — cenário 5 — a apresentação não é reexibida e "voltar" fecha o app.)

---

### User Story 3 - Trocar o idioma pelas Configurações e voltar (Priority: P2)

Depois do primeiro uso, a pessoa abre Configurações → Idioma, vê o idioma atual marcado, toca em
outro e a tela muda na hora. Ao sair, volta para as Configurações, já no novo idioma.

**Why this priority**: Funcionalidade usada raramente, mas hoje leva a um lugar errado (a
apresentação), o que desorienta.

**Independent Test**: Na Home, abrir Configurações → Idioma, trocar para inglês, sair da tela:
deve-se estar nas Configurações em inglês; reabrir o app: Home em inglês.

**Acceptance Scenarios**:

1. **Given** o app está em português, **When** a pessoa abre Configurações → Idioma, **Then**
   "Português" aparece marcado.
2. **Given** a tela de idioma está aberta, **When** a pessoa toca em "English", **Then** a tela
   passa a inglês imediatamente e a escolha fica salva, sem precisar confirmar.
3. **Given** a pessoa trocou o idioma, **When** sai da tela (botão de voltar da tela ou do
   sistema), **Then** volta às Configurações, já no novo idioma.
4. **Given** a pessoa trocou o idioma pelas Configurações, **When** reabre o app, **Then** a Home
   aparece no novo idioma.

---

### User Story 4 - Ver a oferta do Premium a partir do app e voltar (Priority: P3)

Depois do primeiro uso, a pessoa abre a oferta do Premium por qualquer atalho (Home, Whitelist,
Configurações, limite atingido). Ao escolher "continuar grátis" ou "voltar", retorna para a tela
de onde saiu.

**Why this priority**: Hoje "continuar grátis" nesse contexto empilha uma nova Home; o efeito é
discreto (voltar precisa ser tocado mais vezes), por isso P3.

**Independent Test**: Abrir a oferta pela Whitelist, tocar "continuar grátis": deve-se voltar à
Whitelist; tocar "voltar" do sistema na Home fecha o app de primeira.

**Acceptance Scenarios**:

1. **Given** a pessoa abriu a oferta pela Whitelist, **When** toca em "continuar grátis",
   **Then** volta à Whitelist.
2. **Given** a pessoa abriu a oferta pela Home, **When** assina com sucesso, **Then** volta à
   Home com o plano Premium ativo.
3. **Given** a pessoa voltou da oferta para a Home, **When** toca em "voltar" do sistema,
   **Then** o app é fechado (não há Home duplicada).

### Edge Cases

- **Atualização de quem já usa o app**: pessoas que já usavam a versão anterior (já escolheram
  um idioma, têm números na whitelist ou têm licença) são consideradas com o primeiro uso
  concluído e vão direto para a Home na primeira abertura após a atualização.
- **Variante regional do idioma do aparelho** (ex.: português de Portugal, espanhol do México):
  o idioma marcado na apresentação MUST corresponder ao idioma em que a tela está de fato
  escrita.
- **Idioma trocado fora do app** (Android 13+: Configurações do sistema → Idioma do app): o app
  respeita essa escolha; a tela de idioma do app mostra o idioma vigente marcado.
- **Rotação de tela ou troca de idioma durante o primeiro uso**: a pessoa permanece na mesma
  tela em que estava.
- **Dados do app apagados pelo sistema/usuário**: o app volta a se comportar como recém-instalado
  (primeiro uso).
- **Assinatura que falha ou é cancelada na oferta durante o primeiro uso**: a pessoa continua na
  oferta e pode escolher "continuar grátis"; o primeiro uso só conclui com uma escolha efetiva.
- **Falha ao ler o estado salvo**: o app trata como primeiro uso já concluído se houver
  qualquer dado de uso (whitelist, licença ou idioma) e, caso contrário, mostra a apresentação —
  nunca deixa a pessoa sem conseguir abrir o app.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST registrar de forma persistente que o primeiro uso foi concluído no
  momento em que a pessoa faz uma escolha na oferta do Premium (assinar com sucesso ou continuar
  grátis).
- **FR-002**: Com o primeiro uso concluído, toda abertura do app MUST exibir a Home como primeira
  tela, sem exibir apresentação, seleção de idioma ou oferta do Premium.
- **FR-003**: Sem o primeiro uso concluído, a primeira tela MUST ser a apresentação; se a
  apresentação já foi concluída (a pessoa tocou em "Continuar"), a primeira tela MUST ser a
  oferta do Premium.
- **FR-004**: A tela isolada de seleção de idioma MUST NOT fazer parte do primeiro uso.
- **FR-005**: Na primeira exibição da apresentação, o idioma marcado MUST ser o idioma do
  aparelho quando suportado (português do Brasil, inglês ou espanhol) e inglês caso contrário;
  a tela MUST ser exibida nesse idioma.
- **FR-006**: Tocar num idioma, tanto na apresentação quanto na tela de idioma das
  Configurações, MUST aplicar e salvar o idioma imediatamente, sem botão de confirmação.
- **FR-007**: A tela de idioma aberta pelas Configurações MUST exibir o idioma vigente marcado e,
  ao sair, MUST retornar às Configurações.
- **FR-008**: A oferta do Premium aberta depois do primeiro uso MUST retornar à tela de origem
  quando a pessoa escolher "continuar grátis", voltar ou concluir a assinatura.
- **FR-009**: Na Home exibida como primeira tela, o "voltar" do sistema MUST fechar o app; não
  pode haver telas do primeiro uso nem Homes duplicadas na pilha de navegação.
- **FR-010**: Ao atualizar a partir de uma versão anterior, o sistema MUST considerar o primeiro
  uso concluído se houver idioma salvo, número na whitelist ou licença ativa.
- **FR-011**: Mudanças de configuração (rotação, troca de idioma) MUST preservar a tela atual.
- **FR-012**: Todos os textos novos ou alterados MUST existir em inglês, português do Brasil e
  espanhol.

### Key Entities

- **Progresso do primeiro uso**: indica em que ponto a pessoa está do primeiro uso —
  não iniciado, apresentação concluída, ou primeiro uso concluído. Persistido no aparelho,
  apagado junto com os dados do app.
- **Idioma do app**: idioma escolhido pela pessoa (ou o do aparelho por padrão), já persistido
  pela correção anterior; continua sendo a única fonte do idioma exibido.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Depois do primeiro uso, 100% das aberturas do app mostram a Home como primeira
  tela (verificado em 10 aberturas seguidas, incluindo após reiniciar o aparelho).
- **SC-002**: Uma pessoa recém-instalando o app chega à Home em no máximo 3 toques (ex.:
  "Continuar" na apresentação e "Continuar grátis" na oferta), contra 5 hoje.
- **SC-003**: No primeiro uso, o idioma é perguntado em exatamente uma tela.
- **SC-004**: Trocar o idioma pelas Configurações leva no máximo 2 toques (escolher o idioma e
  voltar) e termina nas Configurações em 100% dos casos.
- **SC-005**: Nenhum usuário da versão anterior volta a ver a apresentação após atualizar.

## Assumptions

- O primeiro uso conclui com a escolha na oferta do Premium (decisão do usuário), não apenas com
  a apresentação.
- A oferta do Premium continua acessível a qualquer momento pelos atalhos existentes.
- A tela de idioma das Configurações mantém a aparência atual (lista com bandeiras); só muda o
  comportamento (aplica ao tocar, sem "Confirmar", e volta). O botão "Confirmar" pode ser
  removido ou substituído por "voltar" — decisão de design no plano.
- O progresso do primeiro uso segue a mesma política de backup das demais preferências; a
  revisão de backup pertence à feature de privacidade (004).
- Fora do escopo: conteúdo/textos da apresentação e da oferta, fluxo de configuração da proteção
  de chamadas e testes em Android ≤ 12 (podem ser feitos se houver emulador disponível).
