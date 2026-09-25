# Checklist de Revisão de Código - Minha Despensa KMP

Utilize este checklist para avaliar cada alteração, commit ou Pull Request no repositório.

---

## 1. Separação de Responsabilidades e Arquitetura Modular
- [ ] **Módulo Correto:** O código foi adicionado no módulo apropriado (`:core`, `:data`, `:uisystem`, `:composeApp`)?
- [ ] **Isolamento do `:core`:** O módulo `:core` permanece livre de dependências de Android, Compose ou `:data`?
- [ ] **Injeção de Dependências:** Novas dependências estão registradas no Koin nos módulos corretos (`coreModule`, `dataModule`, `uiSystemModule`)?
- [ ] **Interfaces vs Implementações:** A camada de apresentação e os use cases comunicam-se com repositórios exclusivamente através de interfaces definidas em `:core`?

---

## 2. Camada de Apresentação (MVVM & Compose Multiplatform)
- [ ] **Gerenciamento de Estado:** O ViewModel expõe o estado como `StateFlow<T>` imutável, utilizando `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ...)`?
- [ ] **Imutabilidade:** `UiState` e `UiModel` são declarados como `data class` com propriedades `val` imutáveis?
- [ ] **Subfluxos e Modais:** Navegações em subfluxos, modais e sheets usam `sealed interface` ou `sealed class` tipadas?
- [ ] **Stateless Composables:** Componentes visuais recebem o estado e emitem eventos via callbacks lambdas, sem reter estado interno indevido?
- [ ] **Recursos de Texto:** Strings de interface vêm de `Res.string.*` (Compose Resources). Não há texto hardcoded no código de UI?
- [ ] **Design System:** Uso adequado dos componentes core (`ItemContainerGlassCard`, `MinhaDespensaButton`, `MinhaDespensaTopBar`) e estilos do `MinhaDespensaTheme`?

---

## 3. Persistência e Padrões Offline-First (CRDT LWW)
- [ ] **Soft-Delete:** O código utiliza `isDeleted = 1` em vez de exclusão física (`DELETE`) em entidades sincronizáveis?
- [ ] **Timestamps:** Toda mutação atualiza `updatedAt` com timestamp UTC em milissegundos?
- [ ] **Conflitos de Concorrência:** DAOs respeitam o timestamp mais recente (`WHERE updatedAt <= :updatedAt` ou LWW)?
- [ ] **Atomicidade:** Operações que alteram múltiplas tabelas (ex: `finalizePurchase`) são executadas dentro de uma transação Room?

---

## 4. Clean Code e Idiomatismo Kotlin
- [ ] **Nomenclatura:** Classes, métodos e variáveis têm nomes descritivos em inglês, autoexplicativos e sem abreviações obscuras?
- [ ] **Preservação de Comentários e Licenças:** Comentários existentes e cabeçalhos de copyright foram preservados?
- [ ] **Tratamento de Exceções:** Falhas são tratadas de forma controlada (`runCatching`, `Result<T>`) e notificadas via `AppNotificationManager` / logger, sem engolir erros silenciosamente ou crashar?
- [ ] **Arquivos Desnecessários:** Nenhum arquivo de contexto de sessão (`SESSION_CONTEXT_EXPORT.md`), arquivo temporário ou binário de build foi incluído no commit?

---

## 5. Testes Automatizados e Validação
- [ ] **Novos Testes:** Foram adicionados ou atualizados testes unitários para a nova funcionalidade ou correção?
- [ ] **Casos de Borda:** Foram cobertos cenários positivos, casos limites (coleção vazia, valores negativos, nulos) e casos de erro?
- [ ] **Execução com Sucesso:** O comando `./gradlew testDebugUnitTest` compila e passa 100% de todos os testes sem falhas?

---

## 6. Estrutura do Arquivo de Relatório (`reports/code-reviews/REVIEW_<branch_ou_pr>.md`)

O arquivo gerado em disco deve seguir rigorosamente a estrutura abaixo:

```markdown
# 📑 Relatório de Revisão de Código

- **Branch / PR:** `<branch-name>`
- **Data da Revisão:** `<YYYY-MM-DD HH:mm>`
- **Status da Validação:** `[APROVADO | APROVADO COM RESSALVAS | REJEITADO (ALTERAÇÕES NECESSÁRIAS)]`

---

## 1. 📋 Resumo das Alterações
[Visão geral objetiva do que o PR/commit altera e qual o objetivo funcional]

---

## 2. ✅ O Que Deu Certo (Pontos Fortes)
- **[Área/Componente]:** [Descrição detalhada do que foi bem implementado, boas práticas, Clean Code e isolamento arquitetural]
- **[Testes]:** [Eficácia dos testes unitários criados]

---

## 3. ⚠️ Bloqueadores e Riscos (Must Fix)
*(Caso não haja nenhum bloqueador, registrar: "Nenhum bloqueador identificado.")*
- **[Arquivo/Linha]:** [Descrição clara do risco ou bug (concorrência, CRDT, quebra de contratos)]
  - **Impacto:** [Por que isso é um problema]

---

## 4. 💡 Sugestões e Oportunidades de Melhoria (Nitpicks / Best Practices)
- **[Sugestão 1]:** [Oportunidade de desacoplamento, micro-otimização ou refinamento de código]
- **[Sugestão 2]**

---

## 5. 🛠️ Plano de Ação e Guia de Correção (Como Corrigir)
Para cada ponto apontado nas seções 3 e 4, forneça o guia prático com snippets de código para facilitar a correção imediata:

### Item 1: [Nome da Correção]
- **Arquivo:** `[caminho/do/arquivo.kt](file:///caminho/do/arquivo.kt)`
- **Como está atualmente:**
```kotlin
// Código atual problemático
```
- **Como deve ficar (Correção recomendada):**
```kotlin
// Código corrigido e limpo
```
- **Por que esta alteração resolve:** [Explicação concisa da solução]

---

## 6. 🏁 Veredito Final
- **Veredito:** `[APROVADO | APROVADO COM RESSALVAS | REJEITADO (ALTERAÇÕES NECESSÁRIAS)]`
- **Justificativa:** [Resumo de 1 a 2 frases da decisão]
```
