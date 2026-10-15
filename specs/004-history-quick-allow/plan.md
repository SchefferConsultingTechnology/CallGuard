# Implementation Plan: Liberar Número a Partir do Histórico

**Branch**: `004-history-quick-allow` | **Date**: 2026-10-15 | **Spec**: [spec.md](spec.md)

## Summary

Cada chamada bloqueada do histórico cujo número possa ser identificado ganha "Adicionar à
whitelist" (confirmação com nome opcional e "Desfazer"); itens já na whitelist mostram o selo.
A decisão de mostrar a ação é uma função pura sobre o número identificado (regra da 001); o limite do
plano (003) leva à oferta e a inclusão é concluída depois do upgrade.

## Technical Context

**Language/Version**: Kotlin 2.3.20 · **Primary Dependencies**: Compose/Material 3, Room, DataStore
(nenhuma nova) · **Storage**: Room v2 inalterado (nova query de exclusão por número) ·
**Testing**: JUnit 4 + coroutines-test; emulador com `adb emu gsm call` · **Target Platform**:
Android minSdk 26 / target 36 · **Project Type**: mobile-app (`:app`) · **Constraints**: mesma
identidade de número da triagem; limite do plano respeitado · **Scale/Scope**: ~6 arquivos de
produção, 2 de teste, strings nos 3 idiomas

## Constitution Check

| Princípio | Avaliação |
|---|---|
| I. Fail-Open | ✅ Só adiciona permissões; nada passa a ser bloqueado. |
| II. Privacidade | ✅ Tudo local; nenhum dado novo. |
| III. Identidade de números | ✅ Inclusão pelo número canônico; registros antigos reinterpretados pela regra única. |
| IV. Puro e testado | ✅ `quickAllowAction` e ViewModel testados em JVM; schema inalterado. |
| V. Localização | ✅ Textos nos 3 idiomas. |
| VI. Monetização honesta | ✅ Limite do Grátis explicado com acesso à oferta dispensável. |

**Resultado**: PASS.

## Project Structure

```text
app/src/main/java/com/lsp/callguard/
├── data/local/dao/AllowedNumberDao.kt                 # + deleteByPhone
├── data/repository/WhitelistRepository.kt             # + deleteByPhone
├── ui/screen/history/HistoryQuickAllow.kt             # NOVO: QuickAllowAction + quickAllowAction()
├── ui/screen/history/CallHistoryViewModel.kt          # ações, inclusão, desfazer, limite, pendência
├── ui/screen/history/CallHistoryViewModelFactory.kt   # injeta whitelist, licença, normalizador
├── ui/screen/history/{CallHistoryRoute,CallHistoryScreen}.kt  # botão, selo, diálogos, snackbar
└── ui/navigation/CallGuardNavHost.kt                  # histórico → oferta
app/src/test/java/com/lsp/callguard/
├── ui/screen/history/HistoryQuickAllowTest.kt
├── ui/screen/history/CallHistoryViewModelTest.kt
└── fakes/FakeCallDecisionLogDao.kt
```

## Complexity Tracking

Sem violações.
