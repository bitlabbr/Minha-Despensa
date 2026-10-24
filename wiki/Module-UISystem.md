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

### 1. Início (`HomeScreen` & `HomeViewModel`)
* Dashboard financeiro reativo (`FinancialWidget`) com gastos reais do mês e barra de consumo orçamentário (`InverseAnchoredGauge`).
* Card de estoque avaliado (`StockValueCard`) com valor patrimonial total da despensa em reais (`R$ X,XX`), quantidade avaliada e custo médio unitário.
* Diálogo de analytics financeiro (`FinancialAnalyticsDialog`) com evolução anual de 12 meses, gasto médio mensal e custo médio ponderado por item.
* Diálogo de definição de orçamento (`SetMonthlyBudgetDialog`) para definir ou atualizar a meta orçamentária familiar do mês, com máscara dinâmica de moeda e separador de milhares (`R$ 1.500,00`).
* Widget de itens próximos do vencimento (`ExpiringSoonWidget`) com badges dinâmicos de urgência e dias restantes.
* Tendências de consumo recentes (`ConsumptionTrendWidget`) agregadas por mês.

### 2. Catálogo & Ficha Técnica (`CatalogScreen` & `ProductDetailsScreen`)
* Listagem de produtos agrupada por categorias ou busca textual.
* Sheet unificada de cadastro (`RegisterProductBottomSheet`) com validação reativa de formulário.
* Tela de detalhes completa com gráfico interativo de histórico de preços ao longo do tempo, lotes ativos com valor unitário e edição em lote (`EditPantryBatchSheet`) com recálculo instantâneo das métricas mín/máx/médio.

### 3. Despensa (`PantryScreen`)
* Visão de estoque por categorias com contadores em tempo real.
* Filtro instantâneo de produtos próximos do vencimento (limiar configurável).
* Baixa rápida de unidades e fluxo de consumo em lote.

### 4. Assistente de Compras (`ShoppingAssistantScreen`)
* Painel financeiro em tempo real no topo da tela com cálculo de subtotal por item.
* Barra de progresso orçamentário (`BudgetGauge`).
* Scanner de código de barras nativo acoplado via CameraX + Google ML Kit no Android.
* Fluxo de substituição de produtos em loja via `ReplaceItemOptionsSheet`.

### 5. Conta & Despensas Compartilhadas (`HouseholdAccountScreen` & `HouseholdAccountViewModel`)
* Autenticação completa de usuário com abas dinâmicas de Login e Registro.
* Gestão de despensa ativa, listagem de outras despensas da família e criação rápida.
* Código de convite amigável (`CASA-XXXXXXXX`) com cópia em um toque e feedback háptico.
* Indicador de sincronização em tempo real (`SyncState`) e acionamento manual de sincronização.
* Seção de Atualização de Software (`AppUpdateSection`): exclusiva para Android, exibe a versão instalada, consulta a versão mais recente no servidor Domus, exibe changelog, barra de progresso linear de download do APK e dispara o instalador nativo. No iOS, a seção é ocultada automaticamente.

---

## ⚙️ Máquinas de Estados (SubFlows)

Para evitar condições de corrida na interface (ex: dois modais abrindo simultaneamente), fluxos complexos usam máquinas de estado baseadas em sealed classes (`PantrySubFlow`, `ProductDetailsSubFlow`).

---

## 🧪 Testes no Módulo (:uisystem)

Contém **184 testes unitários** usando Turbine para assertions de `StateFlow` e validação isolada de regras de UI, `HouseholdAccountViewModel`, `HomeViewModel`, formatadores de moeda, mappers de mês internacionalizados (`MonthMapper`), validadores de formulário e estabilizadores de scanner.
