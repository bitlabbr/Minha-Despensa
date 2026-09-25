---
name: minha-despensa-code-reviewer
description: >-
  Use this skill when reviewing code changes, commits, or pull requests for the Minha Despensa KMP project.
  Guides the agent to analyze architecture, Clean Code, MVVM in Compose Multiplatform, CRDT offline-first patterns,
  business rules, and test coverage according to project standards.
---

# Minha Despensa KMP - Code Reviewer Skill

Esta skill capacita o agente a atuar como **Revisor Sênior de Código e Arquiteto** do projeto **Minha Despensa KMP**. O objetivo é garantir consistência de design, integridade arquitetural (Clean Architecture), excelência em Compose Multiplatform / MVVM, conformidade com os princípios offline-first (CRDT) e 100% de cobertura de testes automatizados.

---

## 🛠️ Procedimento de Revisão Passo a Passo

### Passo 1: Inspecionar as Mudanças
Identifique as alterações a serem revisadas utilizando comandos do Git:
- **Para revisar commits contra a branch base (ex: `dev`):**
  ```bash
  git log origin/dev..HEAD --oneline
  git diff origin/dev...HEAD
  ```
- **Para revisar o working tree ou commits locais recentes:**
  ```bash
  git status
  git diff HEAD~1..HEAD
  ```
- Mapeie a lista de arquivos alterados e classifique-os por módulo (`:core`, `:data`, `:uisystem`, `:composeApp`).

---

### Passo 2: Avaliar contra os Padrões do Projeto
Consulte os documentos de referência desta skill para balizar a análise:
1. **[Arquitetura e Regras de Negócio](./references/architecture-and-rules.md):**
   - Limites modulares e isolamento do `:core`.
   - MVVM, `StateFlow`, `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ...)` no `:uisystem`.
   - Padrão CRDT LWW (`updatedAt`, `isDeleted = 1`, sem `DELETE` físico) no `:data`.
   - Regras de negócio da despensa (agregação por produto), listas de compras (orçamento, finalização atômica) e catálogo.
2. **[Checklist de Revisão](./references/review-checklist.md):**
   - Execute a verificação item a item em busca de violações, code smells, vazamentos de memória ou strings hardcoded.

---

### Passo 3: Validação Automatizada de Testes
Verifique se a suíte de testes do projeto compila e passa sem falhas:
```bash
./gradlew testDebugUnitTest
```
- Se novos métodos ou classes foram introduzidos, certifique-se de que existem testes unitários correspondentes em `commonTest`.
- Confirme que nenhum arquivo temporário de contexto de sessão (como `SESSION_CONTEXT_EXPORT.md`) foi rastreado pelo git.

---

### Passo 4: Gerar o Arquivo de Relatório em Disco
O revisor **deve salvar** um relatório detalhado em Markdown no diretório `reports/code-reviews/`:
- **Nome do arquivo:** `reports/code-reviews/REVIEW_<nome-da-branch-ou-pr>.md`
- **Conteúdo estruturado obrigatório:**
  1. **📋 Resumo das Alterações:** Visão geral do escopo da mudança.
  2. **✅ O Que Deu Certo (Pontos Fortes):** Reconhecimento explícito das boas práticas, Clean Code, decisões de arquitetura e cobertura de testes.
  3. **⚠️ Bloqueadores e Riscos (Must Fix):** Falhas críticas de arquitetura, riscos de concorrência, quebra de contratos CRDT/LWW ou ausência de testes.
  4. **💡 Sugestões e Oportunidades de Melhoria (Nitpicks / Best Practices):** Recomendações de legibilidade, UX e idiomatismo Kotlin.
  5. **🛠️ Plano de Ação e Guia de Correção (Como Corrigir):** Snippets práticos de código no formato *Como está atualmente* vs *Como deve ficar (Correção recomendada)*, permitindo correção imediata e descomplicada pelo desenvolvedor.
  6. **🏁 Veredito Final:** `APROVADO`, `APROVADO COM RESSALVAS` ou `REJEITADO (ALTERAÇÕES NECESSÁRIAS)`.

---

### Passo 5: Apresentar Resumo Executivo no Chat
Após gravar o arquivo em disco, o revisor deve exibir no chat uma mensagem executiva contendo:
- O link markdown clicável para o arquivo gerado: `[Visualizar Relatório Completo](file:///Users/willian/Projetos/Minha-Dispensa/reports/code-reviews/REVIEW_<nome-da-branch-ou-pr>.md)`.
- O Veredito final.
- Destaques rápidos dos pontos fortes e dos principais itens do plano de ação.
