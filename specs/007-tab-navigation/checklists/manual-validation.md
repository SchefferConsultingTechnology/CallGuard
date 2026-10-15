# Manual Validation: Navegação por Abas

**Feature**: [spec.md](../spec.md) · Roteiro: [quickstart.md](../quickstart.md)

## Ambiente

- **Data**: 2026-10-15
- **Dispositivo**: emulador `Pixel_7_Pro`, Android API 37, pt-BR, tema escuro (escolha da 006)
- **Espaço**: o emulador estava com 5% livre e recusava instalar. Foram removidas as atualizações de
  YouTube, YouTube Music, Maps, Docs, Fotos e Gemini (`pm uninstall-system-updates`), o que deixou 1 GB
  livre. Discador, contatos, Play Services e Play Store não foram tocados.

## US1 — Abas

- [x] **Testes JVM**: `AppTabTest` (3) e `CallLogDisplayTest` (4) verdes. `CallHistoryViewModelTest`
  (4) segue verde após a extração de `displayPhoneOf`.
- [x] **Barra**: 4 abas (Início, Whitelist, Histórico, Ajustes) com a aba atual marcada (ícone
  preenchido e indicador). Whitelist e Histórico sem seta de voltar. "Adicionar número" e "Limpar
  histórico" aparecem acima da barra.
- [x] **Estado por aba**: Ajustes rolado até "Contatos" → Whitelist → Ajustes: mesma posição.
- [x] **Reseleção**: Início rolado ("Plano: Grátis" no topo) → toque em Início → volta a "CallGuard".
- [x] **Voltar**: do Histórico vai ao Início; no Início, o app fecha e o foco vai para o launcher.
- [x] **Sem animação lenta**: gravação a 60 qps da troca Início → Histórico. O título muda num único
  quadro (sem fade) e o indicador da aba anima cerca de 84 ms depois. O instante exato do toque não foi
  isolado na gravação.

## US2 — Início numa tela

- [x] **Sem rolagem**: no Pixel 7 Pro e em 6,1" simulado (`wm size 1080x2400`, `wm density 420`), com
  proteção ativa, Grátis, 1 número e 3 chamadas. Cabe com folga em pt-BR, en e es. O horário segue o
  idioma ("17:12" / "5:12 PM").
- [x] **Atalhos**:
  - card Whitelist → aba Whitelist (marcada);
  - "Ver tudo" → aba Histórico;
  - card Plano → oferta, e voltar → Início.
- [x] **Aviso de mudança de plano**: trocado Plus → Grátis em Ajustes, o toque em Início mostra o aviso
  no topo (U2). Com o aviso, o painel rola, como a spec aceita. "Entendi" o remove.
- [x] **Estado vazio**: após "Limpar histórico", aparece "Nenhuma chamada analisada ainda."
- [x] **Itens removidos**: contatos, "sem anúncios", engrenagem e botões "Gerenciar whitelist"/"Ver
  histórico" não aparecem mais no Início.
- Textos curtos acrescentados para os cards compactos (5 chaves, nos 3 idiomas), registrados em
  `contracts/home-dashboard.md`.

## US3 — Ajustes por categoria

- [x] **Testes JVM**: `SettingsSummariesTest` (6) verde. Suíte total: 165 testes, 0 falhas.
- [x] **Lista sem rolar** em 6,1" simulado, nos 3 idiomas: 6 categorias com resumo + versão.
- [x] **Resumos atualizam ao voltar**:
  - desligar "privados" → "Desconhecidos bloqueados", e religar → "Desconhecidos e privados bloqueados";
  - tema → "Escuro · Português".
- [x] **Páginas**: título, seta de voltar e barra de abas visível. Trocar o tema dentro de Aparência
  recria a tela mantendo a página.
- [x] **Estado da aba**: Aparência → Início → Ajustes volta à página Aparência; tocar de novo em Ajustes
  volta à lista.
- [x] **"Sem anúncios" uma vez**: só em Privacidade e termos. O card "App" (idioma + sem anúncios) foi
  desfeito.
- **Achado e corrigido**: o resumo de idioma lia a preferência salva e mostrava "Português" com o app
  em inglês, depois de trocar o idioma por fora (`cmd locale`, equivalente às configurações do sistema
  no Android 13+). Agora lê o idioma aplicado (`LocaleManagerHelper.currentLanguage()`): "Dark ·
  English", "Oscuro · Español", "Escuro · Português".
- **Achado e corrigido (constituição II)**: o switch de contatos das Configurações antigas ligava o uso
  de contatos direto, sem consentimento nem permissão (só o card do Início fazia o fluxo certo). Em
  Ajustes → Contatos, ligar agora abre o consentimento; desligar continua imediato.

## US4 — Telas de tarefa voltam à origem

| Partida | Ação | Resultado |
|---|---|---|
| Whitelist 5/5 (Grátis) | incluir o 6º → "Limite gratuito atingido" → Ver planos → voltar | Whitelist, barra visível ✅ |
| Histórico, chamada bloqueada de (650) 555-0120 | Adicionar à whitelist → limite → Ver planos → voltar → Ajustes → Plano → Plus (debug) → aba Histórico | Histórico mostra "Na sua whitelist"; número na Whitelist (004) ✅ |
| Ajustes → Contatos (Plus) | ligar → consentimento (sem barra) → aceitar termos e política → permissão do sistema → importação "3 números válidos" | volta a Ajustes → Contatos, switch ligado; resumo "Ativado" ✅ |
| Ajustes → Privacidade e termos | Termos de uso (sem barra) → voltar | Privacidade e termos ✅ |
| Ajustes → Aparência e idioma | Idioma (sem barra) → English → Done | "Appearance and language", já em inglês; e o caminho inverso volta a "Aparência e idioma" ✅ |
| Início | botão de proteção → "Ativar proteção" (sem barra) → voltar | Início ✅ |

## Bordas (§6)

- [x] **Girar a tela** em Ajustes → Cópia de segurança: a página continua aberta, em retrato e em
  paisagem.
- [x] **Fonte 200%**: só a aba atual mostra o nome; as outras ficam só com ícone e têm descrição para
  leitor de tela. Nenhum rótulo cortado.
- [x] **Temas**: a barra e as páginas seguem as cores da 006 nos dois temas (capturas da US1 e da US3).
- [x] **Primeiro uso** (`pm clear`): apresentação e oferta sem barra; ao concluir, Início com barra;
  voltar fecha o app (pilha do primeiro uso descartada).

## Polish

- [x] **Limpeza**: 24 recursos de texto órfãos removidos dos 3 idiomas (seções e cards antigos). Imports
  sem uso removidos dos arquivos alterados. Restam sem uso só `brand_*` e `brand_tagline`, reservados
  para a logo (006).
- [x] **Gates**: `testDebugUnitTest` 165 testes, 0 falhas; `lintDebug` 0 erros (40 avisos, antes 41);
  `assembleRelease` OK; `app/schemas/` inalterado.

## Não regressão (T032)

- [x] **001**:
  - número da whitelist fora da agenda (650) 555-0130 → Permitido;
  - desconhecido (650) 555-0199 → Bloqueado (`BLOCKED_NOT_IN_WHITELIST`);
  - os dois aparecem no Início.
  - Observação: (650) 555-0101 é contato do emulador ("Ana"). Sem a permissão de contatos (após
    `pm clear`), o Android não consulta o app para contatos. É o comportamento já documentado, não uma
    regressão.
- [x] **003**: oferta pelo card de plano do Início e por Ajustes → Plano; "Continuar com versão
  gratuita" e voltar retornam à origem.
- [x] **004**: liberar pelo Histórico com limite → oferta → Plus → inclusão concluída (US4).
- [x] **005**:
  - Ajustes → Cópia de segurança → Exportar (senha) → seletor → SAVE: "Cópia salva.";
  - Importar o arquivo com a senha: "Importação concluída…".
- [x] **006**:
  - tema por Ajustes → Aparência e idioma, mantendo a página;
  - backup exportado com Escuro e importado com Claro → volta ao Escuro (fecha a pendência §6 da 006).
- **Achado e corrigido**: aplicar o idioma ou o tema importado recriava a tela na hora e apagava o aviso
  "Importação concluída" (já acontecia na 006). Agora o `SettingsViewModel` aplica os dois 2,5 s depois
  do aviso. Medido: aviso visível a partir de 1,3 s com o tema ainda claro; escuro em seguida. Por estar
  no ViewModel, sair da página não cancela a aplicação.
