# Manual Validation: Liberar Número a Partir do Histórico

**Feature**: [spec.md](../spec.md) · Roteiro: [quickstart.md](../quickstart.md)

- **Data**: 2026-10-15 · **Dispositivo**: emulador `Pixel_7_Pro`, API 37 · plano Grátis

## Resultados

- [x] **Gates** — 125 testes / 0 falhas (9 novos); `lintDebug` 0 erros (44 avisos, sem novos);
  `assembleRelease` OK; schema inalterado.
- [x] **1–2** — chamadas bloqueadas `(650) 555-0199` e `(650) 555-0188` mostram "Adicionar à whitelist";
  `12345` (não interpretável) não mostra. Confirmação com número formatado e nome opcional
  ("Clinica"); item passa a "Na sua whitelist"; aviso "Adicionado à sua whitelist" com "Desfazer".
- [x] **3** — nova ligação de `6505550199` → **permitida** (`ALLOWED_BY_WHITELIST`).
- [x] **4** — `(650) 555-0188` adicionado e desfeito: whitelist volta a 1 número.
- [x] **5** — não interpretável sem ação; chamada permitida de número na whitelist mostra o selo.
  Número oculto: coberto por teste unitário (o emulador não gera chamada sem número).
- [x] **6** — Grátis com 5 números: ação → "Limite gratuito atingido" com explicação e "Ver planos"
  → abre a oferta e volta ao histórico.
- [x] **7** — textos em en ("Add to whitelist", "In your whitelist") e es ("Agregar a la whitelist",
  "En tu whitelist").

## Pendente

- [ ] Conclusão automática da inclusão após assinar o Plus a partir do limite: coberta por teste
  unitário (`free limit keeps the addition pending until upgrade`); no aparelho depende de compra
  real (Play Console). O plano simulado não serve aqui: trocá-lo exige sair do histórico.
