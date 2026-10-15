# Manual Validation: Identidade Visual e Temas

**Feature**: [spec.md](../spec.md) · Roteiro: [quickstart.md](../quickstart.md)

## Ambiente

- **Data**: 2026-10-15
- **Dispositivo**: emulador `Pixel_7_Pro`, Android API 37, pt-BR

## US2 — Esquema de cores (tema claro)

- [x] **Contraste (SC-001)**: `CallGuardPaletteContrastTest` verde nos dois temas (22 pares de texto ≥ 4,5;
  2 pares de controle ≥ 3,0).
- [x] **Sem cores fixas (SC-006)**: varredura `Color(0x…)`, `Color.White/Black/…`, `.copy(alpha` fora de
  `core/theme` → nenhuma ocorrência.
- [x] **Home**: cards brancos com borda, botões primários azuis, botões tonais azul-claro, círculos de
  ícone em `primaryContainer`; barra de uso da whitelist em azul (3 de 5).
- [x] **Aviso de rebaixamento** (Plus → Grátis pelo seletor de debug): fundo amarelo de aviso, ícone de
  alerta, texto marrom legível, "Ver planos" primário e "Entendi" na cor do aviso.
- [x] **Histórico** (ligação simulada `11955550101` bloqueada + `(650) 555-0103` permitida): bloqueado
  com ícone vermelho em círculo vermelho-claro e "Bloqueado" em vermelho; permitido com ícone verde e
  "Permitido" em verde; resumo com selos verde e vermelho. Cor nunca é o único sinal (ícone + texto).
- [x] **Whitelist**: cartão de uso, botão contornado "Ver planos" com borda `outline` visível, card
  tingido de destaque em `surfaceVariant`, "Adicionar número" primário.
- [x] **Oferta**: plano atual com fundo `surfaceVariant` e borda azul, selo "Plano atual", aviso de
  preços indisponíveis legível.
- [x] **Configurações**: todos os cards com o padrão; switches ligado (azul) e desligado (cinza com
  borda) distinguíveis; seletor de plano de debug.

## US3 — Claro / Escuro / Seguir o sistema

- [x] **Testes JVM**: `ThemeModeTest` (3), `BackupCodecTest` (6, inclui arquivo v1 escrito à mão e ida e
  volta v2 com cada tema), `BackupRepositoryTest` (tema Escuro exportado num aparelho volta no outro).
  Total da suíte: 152 testes, 0 falhas.
- [x] **Padrão**: instalação nova → Configurações → Aparência marcada em **Sistema**.
- [x] **Troca na hora (SC-003)**: toque em "Escuro" às 15:55:45,630 (marcador no logcat) → tela recriada
  desenhada às 15:55:45,802 (`finishDrawing of relaunch … 149ms`) = **172 ms**. Uma única recriação
  (U2). A tela continuou em Configurações, no card Aparência.
- [x] **Tema escuro em todas as telas (SC-004)**: Home, Configurações (todas as seções), whitelist,
  diálogo "Adicionar número", histórico (permitido verde / bloqueado vermelho), oferta, diálogo
  "Exportar cópia", idioma, termos de uso — nenhum elemento claro sobrando; barra de status com ícones
  claros.
- [x] **Persistência**: Escuro escolhido, app fechado, celular em claro (`cmd uimode night no`) →
  reabre escuro (brilho médio 48).
- [x] **Seguir o sistema**: Sistema + celular claro → claro (245); `night yes` → escuro (38); `night no`
  → claro (245), sem reabrir o app.
- [x] **Fonte 200%**: telas legíveis. Achado e corrigido: "Sistema" era cortado ("Siste…") no seletor;
  removido o ícone de marcação e reduzido o padding do segmento — cabe em 100% e 200%.
- [x] **Backup no aparelho (§6)** (validado em 2026-10-15, durante a 007): exportado com Escuro, trocado
  para Claro, importado → volta ao Escuro, depois do aviso "Importação concluída".
- [ ] **Android 8–11 (API 26–30)**: sem emulador dessa versão; a leitura antes de `super.onCreate` e o
  `enableEdgeToEdge` nessas versões não foram vistos em aparelho.

## US1 — Ícone, abertura e logo (2026-10-15, versão 1.7.3)

- [x] **Aprovação (T028)**: símbolo da prévia (`assets/symbol-preview.png`) aprovado para uso pelo
  responsável ("Vamos testar com a imagem de preview que gerou").
- [x] **Ícone (SC-002)**: no launcher do Pixel (máscara redonda), o símbolo aparece inteiro sobre navy,
  sem cortes. Máscaras quadrado arredondado e gota conferidas nas prévias geradas (`symbol-preview.png`).
  Os `.webp` legados foram removidos (não usados com minSdk 26).
- [ ] **Ícones temáticos**: a versão monocromática (`ic_launcher_monochrome`) existe e está declarada no
  adaptive icon, mas a opção do launcher não foi ligada no emulador. Prévia em `assets/mono-preview.png`
  (gerador).
- [x] **Abertura sem flash (US1-4)**: gravação a 20 qps de aberturas a frio:
  - tema escuro: símbolo sobre fundo escuro (brilho ~21) de ~1,25 s a ~3,0 s, sem nenhum quadro branco;
  - tema claro: símbolo sobre fundo claro.
  - O símbolo fica direto sobre o fundo, sem círculo (U4).
- [x] **Apresentação (FR-004)**: símbolo + "Call**Guard**" ("Guard" na cor primária) + tagline em caixa
  alta, em pt-BR ("PRIVACIDADE EM CADA CHAMADA"), en ("PRIVACY IN EVERY CALL") e es ("PRIVACIDAD EN
  CADA LLAMADA"), nos temas claro e escuro.
- [x] **Ficha da Play**: `assets/play-icon-512.png` (512×512, navy, sem texto), gerado do mesmo desenho.
- [x] **Gates**: 165 testes, lint 0 erros (38 avisos), `assembleRelease` OK. Triagem: desconhecido
  rejeitado.
