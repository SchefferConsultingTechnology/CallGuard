# Data Model: Navegação por Abas

**Feature**: [spec.md](spec.md) · **Decisões**: [research.md](research.md)

Sem mudança em Room, DataStore ou no formato do backup. Os modelos abaixo são de UI e navegação.

## AppTab (`ui/navigation/AppTab.kt`)

| Aba | Rota de destino | Ícone (outlined / filled quando marcada) | Texto |
|---|---|---|---|
| `HOME` | `home` | Home | `tab_home` |
| `WHITELIST` | `whitelist` | VerifiedUser | `tab_whitelist` |
| `HISTORY` | `call_history` | History | `tab_history` |
| `SETTINGS` | `settings_graph` (início `settings`) | Settings | `tab_settings` |

`fun tabFor(route: String?): AppTab?`:
- `home` → HOME;
- `whitelist` → WHITELIST;
- `call_history` → HISTORY;
- `settings` e `settings/{category}` → SETTINGS;
- qualquer outra rota ou `null` → `null` (barra oculta).

## SettingsCategory (`ui/screen/settings/SettingsCategory.kt`)

| Valor | Rota | Ícone | Título |
|---|---|---|---|
| `PLAN` | `settings/plan` | WorkspacePremium | `settings_category_plan` |
| `PROTECTION` | `settings/protection` | Security | `settings_category_protection` |
| `CONTACTS` | `settings/contacts` | Contacts | `settings_category_contacts` |
| `BACKUP` | `settings/backup` | Backup | `settings_category_backup` |
| `APPEARANCE` | `settings/appearance` | Contrast | `settings_category_appearance` |
| `PRIVACY` | `settings/privacy` | PrivacyTip | `settings_category_privacy` |

## Summary (resumo de categoria)

`data class Summary(@StringRes val text: Int, val args: List<Any> = emptyList())`. Para o tema e o
idioma, o argumento é outro id de string, resolvido na UI.

Regras de `settingsSummaries(state)`:

| Categoria | Estado | Resumo |
|---|---|---|
| PLAN | tier | nome do nível ("Grátis", "Plus", "Pro") |
| PROTECTION | blockUnknown ∧ blockPrivate | "Desconhecidos e privados bloqueados" |
| | só blockUnknown | "Desconhecidos bloqueados" |
| | só blockPrivate | "Privados bloqueados" |
| | nenhum | "Nenhum bloqueio ativo" |
| CONTACTS | plano Grátis | "Disponível no Plus" |
| | pago, ligado | "Ativado" |
| | pago, desligado | "Desativado" |
| BACKUP | — | "Arquivo protegido por senha" |
| APPEARANCE | themeMode × idioma | "{tema} · {idioma}" (idioma nulo → "Idioma do aparelho") |
| PRIVACY | — | "Sem anúncios, dados no aparelho" |

## SettingsUiState (alterado)

Ganha o campo `language: AppLanguage?`, vindo de `LanguagePreferences.selectedLanguageFlow`. Os demais
campos (`appSettings`, `tier`, `themeMode`) não mudam.

## RecentCall e HomeUiState (alterados)

`data class RecentCall(id: String, displayPhone: String?, allowed: Boolean, createdAt: Long)`. Quando
`displayPhone` é `null`, a linha mostra "Número privado".

`HomeUiState` ganha o campo `recentCalls: List<RecentCall>`, com no máximo 3 itens, do mais recente
para o mais antigo. Os campos `useContactsAutomatically` e `importedContactsCount` deixam de ser usados
pelo Início e são removidos, junto com a leitura do `DeviceContactDao` na `HomeViewModel`.

## CallLogDisplay (`domain/phone/CallLogDisplay.kt`)

```text
canonicalOf(log, region, normalizer): String?       // normalizedPhone ?: normalize(phone)
displayPhoneOf(log, region, normalizer, formatter): String?   // formatOrRaw(canonical, phone, region); null = oculto
```

São extraídos do `CallHistoryViewModel` sem mudar o comportamento. O Histórico e o Início usam ambos.

## Estados de navegação

```text
[primeiro uso] onboarding → paywall(firstRun) ──concluir──► home (barra visível)

home ⇄ whitelist ⇄ call_history ⇄ settings_graph      (trocas de aba: estado salvo/restaurado)
settings → settings/{category} → (voltar) settings
qualquer aba → tarefa (sem barra) → (concluir/cancelar) mesma aba e página
voltar: aba ≠ home → home;  home → fecha o app
```
