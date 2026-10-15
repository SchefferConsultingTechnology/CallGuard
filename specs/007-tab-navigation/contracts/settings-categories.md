# Contract: Ajustes por categoria

## Lista (`settings`)

```text
Ajustes
┌──────────────────────────────────────┐
│ ⭐ Plano e compras                  › │
│    Grátis                            │
│──────────────────────────────────────│
│ 🛡 Proteção de chamadas             › │
│    Desconhecidos e privados bloq.    │
│ … (6 linhas, um único card)          │
└──────────────────────────────────────┘
CallGuard · Versão 1.0.0
```

- As 6 categorias ficam num único `SettingsCard`, com divisórias, na ordem de `SettingsCategory`.
- O cabeçalho é "Ajustes" com o subtítulo atual. Não há seta de voltar, porque é uma aba.

## Página (`settings/{category}`)

- Cabeçalho com seta de voltar, título da categoria e os cards listados em research § R6.
- Os snackbars de cada página (restaurar compras, backup) ficam na própria página.

## Textos novos

| Chave | pt-BR | en | es |
|---|---|---|---|
| `settings_category_plan` | Plano e compras | Plan and purchases | Plan y compras |
| `settings_category_protection` | Proteção de chamadas | Call protection | Protección de llamadas |
| `settings_category_contacts` | Contatos | Contacts | Contactos |
| `settings_category_backup` | Cópia de segurança | Backup | Copia de seguridad |
| `settings_category_appearance` | Aparência e idioma | Appearance and language | Apariencia e idioma |
| `settings_category_privacy` | Privacidade e termos | Privacy and terms | Privacidad y términos |
| `summary_block_both` | Desconhecidos e privados bloqueados | Unknown and private numbers blocked | Desconocidos y privados bloqueados |
| `summary_block_unknown` | Desconhecidos bloqueados | Unknown numbers blocked | Desconocidos bloqueados |
| `summary_block_private` | Privados bloqueados | Private numbers blocked | Privados bloqueados |
| `summary_block_none` | Nenhum bloqueio ativo | No blocking active | Ningún bloqueo activo |
| `summary_contacts_locked` | Disponível no Plus | Available on Plus | Disponible en Plus |
| `summary_on` | Ativado | On | Activado |
| `summary_off` | Desativado | Off | Desactivado |
| `summary_backup` | Arquivo protegido por senha | Password-protected file | Archivo protegido con contraseña |
| `summary_appearance` | %1$s · %2$s | %1$s · %2$s | %1$s · %2$s |
| `summary_language_device` | Idioma do aparelho | Device language | Idioma del dispositivo |
| `summary_privacy` | Sem anúncios, dados no aparelho | No ads, data stays on device | Sin anuncios, datos en el dispositivo |

Os nomes dos níveis (`tierDisplayName`), dos temas (`theme_*`) e dos idiomas (`AppLanguage`) já existem.
