# Feature Specification: Normalização de Números de Telefone

**Feature Branch**: `001-phone-normalization`

**Created**: 2026-10-13

**Status**: Implemented — pendente validação manual em aparelho real com chip brasileiro (quickstart §3)

**Input**: User description: "para 001, normalização de números."

## Contexto

Hoje o CallGuard só reconhece números escritos no formato internacional completo, começando com
`+` e código do país (ex.: `+5511987654321`). Isso gera três falhas:

1. **Chamadas legítimas bloqueadas**: quando a operadora entrega a chamada em formato nacional
   (ex.: `11987654321` ou `011987654321`), o número é considerado "inválido" e **bloqueado
   sempre**, mesmo que esteja na whitelist e mesmo com "bloquear desconhecidos" desligado.
   Isso viola o Princípio I (Fail-Open) da constituição.
2. **Contatos ignorados**: contatos salvos na agenda sem `+55` (a maioria no Brasil) são
   descartados silenciosamente na importação e nunca são liberados.
3. **Cadastro manual difícil**: o usuário é obrigado a digitar o número no formato E.164 para
   adicioná-lo à whitelist.

Esta feature estabelece uma identidade única e confiável para cada número (Princípio III), não
importa como ele foi digitado, salvo na agenda ou entregue pela operadora.

## Clarifications

### Session 2026-10-13

- Q: Um celular brasileiro antigo de 8 dígitos (contato ou whitelist) deve ser reconhecido quando
  a chamada chega com o nono dígito? → A: Sim (opção A, recomendada; aplicada por padrão ao
  iniciar o plano sem resposta explícita — revisável). Ao interpretar um celular brasileiro de 8
  dígitos, o sistema acrescenta o 9 automaticamente. Ver FR-014.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Chamada de número conhecido em formato nacional é reconhecida (Priority: P1)

Um familiar está na whitelist do usuário. A operadora entrega a chamada desse familiar sem o
código do país (ex.: `11 98765-4321` ou `011 98765-4321`). O CallGuard reconhece que é o mesmo
número da whitelist e deixa a chamada tocar.

**Why this priority**: É o defeito mais grave do app hoje: pessoas importantes estão sendo
bloqueadas. Corrigir isso sozinho já entrega um app confiável.

**Independent Test**: Cadastrar `+5511987654321` na whitelist, simular chamadas recebidas com
`11987654321`, `011987654321`, `+55 11 98765-4321` e `5511987654321` e verificar que todas são
permitidas com o motivo "permitido pela whitelist".

**Acceptance Scenarios**:

1. **Given** `+5511987654321` está na whitelist e o aparelho está no Brasil, **When** chega uma
   chamada de `11987654321`, **Then** a chamada é permitida e o histórico registra "permitido
   pela whitelist".
2. **Given** o mesmo cenário, **When** chega uma chamada de `01511987654321` (com código de
   operadora de longa distância), **Then** a chamada é permitida.
3. **Given** "bloquear desconhecidos" está **desligado**, **When** chega uma chamada de um
   número que não pode ser interpretado (ex.: `12345`), **Then** a chamada é permitida.
4. **Given** "bloquear desconhecidos" está **ligado**, **When** chega uma chamada de um número
   que não pode ser interpretado e não consta em nenhuma lista, **Then** a chamada é bloqueada
   como desconhecida (e não por "número inválido").
5. **Given** um número estrangeiro `+351912345678` está na whitelist, **When** ele liga com o
   `+` e o código do país, **Then** a chamada é permitida.

---

### User Story 2 - Contatos salvos em qualquer formato são aproveitados (Priority: P2)

Um usuário Premium com "liberar contatos automaticamente" ativo tem na agenda contatos salvos
como `(11) 98765-4321`, `11987654321`, `0xx11 98765-4321` ou `+55 11 98765-4321`. Todos são
importados e reconhecidos quando ligam.

**Why this priority**: Sem isso, o principal benefício do Premium praticamente não funciona para
usuários brasileiros. Depende da mesma regra de identidade da US1.

**Independent Test**: Criar contatos de teste em formatos variados, executar a importação e
verificar que todos aparecem no cache e são permitidos quando ligam.

**Acceptance Scenarios**:

1. **Given** a agenda tem contatos em formato nacional sem `+55`, **When** o usuário importa os
   contatos, **Then** todos os números válidos são importados.
2. **Given** dois contatos com o mesmo número escrito de formas diferentes, **When** a
   importação termina, **Then** o número aparece uma única vez.
3. **Given** um contato com um número que não pode ser interpretado (ex.: `*144`), **When** a
   importação termina, **Then** esse número é ignorado e os demais são importados normalmente.
4. **Given** contatos já importados antes desta atualização, **When** o app é atualizado e o
   usuário abre o app pela primeira vez, **Then** o cache de contatos é reprocessado
   automaticamente com a nova regra (sem ação do usuário), desde que o consentimento e a
   permissão de contatos continuem concedidos.
5. **Given** o consentimento ou a permissão de contatos não está mais ativo, **When** o app é
   atualizado e aberto, **Then** o cache de contatos é apagado.
6. **Given** o cache de contatos está sendo reprocessado, **When** um contato liga nesse
   momento, **Then** a chamada é reconhecida normalmente (o cache nunca fica vazio durante a
   troca).

---

### User Story 3 - Cadastro manual aceita o formato que o usuário conhece (Priority: P3)

O usuário quer adicionar o número da mãe à whitelist e digita `11 98765-4321`, do jeito que
está acostumado. O app aceita, mostra como o número será reconhecido e salva.

**Why this priority**: Melhora muito a usabilidade, mas a US1 e a US2 já resolvem os bloqueios
indevidos. O usuário ainda consegue cadastrar com `+55` enquanto isso não existe.

**Independent Test**: Na tela de whitelist, digitar números em formatos variados e verificar que
são aceitos, que a prévia mostra o número reconhecido e que duplicatas são detectadas.

**Acceptance Scenarios**:

1. **Given** o aparelho está no Brasil, **When** o usuário digita `11 98765-4321`, **Then** o
   app aceita o número e mostra uma prévia como `+55 11 98765-4321` antes de salvar.
2. **Given** `+5511987654321` já está na whitelist, **When** o usuário tenta adicionar
   `(11) 98765-4321`, **Then** o app informa que o número já existe.
3. **Given** o usuário digita algo que não é um número de telefone válido (ex.: `123`), **When**
   tenta salvar, **Then** o app explica o problema e não salva.
4. **Given** o usuário quer cadastrar um número de outro país, **When** digita com `+` e código
   do país, **Then** o app aceita.

---

### User Story 4 - Números exibidos em formato legível (Priority: P3)

Na whitelist, no histórico e na lista de contatos importados, os números aparecem formatados de
forma legível para o usuário (ex.: `(11) 98765-4321` para números do próprio país, `+351 912 345
678` para estrangeiros).

**Why this priority**: Melhoria de clareza; não afeta a proteção.

**Independent Test**: Abrir whitelist e histórico com números nacionais e estrangeiros e
verificar a formatação.

**Acceptance Scenarios**:

1. **Given** um número nacional na whitelist, **When** o usuário abre a whitelist, **Then** o
   número aparece no formato nacional legível.
2. **Given** uma chamada de número não interpretável no histórico, **When** o usuário abre o
   histórico, **Then** o número aparece exatamente como chegou.

### Edge Cases

- **Região do aparelho indisponível** (sem chip, tablet, modo avião): usa a região do chip;
  se não houver, a da rede; se não houver, a do idioma/região do sistema; se não houver, a
  última região válida já detectada pelo app. Só sem nenhuma delas (primeiro uso, sem chip e
  sem região no sistema) números sem `+` seguem a regra de desconhecidos.
- **Usuário em roaming**: um número local do país visitado entregue sem código do país é
  interpretado pela região do chip, não pela rede visitada.
- **Celular brasileiro antigo de 8 dígitos** (salvo antes da inclusão do nono dígito, ex.:
  `(11) 8765-4321`): é interpretado como o celular atual de 9 dígitos (`+55 11 98765-4321`)
  e reconhecido quando a chamada chegar com o nono dígito (ver Clarifications).
- **Número oculto/privado**: continua tratado pela regra "bloquear números privados", sem
  mudança.
- **Números de serviço** (`0800`, `0300`, `4004-xxxx`): reconhecidos se forem válidos para a
  região; caso contrário, seguem a regra de desconhecidos.
- **Códigos curtos e caracteres especiais** (`*144`, `#123`, letras): nunca são considerados
  válidos e nunca causam bloqueio por si só.
- **Falha inesperada durante a interpretação**: a chamada é permitida (Princípio I).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST interpretar números em formato internacional (`+55...`, `0055...`)
  e nacional (com ou sem DDD precedido de `0`, com ou sem código de operadora, com espaços,
  hífens, parênteses ou pontos), convertendo-os para uma única forma canônica internacional.
- **FR-002**: Números sem código do país MUST ser interpretados usando a região padrão do
  aparelho, determinada nesta ordem: chip, rede, configuração regional do sistema.
- **FR-003**: A mesma regra de interpretação MUST ser aplicada às chamadas recebidas, à
  importação de contatos, ao cadastro manual na whitelist e a qualquer comparação de números.
- **FR-004**: Um número que não pode ser interpretado MUST NOT ser bloqueado por esse motivo;
  ele MUST ser tratado como "desconhecido", seguindo a configuração "bloquear desconhecidos".
- **FR-005**: O histórico de decisões MUST registrar o número como chegou e, quando possível, a
  forma canônica, além de distinguir "desconhecido" de "não interpretável" para diagnóstico.
- **FR-006**: A importação de contatos MUST aceitar números em formato nacional, MUST ignorar
  apenas os não interpretáveis e MUST eliminar duplicatas pela forma canônica.
- **FR-007**: Após a atualização do app, o cache de contatos existente MUST ser reprocessado
  automaticamente uma única vez com a nova regra, se o consentimento de contatos estiver ativo
  e a permissão de contatos continuar concedida (independentemente da licença, para que o cache
  esteja correto quando o usuário assinar o Premium).
- **FR-008**: O cadastro manual na whitelist MUST aceitar formato nacional e internacional,
  MUST mostrar uma prévia do número reconhecido antes de salvar e MUST detectar duplicatas pela
  forma canônica.
- **FR-009**: O cadastro manual MUST rejeitar entradas não interpretáveis com uma mensagem
  explicativa, nos três idiomas do app.
- **FR-010**: Números MUST ser exibidos em formato legível: formato nacional para números da
  região do aparelho e formato internacional para os demais; números não interpretáveis são
  exibidos como chegaram.
- **FR-011**: Entradas da whitelist já existentes MUST continuar funcionando após a atualização,
  sem ação do usuário.
- **FR-012**: A avaliação de uma chamada recebida MUST continuar sem consultas à agenda do
  aparelho e MUST concluir dentro do tempo que o sistema concede para a triagem.
- **FR-013**: Textos de ajuda e exemplos de número na interface MUST ser atualizados para
  refletir que o formato nacional é aceito, nos três idiomas.
- **FR-014**: Um celular brasileiro no formato antigo de 8 dígitos (sem o nono dígito) MUST ser
  interpretado como o número atual com o nono dígito, em todos os pontos de entrada; entradas
  já salvas na whitelist nesse formato MUST ser convertidas uma única vez após a atualização,
  sem criar duplicatas.

### Key Entities

- **Número canônico**: identidade única de um telefone (formato internacional completo). É a
  chave usada para comparar whitelist, contatos e chamadas recebidas.
- **Número bruto**: o texto exatamente como foi recebido da operadora, digitado ou salvo na
  agenda. É mantido apenas para exibição e diagnóstico.
- **Região padrão**: país usado para interpretar números sem código do país; derivado do
  aparelho.
- **Resultado da interpretação**: válido (com número canônico) ou não interpretável (com o
  motivo), nunca uma decisão de bloqueio por si só.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das chamadas de números da whitelist são permitidas, qualquer que seja o
  formato de entrega pela operadora, em um conjunto de testes com pelo menos 20 formatos
  brasileiros e 5 internacionais.
- **SC-002**: Zero chamadas são bloqueadas por "número inválido" com "bloquear desconhecidos"
  desligado.
- **SC-003**: Pelo menos 99% dos números de celular e fixo válidos de uma agenda brasileira de
  teste (formatos mistos) são importados, contra praticamente 0% dos números sem `+55` hoje.
- **SC-004**: O usuário consegue adicionar um número à whitelist no formato que costuma usar em
  menos de 20 segundos, sem precisar saber o que é o código do país.
- **SC-005**: A decisão sobre uma chamada recebida continua sendo tomada em menos de 1 segundo
  em um aparelho intermediário.
- **SC-006**: Nenhum usuário existente perde entradas da whitelist ou precisa recadastrar
  números após a atualização.

## Assumptions

- O público principal está no Brasil, mas a regra deve funcionar para qualquer país, usando a
  região do aparelho.
- A forma canônica é o formato internacional completo (E.164), o mesmo já usado nas entradas da
  whitelist; por isso as entradas existentes continuam válidas sem migração de conteúdo.
- O app continua 100% offline; a interpretação de números usa apenas dados embarcados no app.
- A região é lida a partir de informações disponíveis sem novas permissões de runtime.
- Formatação legível (US4) segue as convenções de cada país, sem configuração pelo usuário.
- Bloqueio por prefixo/DDD, listas de spam e identificação de chamador estão fora do escopo.
- Logs não passam a conter números de telefone além do que já existe; a remoção dos logs
  existentes é tratada na feature de privacidade (004).
- Apagar o cache de contatos no exato momento em que o usuário revoga o consentimento
  (Princípio II) também fica para a feature 004; nesta feature, o reprocessamento pós-atualização
  já apaga o cache quando não há consentimento ou permissão.
