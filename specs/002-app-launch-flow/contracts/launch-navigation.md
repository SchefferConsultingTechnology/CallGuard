# Contract: Navegação de Abertura e Idioma

Contrato interno (UI). Cada linha das tabelas vira um teste unitário ou um passo do quickstart.

## `resolveStartDestination` (função pura, `ui/navigation/StartDestination.kt`)

```kotlin
data class LegacySignals(
    val hasSavedLanguage: Boolean,
    val hasWhitelistEntries: Boolean,
    val isLicensed: Boolean
)

data class StartResolution(
    val route: String,                       // rota inicial do NavHost
    val progressToPersist: OnboardingProgress? // não nulo quando a derivação deve ser gravada
)

fun resolveStartDestination(progress: OnboardingProgress?, legacy: LegacySignals): StartResolution
```

| `progress` | legacy (idioma / whitelist / licença) | `route` | `progressToPersist` |
|---|---|---|---|
| `COMPLETED` | qualquer | `home` | `null` |
| `ONBOARDING_DONE` | qualquer | `paywall?firstRun=true` | `null` |
| `NOT_STARTED` | qualquer (inclusive idioma salvo) | `onboarding` | `null` |
| `null` | nenhum sinal | `onboarding` | `NOT_STARTED` |
| `null` | só idioma | `home` | `COMPLETED` |
| `null` | só whitelist | `home` | `COMPLETED` |
| `null` | só licença | `home` | `COMPLETED` |

Falha de leitura do progresso → chamar com `progress = null`; falha também nos sinais →
`LegacySignals(false, false, false)` sem gravar (o chamador não persiste em caso de erro).

## Rotas

| Rota | Origem | Ação | Resultado |
|---|---|---|---|
| `onboarding` | início (primeiro uso) | tocar idioma | aplica + grava idioma; tela recriada no mesmo lugar |
| `onboarding` | — | "Continuar" | grava `ONBOARDING_DONE`; `navigate(paywall?firstRun=true)` sem `popUpTo` |
| `paywall?firstRun=true` | onboarding ou início | "continuar grátis" / compra | grava `COMPLETED`; `navigate(home) { popUpTo(graph) { inclusive = true } }` |
| `paywall?firstRun=true` | onboarding | voltar (sistema) | volta à apresentação |
| `paywall` (`firstRun=false`) | Home, Whitelist, Settings, Consent | "continuar grátis" / compra / voltar | `popBackStack()` |
| `language` | Settings → Idioma | tocar idioma | aplica + grava; tela recriada no mesmo lugar, novo idioma marcado |
| `language` | Settings → Idioma | "Concluir" / voltar | `popBackStack()` → Settings |
| `home` | início ou fim do primeiro uso | voltar (sistema) | fecha o app |

## `OnboardingPreferences`

```kotlin
class OnboardingPreferences(dataStore: DataStore<Preferences>) {
    suspend fun progress(): OnboardingProgress?          // null = chave ausente
    suspend fun advanceTo(progress: OnboardingProgress)   // ignora regressão (data-model)
}
```

## `LanguageSelectionViewModel` (mudança)

- `onLanguageSelected(language)`: atualiza seleção, grava em `LanguagePreferences` e chama
  `LocaleManagerHelper.applyLanguage(language)`.
- `onConfirm` removido.
