# Quickstart: validar os Planos Plus e Pro

**Feature**: 003-premium-plans

## 0. Pré-requisitos da loja (manual, responsável pelo app)

1. App `com.lsp.callguard` criado na Play Console e um build enviado a uma **trilha de teste
   interno** (mesma versão/assinatura do build a ser testado).
2. Produtos criados conforme [research R1](research.md#r1-produtos-na-play-console), com preços BR.
3. Contas Google de teste cadastradas em **Testadores de licença** e na trilha interna; o aparelho
   de teste (ex.: Samsung SM-G990E) logado com uma delas.

Sem estes passos, a loja não devolve produtos: a oferta deve mostrar a mensagem de loja
indisponível (US2-3) — isso já é testável.

## 1. Gates automatizados

```bash
./gradlew testDebugUnitTest lintDebug assembleRelease   # flags de ambiente da 001
git diff --exit-code app/schemas/
```

Esperado: testes de [contracts/entitlements.md](contracts/entitlements.md) verdes.

## 2. Sem loja (qualquer aparelho/emulador)

- Oferta: três níveis, "Sem anúncios" como comum a todos, Pro "em breve", sem preços e com
  "tente de novo"; "continuar grátis" funciona.
- Build de debug: seletor de nível fictício (Grátis/Plus/Pro) nas Configurações; conferir limites:
  Grátis bloqueia o 6º número; Plus libera whitelist e contatos; histórico apaga registros antigos
  conforme o nível (inserir registros antigos via teste ou ajustar relógio do emulador).

## 3. Compras com testador de licença

| Caso | Esperado |
|---|---|
| Plus mensal | recursos Plus em ≤ 5 s; oferta indica plano atual |
| Plus anual (elegível) | "7 dias grátis" exibido; após compra, Plus ativo |
| Plus vitalício | Plus ativo; sem data de renovação |
| Cancelar Plus mensal na Play | continua Plus até o fim; depois Grátis com aviso e whitelist intacta |
| Pagamento recusado (cartão de teste "recusa") | mensagem da Play sobre pagamento; recursos mantidos na carência |
| Compra pendente (método "aprovar depois") | "pagamento em processamento"; recursos só após aprovar |
| Reinstalar o app | plano restaurado sozinho / "Restaurar compras" |

Testadores têm assinaturas aceleradas (ex.: mensal renova em minutos) — usar para ver
expiração/renovação.

## 4. Rebaixamento (US3)

- Com Plus e 12 números, cancelar e aguardar expiração (testador): os 12 continuam permitidos
  (simular chamada de um deles); adicionar o 13º → limite; aviso único na Home; histórico > 7 dias
  apagado.

Registrar em `specs/003-premium-plans/checklists/manual-validation.md`.
