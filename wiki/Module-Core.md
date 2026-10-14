# 🧠 Módulo :core (Domínio & Regras de Negócio)

O módulo `:core` é o coração das regras de negócio do **Minha Despensa**. Ele é um módulo Kotlin Multiplatform puro, **sem dependência alguma de bibliotecas de interface gráfica (Compose) ou de persistência (Room/SQLite)**.

---

## 📦 Modelos de Domínio

Todos os modelos residem em `com.bitlabbr.minhadespensa.core.domain.model` e são anotados com `@Serializable`:

*   **`CatalogProduct`**: Ficha técnica do produto (id, nome, marca, categoria, unidade de medida, peso líquido, EAN, notas).
*   **`MeasureUnit`**: Unidades físicas suportadas: `UNIT`, `KILOGRAM`, `GRAM`, `LITER`, `MILLILITER`, `PACKAGE`.
*   **`PantryItem`**: Estoque físico na despensa (quantidade, lote, data de validade).
*   **`PantryItemWithCategory`**: Modelo de leitura unificado com informações de categoria e unidade do catálogo.
*   **`PriceEntry`**: Histórico financeiro de preços pagos em lojas físicas por produto.
*   **`ShoppingList`**: Listas com tipos (`SCRATCHPAD`, `PLANNED`, `ASSISTANT`), status (`DRAFT`, `SHOPPING`, `COMPLETED`, `CANCELLED`) e métricas calculadas:
    *   `totalActiveItems`: Total de itens não deletados.
    *   `totalCheckedItems`: Itens marcados no carrinho.
    *   `totalCartInCents`: Valor financeiro acumulado no carrinho.
    *   `isOverBudget`: Indicador de extrapolação do teto orçamentário (`budgetInCents`).
*   **`ShoppingItem`**: Item da lista com cálculo de `subtotalInCents` (`priceAtTime * quantity`).
*   **`MonthlyBudget`**: Meta de orçamento financeiro da família por mês e ano (`year`, `month`, `budgetInCents`).
*   **`MonthlyFinancialSummary`**: Modelo de saúde financeira combinando orçamento, total gasto, saldo restante, progresso e status de estouro.
*   **`PantryMovement`**: Ledger imutável de movimentações de estoque e despesas (`ENTRY_MANUAL`, `ENTRY_PURCHASE`, `CONSUMPTION`, `EXPIRATION_DISCARD`, `ADJUSTMENT`).
*   **`PantryStockValue`**: Valoração patrimonial da despensa com valor total em centavos, quantidade de itens com valor e custo médio unitário.

---

## ⚡ Casos de Uso (Use Cases)

A camada de domínio orquestra as regras da aplicação em 17 casos de uso atômicos:

1. **`SaveCatalogProductUseCase`**: Validação de limites de caracteres (30 chars), formato numérico de EAN (8, 12, 13, 14 dígitos) e salvamento com foto.
2. **`CheckEanStatusUseCase`**: Verificação instantânea de unicidade e existência de código de barras.
3. **`AddPantryItemUseCase`**: Validação de quantidades positivas, preço unitário opcional e persistência de estoque.
4. **`UpdatePantryItemUseCase`**: Validação de modificações em estoque existente com resolução de conflitos Last-Write-Wins.
5. **`CreatePlannedShoppingListUseCase`**: Construção de listas planejadas com teto de gastos.
6. **`CreateQuickShoppingListUseCase`**: Parser de notas de texto em linhas para itens de rascunho.
7. **`AddCatalogItemToShoppingListUseCase`**: Inserção de itens do catálogo na lista.
8. **`AddOrUpdateCartItemUseCase`**: Ajuste em tempo real de preço, quantidade e marcação no carrinho.
9. **`ReplaceCartItemUseCase`**: Substituição de produtos em loja durante a compra.
10. **`RemoveCartItemUseCase`**: Exclusão lógica de itens da lista ativa.
11. **`StartShoppingSessionUseCase`**: Abertura ou transição da lista para o modo `SHOPPING`.
12. **`FinalizeShoppingSessionUseCase`**: Checkout atômico coordenando despensa, preços e checklist.
13. **`SetMonthlyBudgetUseCase`**: Validação e persistência do teto de orçamento mensal da família.
14. **`GetMonthlyFinancialSummaryUseCase`**: Agregação reativa de orçamento e despesas mensais em tempo real.
15. **`GetMostConsumedProductsUseCase`**: Identificação dos produtos com maior volume de consumo em um período.
16. **`GetPantryTotalValueUseCase`**: Cálculo monetário do patrimônio estocado na despensa (`PantryStockValue`).
17. **`GetYearlyFinancialSummaryUseCase`**: Agregação histórica de 12 meses de despesas versus orçamento para analytics anual.

---

## 🧪 Testes no Módulo (:core)

Possui **102 testes unitários** executados via `./gradlew :core:test`, cobrindo tolerância a desvios de relógio (`clock-drift`), serialização JSON, valoração de estoque, use cases financeiros e todas as validações de regras e limites.
