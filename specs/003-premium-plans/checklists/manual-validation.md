# Manual Validation: Planos Plus e Pro

**Feature**: [spec.md](../spec.md) · Roteiro: [quickstart.md](../quickstart.md)

## Ambiente

- **Data**: 2026-10-15
- **Dispositivo**: emulador `Pixel_7_Pro`, Android API 37
- **Loja**: **sem Play Console** (conta de organização adiada para a próxima etapa). A loja responde,
  mas o app não existe nela → catálogo vazio → estado "loja indisponível".
- **Planos**: alternados pelo seletor de plano simulado (somente build de debug).

## Resultados

- [x] **§1 Gates** — `testDebugUnitTest` 116 testes / 0 falhas (22 novos: nível vigente, recursos por
  plano, retenção, migração da licença); `lintDebug` 0 erros (44 avisos; os novos são sugestões de
  plural em textos com números fixos 5/7/90/365); `assembleRelease` OK; `app/schemas/` inalterado.
- [x] **§2 Sem loja** (pt-BR, en, es):
  - Oferta com Grátis / Plus / Pro, recursos de cada nível, Grátis como "Plano atual", "Sem anúncios"
    em "Em todos os planos", Pro com selo "Em breve" e sem botão de compra.
  - Aviso "Não foi possível carregar os preços da Google Play" com "Tentar de novo"; Plus com "Preço
    indisponível"; "Continuar com versão gratuita" funcional; texto de renovação automática.
  - Seletor de plano em Configurações (debug): Grátis → Plus → Pro → Grátis refletido na Home
    ("Plano: …") e nos limites.
- [x] **US3 — rebaixamento** (Plus com 6 números → Grátis):
  - Aviso único "Seu plano mudou de Plus para Grátis" com whitelist mantida, contatos desligados e
    histórico de 7 dias; Pro → Grátis mostra o mesmo; subir de plano não mostra aviso.
  - Ligação do 6º número após o rebaixamento: **permitida** (`ALLOWED_BY_WHITELIST`).
  - Cadastro do 7º número: bloqueado com "Limite gratuito atingido".
  - "Entendi" fecha o aviso e ele não volta ao reabrir o app.
- [x] **Oferta aberta de dentro do app** volta para a Home ("voltar").
- [x] **Textos**: "Premium" substituído por Plus / "Ver planos" em todo o app; termos de uso e
  política de privacidade reescritos nos 3 idiomas (planos, renovação, cancelamento, retenção).

## Pendente — depende da Play Console (próxima etapa)

- [ ] Preços reais da loja e selo "7 dias grátis" (quickstart §3).
- [ ] Compras com testadores de licença: Plus mensal, anual, vitalício (quickstart §3).
- [ ] Estados reais: cancelado ativo, pagamento recusado (mensagem da Play), pendente, expiração,
  reembolso (quickstart §3–§4).
- [ ] Restaurar compras com compra existente e reinstalação.
- [ ] Troca de plano Plus ↔ Pro e mensal ↔ anual (T033) — código pronto, `PRO_ON_SALE = false`.
- [ ] Retenção do histórico em aparelho: coberta por teste unitário do corte; não houve registros
  antigos para observar a exclusão real.

Guia de configuração da loja: https://claude.ai/code/artifact/df0424b6-f8ac-4f4f-b9b7-545f6da61ce2
