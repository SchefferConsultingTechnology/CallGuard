# Data Model: Identidade Visual e Temas Claro/Escuro

**Feature**: [spec.md](spec.md) · **Decisões**: [research.md](research.md)

Sem mudança no banco Room (schema v2 inalterado).

## ThemeMode (domain/model)

| Valor | Gravado como | Efeito |
|---|---|---|
| `SYSTEM` (padrão) | `"system"` | segue o tema do aparelho |
| `LIGHT` | `"light"` | sempre claro |
| `DARK` | `"dark"` | sempre escuro |

- `ThemeMode.fromStorage(value: String?)`: `null`, vazio ou desconhecido → `SYSTEM`.
- Função pura, testável na JVM; o mapeamento para os modos noturnos do AppCompat/UiModeManager fica
  em `core/theme` (camada Android).

## Preferência de aparência (DataStore `app_preferences`)

| Chave | Tipo | Padrão | Escrita por |
|---|---|---|---|
| `theme_mode` | String | ausente → `SYSTEM` | `AppearancePreferences.setThemeMode` (Configurações, importação de backup) |

`AppearancePreferences.themeModeFlow: Flow<ThemeMode>`; erros de leitura emitem `SYSTEM`.

## Paleta (core/theme/CallGuardPalette)

Dois conjuntos (`Light`, `Dark`) com os papéis da tabela R2 da pesquisa, em ARGB `Long`:

`background, surface, surfaceVariant, outline, outlineVariant, onBackground, onSurface,
onSurfaceVariant, primary, onPrimary, primaryContainer, onPrimaryContainer, secondaryContainer,
onSecondaryContainer, tertiary, error, onError, errorContainer, onErrorContainer, success,
successContainer, onSuccessContainer, warning, warningContainer, onWarningContainer, dialogSurface,
inverseSurface, inverseOnSurface`

Regra (verificada por teste): cada par texto/fundo listado em
[contracts/color-roles.md](contracts/color-roles.md) tem contraste ≥ 4,5; pares de ícone/borda de
controle ≥ 3,0.

## BackupSettings (domain/backup) — formato v2

| Campo | Tipo | Desde | Observação |
|---|---|---|---|
| `blockUnknown` | Boolean | v1 | |
| `blockPrivateNumbers` | Boolean | v1 | |
| `language` | String? | v1 | |
| `themeMode` | String? | **v2** | gravado no fim do arquivo; `null` ao ler v1 |

`BACKUP_FORMAT_VERSION`: 1 → **2**. Ver [contracts/backup-format-v2.md](contracts/backup-format-v2.md).

## Estados

```text
Abertura do app ──► lê theme_mode (≤300 ms; falha → SYSTEM)
                    └► aplica modo noturno ──► super.onCreate ──► tela no tema certo

Configurações: toque em Sistema/Claro/Escuro
  ──► grava theme_mode ──► aplica modo noturno ──► Activity recriada com estado preservado

Importação de backup v2 com themeMode ──► grava e aplica como acima
Importação de backup v1 ──► tema do aparelho não muda
```
