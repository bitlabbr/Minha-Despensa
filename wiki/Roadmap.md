# 🗺️ Roadmap & Futuro do Minha Despensa

O **Minha Despensa** está sendo desenvolvido de forma iterativa. O objetivo da versão `1.0.0-alpha` foi validar a persistência Local-First e o fluxo transacional completo. As próximas versões trarão camadas adicionais de inteligência.

---

## 📅 Próximas Fases

### 1. Estabilização Alpha (`v1.0.1-alpha`) ✅ Concluída
- [x] Correção dos 6 bugs e melhorias de UX da issue mãe (#47):
  - [x] Busca e ordenação com normalização de acentos (#48).
  - [x] Remoção física e lógica de imagens no formulário de edição de produtos (#48).
  - [x] Estabilização do leitor de código de barras: filtro de ROI central e debounce temporal (#49).
  - [x] Inclusão direta de produtos do catálogo na despensa e opções contextuais (#50).
  - [x] Alerta visual de itens duplicados e consolidação de quantidades nas listas e carrinho (#51).
  - [x] Busca textual expandível e filtro no Assistente de Compras e Listas Planejadas (#52).
  - [x] Busca e filtragem de itens na Despensa por marca do produto (#58).
- [x] Fechamento automático de modais/sheets e fluidez na navegação de entrada de estoque.

### 2. Gestão Financeira, Orçamento Mensal & Visão Geral Real (`v1.1.1-alpha`) ✅ Concluída
- [x] Suporte a preço unitário e histórico de preços na despensa (#64).
- [x] Exibição de valor unitário, edição de lotes e correção de métricas mín/máx (#65).
- [x] Orçamento mensal, agregação reativa de despesas e arquitetura de movimentações (#66).
- [x] Desmock da tela inicial (Home) e integração do HomeViewModel com dados reais (#67).
- [x] Avaliação do patrimônio em estoque e analytics financeiro anual com evolução de 12 meses (#68).
- [x] Consolidação da suíte com 412 testes unitários automatizados (100% passando).

### 3. Sincronização em Nuvem, Multi-família & Distribuição OTA (`v1.2.0-alpha`) ✅ Concluída
- [x] Autenticação de usuários via JWT com rotação de refresh token (#86).
- [x] Gestão de múltiplas despensas familiares (`Household`) com códigos de convite Crockford Base32 (`CASA-XXXXXXXX`).
- [x] Motor de sincronização bidirecional offline-first v2.1 com Outbox, serialização monotônica (`syncVersion`) e resolução de conflitos Last-Write-Wins (LWW).
- [x] Deduplicação híbrida de catálogo e migração atômica de chaves estrangeiras (`productIdMappings`).
- [x] Distribuição autônoma de novas versões in-app (Self-Hosted OTA Android) com download de APK via streaming e integração nativa ao `FileProvider`.
- [x] Tela de Gerenciamento de Conta e Despensas (`HouseholdAccountScreen`) em Glassmorphism.
- [x] Expansão da suíte para 553 testes unitários automatizados (100% passando).

### 4. Notificações Nativas de Validade (`v1.3.0`)
- [ ] Notificações push locais avisando sobre itens que estão a X dias de vencer.
- [ ] Configuração personalizada do limiar de antecedência de alerta por categoria.

### 5. Inteligência de Consumo & Reposição (`v1.4.0`)
- [ ] Cálculo da velocidade de consumo médio da despensa da família.
- [ ] Sugestão automática de itens para a próxima lista de compras quando o estoque atingir níveis baixos.
- [ ] Detecção de variações de preço para alertar se um produto está mais caro do que a média habitual.

### 6. Receitas & Aproveitamento (`v1.5.0`)
- [ ] Sugestão de receitas com base nos produtos disponíveis na despensa para evitar desperdício de itens próximos ao vencimento.
- [ ] Adição automática dos ingredientes faltantes de uma receita diretamente na lista de compras.
