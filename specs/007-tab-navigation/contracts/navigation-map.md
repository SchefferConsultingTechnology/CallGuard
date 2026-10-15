# Contract: Mapa de navegação

## Rotas

| Rota | Tipo | Barra | Seta voltar | Origem → retorno |
|---|---|---|---|---|
| `home` | aba | sim | não | — |
| `whitelist` | aba | sim | **não** (removida) | — |
| `call_history` | aba | sim | **não** (removida) | — |
| `settings` | aba (início do grafo) | sim | não | — |
| `settings/{category}` | página da aba | sim | sim → `settings` | — |
| `onboarding` | tarefa (primeiro uso) | não | — | início do app → paywall |
| `paywall?firstRun=true` | tarefa (primeiro uso) | não | voltar → onboarding | → `home` limpando a pilha |
| `paywall?firstRun=false` | tarefa | não | sim | Início (card plano), Whitelist (limite), Histórico (limite), Ajustes → Plano, Contatos → origem |
| `protection_setup` | tarefa | não | sim | Início (card proteção) → Início |
| `contacts_consent` | tarefa | não | sim | Ajustes → Contatos → Contatos |
| `contacts_import` | tarefa | não | — | substitui o consentimento → `popBackStack` → Ajustes → Contatos |
| `terms`, `privacy_policy` | tarefa | não | sim | Ajustes → Privacidade, consentimento → origem |
| `language` | tarefa | não | "Concluir" → origem | Ajustes → Aparência e idioma |

## Comportamentos da barra

| Ação | Resultado |
|---|---|
| Toque em aba diferente | `navigate(aba) { popUpTo(home){saveState}; launchSingleTop; restoreState }` |
| Toque na aba atual (Ajustes em categoria) | volta para `settings` |
| Toque na aba atual (demais) | rola a lista até o topo |
| Voltar do sistema em aba ≠ Início | vai para `home` (estado da aba guardado) |
| Voltar no Início | fecha o app |

## Textos (strings.xml, 3 idiomas)

| Chave | pt-BR | en | es |
|---|---|---|---|
| `tab_home` | Início | Home | Inicio |
| `tab_whitelist` | Whitelist | Whitelist | Whitelist |
| `tab_history` | Histórico | History | Historial |
| `tab_settings` | Ajustes | Settings | Ajustes |
