# Quickstart: validar o Fluxo de Abertura e Idioma

**Feature**: 002-app-launch-flow

## Pré-requisitos

- Emulador API 33+ (ex.: `Pixel_7_Pro`, API 37) ligado; `adb` no PATH.
- **Trocar o idioma do sistema** (§2, §7): é um passo **manual** — abrir com
  `adb shell am start -a android.settings.LOCALE_SETTINGS` e escolher o idioma na tela do
  Android (o idioma *do app* muda por `adb shell cmd locale set-app-locales`, mas o do sistema
  não tem comando equivalente sem root). Voltar o sistema ao idioma original ao terminar.
- Build: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon
  -Porg.gradle.java.installations.auto-detect=false :app:assembleDebug` (ver nota de ambiente na
  001).

## 1. Gates automatizados

```bash
./gradlew testDebugUnitTest lintDebug assembleRelease   # (com as flags acima)
git diff --exit-code app/schemas/                        # Room inalterado
```

Esperado: testes de [contracts/launch-navigation.md](contracts/launch-navigation.md) verdes.

## 2. Instalação nova (US2, SC-002, SC-003)

1. `adb uninstall com.lsp.callguard`; `adb shell cmd locale set-app-locales com.lsp.callguard
   --locales ""`; idioma do sistema em pt-BR; instalar o APK.
2. Abrir: **primeira tela = apresentação**, em português, "Português" marcado; "Continuar"
   habilitado.
3. Tocar "Español" → tela em espanhol na hora, continua na apresentação.
4. "Continuar" → oferta; "voltar" → apresentação; "Continuar" → oferta.
5. Fechar o app (`am force-stop`) e reabrir → **oferta** (não a apresentação).
6. "Continuar grátis" → Home (3 toques no caminho feliz). "Voltar" na Home → app fecha.
7. Repetir do zero e, **imediatamente** após "Continuar grátis", `adb shell am force-stop
   com.lsp.callguard` → reabrir → **Home** (a conclusão foi gravada antes da navegação).
8. Na oferta do primeiro uso, iniciar uma compra e cancelá-la → continua na oferta. (Requer o
   produto configurado no Play Console; com o ID provisório o botão de assinar não abre a compra
   — nesse caso registrar como "não aplicável" e confirmar que tocar em "Assinar" não conclui o
   primeiro uso.)

## 3. Aberturas seguintes (US1, SC-001)

- 10× `am force-stop` + abrir → Home em espanhol todas as vezes; repetir após `adb reboot`.

## 4. Idioma pelas Configurações (US3, SC-004)

1. Home → Configurações → Idioma: "Español" marcado.
2. Tocar "English" → tela em inglês na hora, "English" marcado.
3. "Concluir" (ou voltar) → Configurações em inglês. Reabrir app → Home em inglês.

## 5. Oferta a partir do app (US4)

- Whitelist → "Saiba mais sobre o Premium" → "continuar grátis" → volta à Whitelist.
- Home → "Ver Premium" → "continuar grátis" → Home; "voltar" fecha o app de primeira.

## 6. Atualização a partir da versão anterior (FR-010, SC-005)

1. Instalar a versão anterior, escolher idioma e/ou cadastrar um número.
2. `adb install -r` desta versão, abrir → **Home** direto.
3. Instalação anterior sem nenhum dado (recém-instalada, nunca aberta) → após atualizar, abre na
   apresentação.

## 7. Idioma do aparelho não suportado (FR-005)

- `adb uninstall`; sistema em francês; instalar e abrir → apresentação em inglês com "English"
  marcado.
- `adb uninstall`; sistema em **português (Portugal)**; instalar e abrir → registrar em que idioma
  a apresentação aparece e qual cartão está marcado. Os dois MUST coincidir (FR-005); se a tela
  vier em inglês com "Português" marcado, aplicar o ajuste descrito na T016.

Registrar resultados em `specs/002-app-launch-flow/checklists/manual-validation.md`.
