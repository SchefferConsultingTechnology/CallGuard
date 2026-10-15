# Contract: Papéis de cor e pares de contraste

Fonte da verdade dos valores: [research.md § R2](../research.md). Este contrato define **quais pares
o teste `CallGuardPaletteContrastTest` verifica** e **qual papel cada elemento de tela usa**.

## Pares verificados (por tema)

Texto (≥ 4,5:1):

| Texto | Sobre |
|---|---|
| onBackground | background |
| onSurface | surface, dialogSurface, surfaceVariant |
| onSurfaceVariant | background, surface, dialogSurface, surfaceVariant |
| primary | background, surface, dialogSurface, surfaceVariant |
| onPrimary | primary |
| onPrimaryContainer | primaryContainer |
| onSecondaryContainer | secondaryContainer |
| error, success, warning | surface |
| onErrorContainer | errorContainer |
| onSuccessContainer | successContainer |
| onWarningContainer | warningContainer |
| inverseOnSurface | inverseSurface |

Ícone / borda de controle (≥ 3,0):

| Elemento | Sobre |
|---|---|
| outline | surface |
| primary | primaryContainer |

## Uso nas telas

| Elemento | Papel |
|---|---|
| Fundo de tela (Scaffold) | background |
| Card | surface + borda 1 dp outlineVariant (sempre, nos dois temas) |
| Título / texto de card | onSurface |
| Descrição, legenda | onSurfaceVariant |
| Link, texto destacado, ícone de ação | primary |
| Círculo atrás de ícone de card | primaryContainer + ícone primary (substitui `primary.copy(alpha=…)`) |
| Botão principal | Button (primary/onPrimary) |
| Ação secundária de destaque | FilledTonalButton (secondaryContainer) |
| Ação alternativa | OutlinedButton (borda outline, texto primary) |
| Desabilitado | padrão Material 3 (onSurface 38% / 12%) |
| Histórico — permitido | ícone + texto `success`; selo successContainer/onSuccessContainer |
| Histórico — bloqueado | ícone + texto `error`; selo errorContainer/onErrorContainer |
| Aviso (limite, pagamento pendente, rebaixamento) | warningContainer/onWarningContainer + ícone |
| Diálogo | dialogSurface |
| Snackbar | inverseSurface / inverseOnSurface |

**Proibido** em telas: `Color(0x…)`, `Color.White/Black/...` e cores de `colors.xml`. Toda cor vem de
`MaterialTheme.colorScheme` ou `CallGuardTheme.extendedColors`.
