# Manual Validation: Backup (arquivo exportado)

**Feature**: [spec.md](../spec.md) · Roteiro: [quickstart.md](../quickstart.md)

- **Data**: 2026-10-15 · **Dispositivo**: emulador `Pixel_7_Pro`, API 37 · plano Grátis

## Resultados (US4)

- [x] **Gates** — 145 testes / 0 falhas (20 novos: cifra, formato, combinação e repositório
  ponta a ponta); `lintDebug` 0 erros (45 avisos; o único novo é o número fixo de caracteres da
  senha); `assembleRelease` OK; schema inalterado.
- [x] **Exportar** — 3 números + "bloquear números ocultos" desligado; senha `segredo123` (2×);
  seletor do Android com nome `callguard-2026-10-15.cgbk`; "Cópia salva."
- [x] **Arquivo** — 160 bytes, cabeçalho `CGBK` v1; nenhum número ou nome legível no conteúdo.
- [x] **Senha errada** — "Senha errada ou arquivo danificado. Nada foi alterado." (whitelist vazia).
- [x] **Importar** (após apagar os dados do app) — 3 números novos; "bloquear números ocultos"
  restaurado como desligado; ligação de `6505550103` (restaurado) → `ALLOWED_BY_WHITELIST`.
- [x] **Importar de novo** — 0 novos (sem duplicar).
- [x] **Idiomas** — seção e textos em en ("Backup", "Protected backup file") e es ("Copia de
  seguridad", "Archivo protegido con contraseña").

## Bloqueado — Google Drive (US1, US2, US3, US5)

- [ ] Requer projeto Google Cloud (consentimento OAuth, cliente Android com SHA-1, Drive API).
  Ao desbloquear: `/speckit-tasks` para as fases do Drive e `PRO_ON_SALE = true` ao publicar.

## Observação

- Em aparelhos onde o número que liga está salvo nos contatos e o CallGuard não tem permissão de
  contatos, o Android não consulta o app (achado já registrado); por isso o teste de ligação usou
  um número restaurado que não é contato.
