# Contract: Painel do Início

```text
┌──────────────────────────────────────┐
│ CallGuard                            │  título + subtítulo (sem engrenagem)
│ Proteja suas chamadas…               │
│ ┌──────────────────────────────────┐ │  aviso de mudança de plano (só quando pendente)
│ └──────────────────────────────────┘ │
│ ┌──────────────────────────────────┐ │  ProtectionStatusCard (atual, sem mudança)
│ │ 🛡 Proteção ativa                 │ │
│ │ [ Proteção ativa ]               │ │
│ └──────────────────────────────────┘ │
│ ┌───────────────┐ ┌───────────────┐  │  cards compactos, mesma altura, tocáveis
│ │ 📞 Whitelist  │ │ ⭐ Plano       │  │
│ │ 3 de 5        │ │ Grátis        │  │  whitelist → aba Whitelist
│ │ ▓▓▓▓▓░░░      │ │ Ver planos ›  │  │  plano → oferta (paywall)
│ └───────────────┘ └───────────────┘  │
│ ┌──────────────────────────────────┐ │  RecentCallsCard
│ │ Últimas chamadas       Ver tudo › │ │  "Ver tudo" e cada linha → aba Histórico
│ │ ⊘ 11955550101   Bloqueada  15:32 │ │  ícone + texto de estado (cores da 006)
│ │ ✓ (650) 555-0103 Permitida 14:32 │ │
│ │ ✓ (650) 555-0101 Permitida 09:10 │ │
│ └──────────────────────────────────┘ │  vazio: "Nenhuma chamada analisada ainda."
└──────────────────────────────────────┘
```

- Card de whitelist: Grátis mostra "3 de 5" + barra (cor de aviso no limite, como na 006). Plus/Pro
  mostram a contagem (plural existente `whitelist_count_unlimited`) sem barra.
- Card de plano: nome do nível. "Ver planos ›" aparece só no Grátis; no Plus/Pro o card mostra
  "Gerenciar".
- Horário: hoje → "HH:mm"; dias anteriores → data curta do idioma.

## Textos novos

| Chave | pt-BR | en | es |
|---|---|---|---|
| `home_recent_calls` | Últimas chamadas | Recent calls | Últimas llamadas |
| `home_see_all` | Ver tudo | See all | Ver todo |
| `home_no_calls_yet` | Nenhuma chamada analisada ainda. | No calls screened yet. | Aún no se analizaron llamadas. |
| `home_plan_manage` | Gerenciar | Manage | Gestionar |
| `home_whitelist_short` | %1$d de %2$d | %1$d of %2$d | %1$d de %2$d |
| `home_whitelist_count_short` (plural) | %1$d número(s) | %1$d number(s) | %1$d número(s) |
| `home_plan_title` | Plano | Plan | Plan |
| `home_plan_pending` | Pagamento pendente | Payment pending | Pago pendiente |
| `home_plan_not_renewing` | Não renova | Not renewing | No se renueva |

Os textos curtos dos cards compactos foram acrescentados na implementação: os textos longos atuais não
cabem em meio card.

