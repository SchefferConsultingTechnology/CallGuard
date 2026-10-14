# Data Model: Fluxo de Abertura e Idioma

**Feature**: 002-app-launch-flow | **Date**: 2026-10-14

Sem mudanças no Room. Uma chave nova no DataStore `app_preferences`.

## OnboardingProgress (enum, persistido como string)

| Valor | Significado | Primeira tela ao abrir |
|---|---|---|
| `NOT_STARTED` | Primeiro uso não iniciado ou apresentação não concluída | Apresentação (Onboarding) |
| `ONBOARDING_DONE` | Tocou "Continuar" na apresentação, ainda sem escolha na oferta | Oferta do Premium (`firstRun = true`) |
| `COMPLETED` | Assinou ou escolheu continuar grátis no primeiro uso | Home |

Chave: `stringPreferencesKey("onboarding_progress")` em `OnboardingPreferences`
(`data/local/preferences`). Valor ausente = "não sabemos" (instalação nova **ou** versão
anterior) → ver derivação.

### Transições

```text
(ausente) ──derivação R3──► NOT_STARTED ──"Continuar" na apresentação──► ONBOARDING_DONE
    │                                                                         │
    └──sinais da versão anterior──► COMPLETED ◄──"continuar grátis" ou compra──┘
```

- `COMPLETED` é terminal (só "apagar dados do app" volta ao início).
- Nunca regride: gravar um estado anterior ao atual é ignorado.

## LegacySignals (valor de entrada, não persistido)

| Campo | Fonte |
|---|---|
| `hasSavedLanguage` | `LanguagePreferences.selectedLanguageFlow.first() != null` |
| `hasWhitelistEntries` | contagem de `allowed_numbers` > 0 |
| `isLicensed` | `LicensePreferences.licenseFlow.first().isLicensed` |

Lidos **apenas** quando `onboarding_progress` está ausente.

## AppLanguage (existente) — adição

- `forLocale(locale: Locale): AppLanguage?` — mapeia pelo código de idioma (`pt`, `en`, `es`);
  outros → `null`.
