# 🎨 Módulo :uisystem (Interface & Design System)

O módulo `:uisystem` é a camada de apresentação em **Compose Multiplatform**, compartilhada entre Android e iOS. Ele adota uma arquitetura estritamente **Bottom-Up** (Atomics -> Molecules -> Feature Screens).

---

## 💎 Design System Proprietário (Glassmorphism)

O aplicativo utiliza uma identidade visual baseada em profundidade e vidro fosco (Glassmorphism):
* **`PrimaryContainerGlassCard`**: Container principal estrutural da tela com borda sutil e gradiente translúcido.
* **`SecondaryContainerGlassCard`**: Cards internos para itens de lista e blocos de conteúdo.
* **Tipografia Estruturada (`AppTypography`)**: `displayLarge`, `displayMedium`, `bodyLarge`, `bodySmall`, `priceLabel`.
* **`UiText`**: Padrão que elimina strings hardcoded nas ViewModels, permitindo mensagens dinâmicas ou recursos Compose localizados.

---

## 📱 Telas & Recursos Principais

### 1. Início (`HomeScreen`)
* Dashboard financeiro com totais gastos no mês.
* Widget de itens próximos do vencimento (`ExpiringSoonWidget`).
* Tendências de consumo recentes (`ConsumptionTrendWidget`).

### 2. Catálogo & Ficha Técnica (`CatalogScreen` & `ProductDetailsScreen`)
* Listagem de produtos agrupada por categorias ou busca textual.
* Sheet unificada de cadastro (`RegisterProductBottomSheet`) com validação reativa de formulário.
* Tela de detalhes completa com gráfico interativo de histórico de preços ao longo do tempo e lotes ativos na despensa.

### 3. Despensa (`PantryScreen`)
* Visão de estoque por categorias com contadores em tempo real.
* Filtro instantâneo de produtos próximos do vencimento (limiar configurável).
* Baixa rápida de unidades e fluxo de consumo em lote.

### 4. Assistente de Compras (`ShoppingAssistantScreen`)
* Painel financeiro em tempo real no topo da tela com cálculo de subtotal por item.
* Barra de progresso orçamentário (`BudgetGauge`).
* Scanner de código de barras nativo acoplado via CameraX + Google ML Kit no Android.
* Fluxo de substituição de produtos em loja via `ReplaceItemOptionsSheet`.

---

## ⚙️ Máquinas de Estados (SubFlows)

Para evitar condições de corrida na interface (ex: dois modais abrindo simultaneamente), fluxos complexos usam máquinas de estado baseadas em sealed classes (`PantrySubFlow`, `ProductDetailsSubFlow`).

---

## 🧪 Testes no Módulo (:uisystem)

Contém **94 testes unitários** usando Turbine para assertions de `StateFlow` e validação isolada de regras de UI, formatadores de moeda e validadores de formulário.
