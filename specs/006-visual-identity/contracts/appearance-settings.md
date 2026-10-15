# Contract: Card "Aparência" em Configurações

Posição: seção "Aplicativo" de Configurações, logo após o card de Idioma (`AppSettingsCard`).

```text
┌────────────────────────────────────────────┐
│ (◐)  Aparência                             │
│      Escolha entre tema claro e escuro.    │
│                                            │
│  [ Sistema ✓ ][   Claro   ][  Escuro   ]   │  ← SingleChoiceSegmentedButtonRow
└────────────────────────────────────────────┘
```

- Mesmo `SettingsCard` / `SettingsItemHeader` dos demais cards (ícone `Icons.Outlined.Contrast`).
- Toque numa opção: grava e aplica na hora; a opção marcada reflete `themeModeFlow`.
- Leitor de tela: cada segmento anuncia o nome e "selecionado".

## Textos (strings.xml, 3 idiomas)

| Chave | pt-BR | en | es |
|---|---|---|---|
| `appearance_title` | Aparência | Appearance | Apariencia |
| `appearance_description` | Escolha entre tema claro e escuro. | Choose between light and dark theme. | Elige entre tema claro y oscuro. |
| `theme_system` | Sistema | System | Sistema |
| `theme_light` | Claro | Light | Claro |
| `theme_dark` | Escuro | Dark | Oscuro |
| `brand_tagline` | Privacidade em cada chamada | Privacy in every call | Privacidad en cada llamada |
