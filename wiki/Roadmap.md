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

### 2. Notificações Nativas de Validade (`v1.1.0`)
- [ ] Notificações push locais avisando sobre itens que estão a X dias de vencer.
- [ ] Configuração personalizada do limiar de antecedência de alerta por categoria.

### 3. Inteligência de Consumo & Reposição (`v1.2.0`)
- [ ] Cálculo da velocidade de consumo médio da despensa da família.
- [ ] Sugestão automática de itens para a próxima lista de compras quando o estoque atingir níveis baixos.
- [ ] Detecção de variações de preço para alertar se um produto está mais caro do que a média habitual.

### 4. Receitas & Aproveitamento (`v1.3.0`)
- [ ] Sugestão de receitas com base nos produtos disponíveis na despensa para evitar desperdício de itens próximos ao vencimento.
- [ ] Adição automática dos ingredientes faltantes de uma receita diretamente na lista de compras.

### 5. Sincronização em Nuvem (Cloud Sync & Multi-dispositivo)
- [ ] Sincronização entre membros da mesma família através de arquitetura CRDT / LWW já estruturada nas entidades.
