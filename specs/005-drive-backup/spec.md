# Feature Specification: Backup no Google Drive

**Feature Branch**: `005-drive-backup`

**Created**: 2026-10-14

**Status**: Partially implemented — exportação/importação por arquivo (US4) concluída; Google Drive (US1–US3, US5) bloqueado até o projeto Google Cloud

**Input**: User description: "Numa assinatura premium, armazenar os dados do banco local em um banco
remoto que o usuário possa recuperar depois. Sem versão iOS: armazenar apenas no Google Drive."

## Contexto

Ao trocar de celular ou reinstalar o app, a pessoa perde a whitelist e as configurações. Esta
feature cria a cópia de segurança decidida em 2026-10-14 e permitida pela constituição v2.0.0
(Princípio II): **somente na pasta privada do app no Google Drive da própria pessoa**, sem servidor
do CallGuard, e um arquivo de exportação criptografado. O backup automático e a sincronização
entre aparelhos são exclusivos do plano **Pro** (feature 003).

## O que vai no backup

| Dado | Incluído | Observação |
|---|---|---|
| Whitelist (nomes e números) | ✅ sempre | número no formato identificado da feature 001 |
| Configurações (bloqueios, idioma, consentimentos) | ✅ sempre | consentimento de contatos é refeito no novo aparelho se a permissão não existir |
| Histórico de chamadas | só se a pessoa escolher | dentro da retenção do plano |
| Contatos importados da agenda | ❌ nunca | reconstruídos a partir da agenda do novo aparelho |
| Plano / assinatura | ❌ | a loja do Google restaura sozinha |

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ativar o backup no Drive (Priority: P1)

Uma pessoa no Pro abre Configurações → Backup, entende o que será guardado e onde, escolhe se inclui
o histórico, entra com a conta Google e o primeiro backup é feito.

**Why this priority**: Sem ativação não existe backup; é a base de todo o resto.

**Independent Test**: Com Pro de teste, ativar o backup e verificar no app a data do último backup
e, na conta Google, que os dados do app existem na área privada do app no Drive.

**Acceptance Scenarios**:

1. **Given** plano Pro sem backup, **When** a pessoa ativa o backup e autoriza a conta Google,
   **Then** o primeiro backup termina e a tela mostra "Último backup: agora" e a conta usada.
2. **Given** a tela de ativação, **When** a pessoa a lê, **Then** vê o que entra, o que não entra,
   que os dados ficam no Drive dela e que o CallGuard não tem acesso a eles.
3. **Given** a pessoa não marcou o histórico, **When** o backup é feito, **Then** ele contém
   whitelist e configurações, mas nenhum registro de chamada.
4. **Given** plano Plus ou Grátis, **When** a pessoa abre Backup, **Then** vê o que o Pro oferece
   e pode usar a exportação manual (US4).

---

### User Story 2 - Restaurar no aparelho novo (Priority: P1)

Ao instalar o CallGuard num celular novo, a pessoa escolhe "Restaurar backup" logo na
apresentação (ou depois, em Configurações), entra com a mesma conta Google e recupera whitelist e
configurações.

**Why this priority**: É o motivo de existir do backup.

**Independent Test**: Fazer backup no aparelho A, instalar no aparelho B, restaurar e conferir
whitelist, configurações e (se incluído) histórico.

**Acceptance Scenarios**:

1. **Given** existe backup na conta, **When** a pessoa restaura em um aparelho sem dados, **Then**
   a whitelist e as configurações ficam iguais às do backup.
2. **Given** o aparelho novo já tem números na whitelist, **When** a pessoa restaura, **Then** os
   dados são combinados sem duplicar números.
3. **Given** a conta não tem backup, **When** a pessoa tenta restaurar, **Then** o app informa e
   segue o primeiro uso normalmente.
4. **Given** a restauração termina, **When** a pessoa tinha contatos liberados, **Then** o app
   pede de novo o consentimento/permissão de contatos e reimporta a agenda deste aparelho.

---

### User Story 3 - Backup automático e vários aparelhos (Priority: P2)

Com o backup ativo, toda alteração relevante é guardada automaticamente; dois aparelhos com a mesma
conta e o Pro ficam com a mesma whitelist.

**Why this priority**: Remove a necessidade de lembrar de fazer backup; multi-aparelho é diferencial
do Pro.

**Independent Test**: Com dois aparelhos ativos, adicionar um número no A e verificar que aparece
no B; remover no B e verificar que some no A.

**Acceptance Scenarios**:

1. **Given** backup ativo, **When** a pessoa adiciona um número, **Then** o backup é atualizado em
   até 15 minutos com internet disponível.
2. **Given** dois aparelhos ativos, **When** um número é removido no aparelho B, **Then** ele não
   reaparece no aparelho A depois da sincronização.
3. **Given** o mesmo número foi renomeado nos dois aparelhos, **When** sincronizam, **Then** vale a
   alteração mais recente.
4. **Given** sem internet, **When** a pessoa faz alterações, **Then** elas são enviadas quando a
   conexão voltar, sem ação da pessoa.

---

### User Story 4 - Exportar e importar um arquivo protegido (Priority: P2)

Qualquer pessoa, em qualquer plano, pode exportar seus dados para um arquivo protegido por senha e
guardá-lo onde quiser, e importar esse arquivo depois.

**Why this priority**: Garante a portabilidade dos próprios dados (LGPD) e um caminho de backup
sem conta Google.

**Independent Test**: Exportar com senha, apagar os dados do app, importar o arquivo com a senha e
conferir a whitelist; tentar importar com senha errada e ver a recusa.

**Acceptance Scenarios**:

1. **Given** a pessoa escolhe exportar, **When** define uma senha e um local, **Then** um arquivo é
   salvo e só pode ser lido com essa senha.
2. **Given** um arquivo exportado, **When** a pessoa importa com a senha correta, **Then** os dados
   são combinados como na restauração (US2).
3. **Given** senha errada ou arquivo alterado, **When** a pessoa importa, **Then** nada é alterado
   e o app explica o motivo.

---

### User Story 5 - Desativar e apagar o backup (Priority: P3)

A pessoa pode desativar o backup e apagar a cópia guardada no Drive a qualquer momento.

**Why this priority**: Controle total dos próprios dados (constituição, Princípio II).

**Independent Test**: Desativar e apagar; verificar que nada mais é enviado e que a restauração
não encontra backup.

**Acceptance Scenarios**:

1. **Given** backup ativo, **When** a pessoa desativa, **Then** nada mais é enviado e o app
   pergunta se deve apagar a cópia existente.
2. **Given** a pessoa confirma apagar, **When** a operação termina, **Then** a cópia no Drive não
   existe mais e a conta Google é desconectada do app.

### Edge Cases

- **Plano Pro termina**: o backup automático para; a cópia no Drive é mantida e ainda pode ser
  restaurada ou apagada; exportação manual continua disponível.
- **Pessoa revoga o acesso do app pela conta Google**: o app detecta, desativa o backup e avisa.
- **Drive sem espaço**: o app avisa e tenta de novo depois; os dados locais não são afetados.
- **Restauração com mais números que o limite do plano atual**: todos são restaurados e
  permanecem permitidos (mesma regra de rebaixamento da feature 003); novas inclusões seguem o
  limite.
- **Backup feito por versão mais nova do app**: o app informa que precisa ser atualizado antes de
  restaurar, sem alterar nada.
- **Falha no meio da restauração**: nenhum dado local é perdido; a restauração é refeita do zero.
- **Cópia corrompida**: detectada e recusada; o último backup bom não é sobrescrito por uma cópia
  inválida.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Os dados remotos MUST ser gravados exclusivamente na área privada do app no Google
  Drive da própria pessoa; nenhum dado vai para servidores do CallGuard.
- **FR-002**: O backup MUST incluir whitelist e configurações; o histórico só com escolha explícita;
  MUST NOT incluir contatos importados da agenda nem dados de assinatura.
- **FR-003**: A ativação MUST explicar o que é guardado, onde, e que pode ser apagado, antes de
  pedir acesso à conta Google; o acesso pedido MUST se limitar à área privada do app.
- **FR-004**: Backup automático e sincronização entre aparelhos MUST estar disponíveis apenas no
  plano Pro.
- **FR-005**: Restaurar a partir do Drive e importar arquivo MUST estar disponíveis em qualquer
  plano; restaurar MUST ser oferecido na apresentação do primeiro uso.
- **FR-006**: Restauração e importação MUST combinar dados pelo número identificado (feature 001),
  sem duplicar, aplicando a alteração mais recente e respeitando remoções.
- **FR-007**: Com o backup ativo, alterações MUST chegar ao Drive em até 15 minutos quando houver
  internet, sem ação da pessoa.
- **FR-008**: A exportação MUST gerar arquivo criptografado com senha definida pela pessoa;
  importação com senha errada ou arquivo alterado MUST ser recusada sem alterar dados.
- **FR-009**: A pessoa MUST poder desativar o backup e apagar a cópia remota pelo app.
- **FR-010**: Falhas de rede, de espaço ou de acesso MUST NOT alterar ou perder dados locais e
  MUST ser comunicadas de forma compreensível.
- **FR-011**: O app MUST mostrar a data do último backup bem-sucedido e a conta usada.
- **FR-012**: Textos novos, inclusive a explicação de privacidade, MUST existir em inglês,
  português do Brasil e espanhol; a política de privacidade MUST ser atualizada nos três idiomas.

### Key Entities

- **Cópia de backup**: whitelist, configurações e (opcional) histórico, com versão do formato e
  data; uma por conta Google.
- **Registro de alteração**: para cada número, data da última alteração e marca de remoção, usado
  para combinar dados entre aparelhos.
- **Arquivo de exportação**: mesma estrutura da cópia, protegida por senha.
- **Estado do backup**: ativo/inativo, conta, último sucesso, último erro, inclui histórico.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Restaurar em um aparelho novo devolve 100% da whitelist e das configurações em menos
  de 1 minuto.
- **SC-002**: Uma alteração feita num aparelho aparece no outro em até 15 minutos em 95% dos casos
  com internet.
- **SC-003**: Nenhuma cópia remota contém contatos importados da agenda (verificável inspecionando
  o backup de teste).
- **SC-004**: Arquivos exportados não são legíveis sem a senha.
- **SC-005**: A pessoa ativa o backup em até 4 toques a partir das Configurações.

## Assumptions

- A cópia no Drive fica na área privada do app, invisível na interface normal do Drive; a
  segurança vem da conta Google da pessoa. Criptografia própria é exigida para o arquivo exportado
  (constituição v2.0.0); para a cópia no Drive fica como melhoria futura.
- Uma cópia por conta Google; não há histórico de versões de backup nesta feature.
- Depende da feature 003 (níveis) para a regra "automático só no Pro".
- Sem versão iOS: não há necessidade de formato ou armazenamento multiplataforma.
