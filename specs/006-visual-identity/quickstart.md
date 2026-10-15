# Quickstart: validar Identidade Visual e Temas

**Feature**: [spec.md](spec.md) · **Contratos**: [contracts/](contracts/)

## Pré-requisitos

- JDK 21 do sistema; emulador `Pixel_7_Pro` (API 37) e, se possível, o Samsung de teste.
- Build: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon -Porg.gradle.java.installations.auto-detect=false …`

## §1 Gates

```bash
./gradlew … :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

Esperado: verde; inclui `CallGuardPaletteContrastTest`, `ThemeModeTest` e `BackupCodecTest` (v1 e v2).
`app/schemas/` inalterado. `grep -rn "Color(0x\|Color\.\(White\|Black\)" app/src/main/java --include=*.kt | grep -v core/theme`
→ nada.

## §2 Ícone (US1)

1. Instalar e ver a tela inicial do launcher: símbolo navy, inteiro.
2. Formatos: conferir as prévias de máscara geradas na T027 (círculo, quadrado arredondado e gota)
   e o ícone instalado no Pixel (círculo) e no Samsung (quadrado arredondado). O símbolo deve
   aparecer inteiro em todos (SC-002).
3. Papel de parede e estilo → *Ícones temáticos* ligado: o símbolo aparece monocromático.

## §3 Abertura e apresentação (US1)

1. `adb shell pm clear com.lsp.callguard`, celular no **tema escuro**, abrir: abertura com símbolo
   sobre fundo escuro, sem flash branco; apresentação com símbolo, "Call**Guard**" e tagline.
2. Repetir com idioma en e es: tagline traduzida.

## §4 Esquema de cores (US2)

1. Tema claro: capturar apresentação, oferta, Home, whitelist (lista e diálogo de inclusão),
   histórico (permitido e bloqueado — `adb emu gsm call`), Configurações (todos os cards),
   diálogos de backup, idioma, termos.
2. Conferir os papéis com [contracts/color-roles.md](contracts/color-roles.md): bloqueado em
   vermelho e permitido em verde, ambos com ícone e texto; "Salvar" desabilitado apagado.

## §5 Temas (US3)

1. Instalação nova → Configurações → Aparência mostra **Sistema**.
2. Tocar **Escuro**: muda na hora; a tela continua em Configurações, rolada no mesmo ponto.
3. Repetir as capturas do §4 no escuro: nenhum elemento claro sobrando (SC-004); barras do sistema
   com ícones claros.
4. Fechar e reabrir: continua escuro com o celular em claro (`adb shell cmd uimode night no`).
5. Escolher **Sistema** e alternar `cmd uimode night yes|no`: o app acompanha.
6. Fonte do sistema em 200% (`adb shell settings put system font_scale 2.0`): telas legíveis.

## §6 Backup (FR-014)

1. Tema Escuro → exportar arquivo; mudar para Claro → importar: volta ao Escuro.
2. Importar um arquivo exportado antes desta feature (v1): importa normalmente e o tema não muda.

## §7 Não regressão

- Ligação de número desconhecido continua bloqueada e da whitelist permitida (triagem não depende do
  tema; abrir o processo pelo serviço não lê a preferência de tema).
