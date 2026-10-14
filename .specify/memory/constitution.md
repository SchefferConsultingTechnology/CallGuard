<!--
Sync Impact Report (1.0.0)
- Version change: (template) → 1.0.0 (ratificação inicial)
- Principles definidos:
  I. Fail-Open na Chamada (NÃO NEGOCIÁVEL)
  II. Privacidade Local-First
  III. Identidade Canônica de Números
  IV. Motor de Decisão Puro e Testado
  V. Paridade de Localização
  VI. Monetização Honesta
- Added sections: Restrições Técnicas; Fluxo de Desenvolvimento e Quality Gates; Governance
- Removed sections: nenhuma
- Templates: plan/spec/tasks leem a constituição em tempo de execução; nenhuma alteração necessária.
- Violações conhecidas no código atual (débito a quitar via features, não bloqueia ratificação):
  - I/III: CallDecisionEngine bloqueia INVALID_NUMBER independentemente de blockUnknown;
    PhoneNormalizer exige "+" e rejeita números nacionais.
  - II: AppCallScreeningService registra telefone em Log.d/Log.e no release;
    allowBackup=true com regras de backup vazias.
- Deferred TODOs: nenhum
-->

# CallGuard Constitution

## Core Principles

### I. Fail-Open na Chamada (NÃO NEGOCIÁVEL)

O CallGuard protege pessoas; bloquear uma chamada legítima (família, emergência, médico) é o
pior defeito possível, pior do que deixar passar uma chamada indesejada.

- Uma chamada só PODE ser bloqueada por uma decisão positiva de uma regra explícita configurada
  pelo usuário (ex.: `blockUnknown`, `blockPrivateNumbers`).
- Qualquer exceção, timeout, banco indisponível, preferência ilegível ou estado inesperado
  durante a triagem MUST resultar em permitir a chamada.
- Falha de parsing/normalização de um número MUST NOT ser, por si só, motivo de bloqueio:
  o número é tratado como desconhecido e segue a regra `blockUnknown`.
- `onScreenCall` MUST sempre responder (`respondToCall`) para toda chamada recebida, dentro do
  limite de tempo do sistema; I/O pesado (ex.: leitura do ContentProvider de contatos) MUST NOT
  ocorrer no caminho da triagem — apenas consultas indexadas ao Room.
- Números que o usuário adicionou explicitamente à whitelist MUST ser sempre permitidos,
  independentemente de plano, licença ou demais configurações.

### II. Privacidade Local-First

O app lida com contatos e histórico de chamadas — dados pessoais sensíveis (LGPD/GDPR).

- Todos os dados (whitelist, cache de contatos, histórico de decisões, preferências) MUST ficar
  exclusivamente no dispositivo. Não existe backend; nenhum dado pessoal é enviado a terceiros.
  A única comunicação de rede permitida é a do Google Play Billing.
- Números de telefone e nomes de contatos MUST NOT aparecer em logs de builds release; logs de
  depuração com dados pessoais MUST ser condicionados a `BuildConfig.DEBUG` ou mascarados.
- Dados sensíveis (banco Room, cache de contatos, histórico) MUST ser excluídos do backup em
  nuvem e da transferência entre dispositivos via `data_extraction_rules`/`backup_rules`.
- Leitura de contatos MUST ser precedida de consentimento explícito no app (tela de consentimento)
  além da permissão de runtime; revogar consentimento MUST apagar o cache de contatos.
- Permissões MUST ser as mínimas necessárias; toda nova permissão exige justificativa no plano.
- O histórico de decisões MUST ter política de retenção definida (limite de idade ou quantidade).

### III. Identidade Canônica de Números

- E.164 é a única chave de identidade de um número em todo o app (whitelist, cache de
  contatos, histórico, chamadas recebidas).
- Uma única implementação de normalização (`PhoneNormalizer`) MUST ser usada em todos os pontos
  de entrada: digitação manual, importação de contatos e `Call.Details.handle`.
- A normalização MUST aceitar formatos nacionais usando a região padrão do dispositivo/SIM
  (ex.: `11 98765-4321`, `011...` no Brasil) e não apenas números com `+`.
- Comparações de números MUST ser feitas sobre o valor normalizado, nunca sobre a string bruta.

### IV. Motor de Decisão Puro e Testado

- A lógica de decisão de chamada vive na camada `domain` e MUST ser testável em JVM pura, sem
  dependências do framework Android (acesso a dados via interfaces/DAOs substituíveis por fakes).
- Todo `CallDecisionReason` MUST ter ao menos um teste unitário que o produza.
- Todo bug corrigido no motor de decisão, na normalização ou na triagem MUST vir acompanhado de
  um teste de regressão que falha antes da correção.
- Toda mudança de schema Room MUST incrementar a versão, exportar o schema e incluir `Migration`
  explícita com teste em `CallGuardDatabaseMigrationTest`. Migração destrutiva em upgrade é
  proibida (perderia a whitelist do usuário).

### V. Paridade de Localização

- Todo texto visível ao usuário MUST estar em `strings.xml`; strings hardcoded em Composables
  são proibidas.
- Toda string MUST existir nos três idiomas suportados: inglês (`values`), português do Brasil
  (`values-pt-rBR`) e espanhol (`values-es-rES`). Uma feature só está pronta com os três.
- Textos legais (termos, privacidade) MUST refletir o comportamento real do app em cada idioma.

### VI. Monetização Honesta

- O plano gratuito MUST ser útil por si só: whitelist manual até `FREE_WHITELIST_LIMIT` com
  proteção real ativa.
- Recursos Premium (ex.: liberar contatos automaticamente) são decididos no domínio a partir de
  `LicenseState`; a UI apenas reflete esse estado.
- O Google Play Billing é a fonte da verdade da licença; o DataStore é apenas espelho/cache.
  Perda de licença MUST degradar para o comportamento gratuito sem bloquear números que o
  usuário adicionou manualmente (Princípio I).
- Mocks/overrides de licença MUST existir apenas sob `BuildConfig.DEBUG`.
- O paywall MUST ser dispensável (opção "continuar grátis") e nunca bloquear o acesso às
  funções gratuitas.

## Restrições Técnicas

- Plataforma: Android nativo, Kotlin, Jetpack Compose (Material 3), Navigation Compose.
- SDK: `minSdk 26`, `targetSdk`/`compileSdk 36`. A triagem via papel `ROLE_CALL_SCREENING`
  requer API 29+; o comportamento em API 26–28 MUST ser definido explicitamente (degradação
  documentada na UI), nunca falhar silenciosamente.
- Persistência: Room (dados estruturados) e DataStore Preferences (configurações/licença).
- Billing: Google Play Billing Library (`billing-ktx`).
- Camadas: `ui` → `domain` ← `data`; `telecom` orquestra domínio e dados. A camada `domain` MUST
  NOT depender de `ui`.
- Simplicidade: sem framework de DI ou novas dependências pesadas sem justificativa registrada
  no `plan.md` (seção de Complexity Tracking). Preferir libs AndroidX/Google já em uso; exceção
  prevista: `libphonenumber` para o Princípio III.
- Versões de dependências centralizadas em `gradle/libs.versions.toml`.

## Fluxo de Desenvolvimento e Quality Gates

- Todo trabalho não trivial segue o Spec Kit: `/speckit-specify` → (`/speckit-clarify`) →
  `/speckit-plan` → `/speckit-tasks` → (`/speckit-analyze`) → `/speckit-implement`.
  Specs em `specs/NNN-nome/`, numeração sequencial.
- Branches de feature partem de `development`; `master` recebe apenas código liberável.
- Gates obrigatórios antes de merge:
  - `./gradlew testDebugUnitTest` verde;
  - `./gradlew lintDebug` sem novos erros;
  - `./gradlew assembleRelease` compilando;
  - mudanças de schema: `connectedDebugAndroidTest` da migração verde.
- Mudanças em `AppCallScreeningService`, `CallDecisionEngine` ou `PhoneNormalizer` MUST incluir
  validação manual em dispositivo real (ou emulador com papel de triagem concedido), registrada
  no checklist da feature.
- Commits seguem Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`).

## Governance

- Esta constituição prevalece sobre outras práticas do projeto. Todo `plan.md` MUST conter a
  seção "Constitution Check" avaliando cada princípio; violações MUST ser justificadas em
  Complexity Tracking ou eliminadas.
- Emendas: alteração via `/speckit-constitution`, com Sync Impact Report atualizado e commit
  `docs: amend constitution to vX.Y.Z (...)`.
- Versionamento semântico:
  - MAJOR: remoção ou redefinição incompatível de princípio;
  - MINOR: novo princípio/seção ou expansão material;
  - PATCH: clarificações e redação.
- Revisões de PR MUST verificar conformidade, com atenção especial aos Princípios I e II.
- Violações conhecidas no código existente são tratadas como débito e priorizadas como features
  do Spec Kit; código novo MUST NOT introduzir novas violações.

**Version**: 1.0.0 | **Ratified**: 2026-10-13 | **Last Amended**: 2026-10-13
