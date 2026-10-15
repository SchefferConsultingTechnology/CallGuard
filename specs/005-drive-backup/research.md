# Research: Backup no Google Drive

**Feature**: 005-drive-backup | **Date**: 2026-10-15

## R1. Escopo desta entrega

- **Decision**: Implementar agora a **US4 (arquivo exportado com senha)**, que não depende de conta.
  US1/US2/US3/US5 (Google Drive) ficam **bloqueadas** até existir um projeto no Google Cloud com
  cliente OAuth (pacote + SHA-1) e a API do Drive habilitada — configuração de conta, como a Play.
- **Rationale**: entrega valor (portabilidade LGPD, troca de aparelho manual) sem esperar a conta;
  o formato e a combinação de dados são os mesmos que o Drive usará depois.

## R2. Criptografia do arquivo (FR-008)

- **Decision**: Senha → chave com **PBKDF2WithHmacSHA256**, 120 000 iterações, sal aleatório de 16
  bytes; conteúdo cifrado com **AES-256-GCM**, IV aleatório de 12 bytes. Arquivo:
  `"CGBK"` (4 bytes) · versão do envelope (1 byte) · sal · IV · texto cifrado com tag.
  Senha errada ou arquivo alterado → falha da tag GCM → importação recusada sem tocar em nada.
- **Rationale**: disponível no Android desde a API 26 (minSdk) e na JVM — testável sem aparelho;
  GCM autentica, cobrindo "arquivo alterado" sem código extra; nenhuma dependência nova.
- **Alternatives considered**: Jetpack Security / Tink (dependência nova; chaves presas ao aparelho,
  inúteis para restaurar em outro).

## R3. Formato dos dados

- **Decision**: Conteúdo binário versionado com `DataOutputStream` (versão do formato, data,
  whitelist, configurações, histórico opcional). Leitura recusa versões maiores que a suportada
  ("atualize o app antes de importar").
- **Rationale**: JVM pura, sem dependência de JSON (org.json não roda em teste JVM; kotlinx
  serialization exigiria plugin novo).

## R4. O que entra (FR-002)

- **Decision**: whitelist (nome, número canônico, data de alteração); configurações
  `blockUnknown`, `blockPrivateNumbers` e idioma escolhido; histórico só se marcado. **Não** entram
  cache de contatos, consentimento/uso de contatos (refeitos no aparelho novo), plano/licença.

## R5. Combinar ao importar (FR-006)

- **Decision**: Função pura `mergeWhitelist(local, imported, normalizer, region)`: cada número
  importado passa de novo pela regra da 001; ausente localmente → inserir; presente com data mais
  nova no arquivo → atualizar o nome; nunca apaga (o arquivo não carrega remoções). Histórico:
  inserir registros cujo id não existe; depois aplicar a retenção do plano (003).
  Limite do plano não impede a importação (mesma regra do rebaixamento da 003).

## R6. Interface

- **Decision**: Configurações → seção "Cópia de segurança": "Exportar dados" (senha + confirmação,
  mínimo 8 caracteres, opção "incluir histórico", destino escolhido pelo seletor de arquivos do
  Android) e "Importar dados" (escolher arquivo, digitar senha, resumo do resultado). Disponível em
  todos os planos (FR-005). Nada sobre Drive é exibido enquanto ele não existir (Princípio VI).
