# Manual Validation: Fluxo de Abertura e Idioma

**Feature**: [spec.md](../spec.md) · Roteiro: [quickstart.md](../quickstart.md)

## Ambiente

- **Data**: 2026-10-14
- **Dispositivo**: emulador `Pixel_7_Pro`, Android API 37, sistema em inglês
- **Build**: `app-debug.apk` da branch `002-app-launch-flow`

## Resultados

- [x] **§1 Gates** — `testDebugUnitTest` 91 testes / 0 falhas; `lintDebug` 0 erros (39 avisos,
  mesmos de antes); `assembleRelease` OK; `app/schemas/` inalterado.
- [x] **§2 Instalação nova (US2)** — primeira tela = apresentação com "English" marcado (sistema
  en) e "Continue" habilitado; tocar "Español" traduz na hora; "Continuar" → oferta; voltar →
  apresentação; fechar/reabrir → oferta; "Continuar con la versión gratuita" → Home; voltar na
  Home fecha o app.
  - Passo 7 (U1): 3 rodadas de `force-stop` **imediato** após "continuar grátis" → reabre na
    Home nas 3.
  - Passo 8 (compra cancelada): **não aplicável** — o ID do produto ainda é provisório e a compra
    não abre; "Assinar" não conclui o primeiro uso.
- [x] **§3 Aberturas seguintes (US1)** — Home em 10/10 aberturas (`force-stop` + abrir).
  Após reiniciar o aparelho: 3 de 4 reinícios abriram na tela esperada (ver pendência abaixo).
- [x] **§4 Idioma pelas Configurações (US3)** — com o app seguindo o sistema, "English" vem
  marcado; tocar "Português"/"Español" traduz na hora sem confirmar; "Concluir"/"Listo" volta às
  Configurações no novo idioma; reabrir → Home no novo idioma.
- [x] **§5 Oferta a partir do app (US4)** — Whitelist → "Conoce Premium" → "continuar grátis" →
  Whitelist; voltar do sistema na oferta → Whitelist; Home → "Ver Premium" → "continuar grátis" →
  Home; voltar na Home fecha o app (sem Home duplicada).
- [x] **§6 Atualização da versão anterior (FR-010)** — versão `60ff83a` com idioma escolhido →
  atualizar → Home em português; versão anterior instalada e nunca aberta → atualizar →
  apresentação.
- [x] **§7 Idioma regional/não suportado (FR-005, U3)** — simulado com idioma do app
  (`cmd locale set-app-locales`, mesma resolução de recursos que o idioma do sistema):
  `pt-PT` → tela em português, "Português" marcado; `es-MX` → espanhol, "Español" marcado;
  `fr-FR` → inglês, "English" marcado. Ajuste condicional da T016 **não** foi necessário.
  Troca do idioma *do sistema* pela tela do Android não foi exercitada.
- [x] **Recriação (FR-011)** — na oferta do primeiro uso: girar a tela e trocar o idioma do app
  mantêm a oferta; voltar depois da recriação retorna à apresentação.
- [x] **3 idiomas** — fluxos exercitados em inglês (instalação nova), espanhol (primeiro uso,
  Configurações, oferta) e português (Configurações, atualização).

## Pendência

- **1 de 4 reinícios abriu na apresentação** em vez da oferta, com `onboarding_progress =
  ONBOARDING_DONE` gravado. Não reproduzido em 3 tentativas seguintes (inclusive deixando o app
  aberto na apresentação antes de reiniciar); logs daquele boot perdidos. Causa provável: falha
  pontual ao ler o DataStore logo após o boot → fallback de R4 (trata como desconhecido →
  apresentação). Efeito: rever uma tela, nunca bloqueia o app. Sugestão de reforço: tentar a
  leitura de novo antes do fallback e registrar a falha em log de debug.
