# 🗺️ Roadmap & Futuro do Minha Despensa

O **Minha Despensa** está sendo desenvolvido de forma iterativa. O objetivo da versão `1.0.0-alpha` foi validar a persistência Local-First e o fluxo transacional completo. As próximas versões trarão camadas adicionais de inteligência.

---

## 📅 Próximas Fases

### 1. Estabilização Alpha (`v1.0.0-alpha.x`)
- [ ] Correção dos 6 bugs identificados na issue mãe (normalização de acentos, remoção de fotos, debounce do scanner, etc.).
- [ ] Otimização do leitor de código de barras para foco e região central.
- [ ] Opção de busca rápida dentro de listas de compras.

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
