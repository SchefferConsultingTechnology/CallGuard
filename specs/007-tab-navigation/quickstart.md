# Quickstart: validar Navegação por Abas

**Feature**: [spec.md](spec.md) · **Contratos**: [contracts/](contracts/)

## Pré-requisitos

- JDK 21 do sistema; emulador `Pixel_7_Pro` (6,7", API 37).
- Tela de 6,1" (SC-003): simular o Pixel 7 no mesmo emulador com `adb shell wm size 1080x2400` e
  `adb shell wm density 420`. Desfazer com `wm size reset` e `wm density reset`. Se possível, conferir
  também no Samsung de teste.
- Papel de triagem concedido (`cmd role add-role-holder android.app.role.CALL_SCREENING com.lsp.callguard 0`).
- Dados: 3 números na whitelist, ≥ 4 chamadas no histórico (`adb emu gsm call …`).

## §1 Gates

`./gradlew … :app:testDebugUnitTest :app:lintDebug :app:assembleRelease` verde. O resultado inclui
`AppTabTest`, `SettingsSummariesTest` e `CallLogDisplayTest`. `app/schemas/` fica inalterado.

## §2 Abas (US1)

1. Abrir o app: deve aparecer o Início com a barra e "Início" marcado.
   A troca de aba é imediata, sem fade lento.
2. Tocar em cada aba: a tela certa aparece e Whitelist/Histórico não têm seta.
3. Rolar o Histórico até o fim → Whitelist → Histórico: a posição deve se manter.
4. Ajustes → Contatos → aba Início → aba Ajustes: deve voltar à página Contatos. Tocar em Ajustes de
   novo: volta à lista.
5. Voltar do sistema em Whitelist deve levar ao Início; voltar no Início deve fechar o app.
6. Tocar de novo na aba Histórico com a lista rolada: deve voltar ao topo.

## §3 Início (US2)

1. Com proteção ativa, Grátis, 3 números, chamadas e sem aviso de mudança de plano: tudo cabe sem rolar (6,1", fonte padrão, pt-BR,
   en, es).
2. Card whitelist → aba Whitelist. Card plano → oferta. Linha de chamada / "Ver tudo" → aba Histórico.
3. Limpar histórico → o Início deve mostrar "Nenhuma chamada analisada ainda."
4. Plus → Grátis (seletor debug): o aviso deve aparecer no topo, e "Entendi" o remove.

## §4 Ajustes (US3)

1. Lista com 6 categorias + versão, sem rolar.
2. Em cada categoria, alterar algo e voltar: o resumo deve mudar (tema Escuro → "Escuro · Português";
   desligar privados → "Desconhecidos bloqueados"; plano Plus → "Plus").
3. "Sem anúncios" deve aparecer uma única vez (Privacidade e termos).

## §5 Tarefas (US4)

| Partida | Ação | Esperado ao concluir/cancelar |
|---|---|---|
| Whitelist com 5 números (Grátis) | Adicionar → limite → Ver planos → voltar | Whitelist |
| Histórico, Grátis cheio | Adicionar à whitelist → limite → oferta → Plus (debug) → voltar | Histórico; número incluído (004) |
| Ajustes → Contatos (Plus) | ligar → consentimento → aceitar → importação | Ajustes → Contatos, ligado |
| Ajustes → Privacidade | Termos → voltar | Ajustes → Privacidade |
| Ajustes → Aparência e idioma | Idioma → English → Concluir | Ajustes → Appearance and language, em inglês |
| Início (proteção não configurada) | Ativar → voltar | Início |

Em todas as telas de tarefa a barra não deve aparecer.

## §6 Bordas

- Girar a tela e trocar o tema numa página de Ajustes: a página deve continuar aberta.
- Fonte 200% (`settings put system font_scale 2.0`): os rótulos das abas devem ficar legíveis (R9), e o
  botão "Adicionar número" e as snackbars devem aparecer acima da barra.
- Tema claro e escuro: a barra deve usar as cores da 006, sem área atrás do gesto de navegação.
- Primeiro uso (`pm clear`): sem barra até concluir a oferta; depois o Início com a barra.

## §7 Não regressão

- Bloqueio e liberação de chamadas inalterados (`adb emu gsm call` de desconhecido e de número da
  whitelist).
- Roteiros das features 004 (liberar pelo histórico, desfazer) e 005 (exportar/importar) executáveis
  pelos novos caminhos.
