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

---

## ⚡ Casos de Uso (Use Cases)

A camada de domínio orquestra as regras da aplicação em 11 casos de uso atômicos:

1. **`SaveCatalogProductUseCase`**: Validação de limites de caracteres (30 chars), formato numérico de EAN (8, 12, 13, 14 dígitos) e salvamento com foto.
2. **`CheckEanStatusUseCase`**: Verificação instantânea de unicidade e existência de código de barras.
3. **`AddPantryItemUseCase`**: Validação de quantidades positivas e persistência de estoque.
4. **`CreatePlannedShoppingListUseCase`**: Construção de listas planejadas com teto de gastos.
5. **`CreateQuickShoppingListUseCase`**: Parser de notas de texto em linhas para itens de rascunho.
6. **`AddCatalogItemToShoppingListUseCase`**: Inserção de itens do catálogo na lista.
7. **`AddOrUpdateCartItemUseCase`**: Ajuste em tempo real de preço, quantidade e marcação no carrinho.
8. **`ReplaceCartItemUseCase`**: Substituição de produtos em loja durante a compra.
9. **`RemoveCartItemUseCase`**: Exclusão lógica de itens da lista ativa.
10. **`StartShoppingSessionUseCase`**: Abertura ou transição da lista para o modo `SHOPPING`.
11. **`FinalizeShoppingSessionUseCase`**: Checkout atômico coordenando despensa, preços e checklist.

---

## 🧪 Testes no Módulo (:core)

Possui **52 testes unitários** executados via `./gradlew :core:test`, cobrindo tolerância a desvios de relógio (`clock-drift`), serialização JSON e todas as validações de limites e erros.
