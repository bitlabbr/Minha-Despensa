# 🗄️ Módulo :data (Persistência & Room KMP)

O módulo `:data` é o responsável por toda a infraestrutura de dados local da aplicação, implementando as interfaces de repositório declaradas no `:core`.

---

## 🗃️ Base de Dados Room (Schema v9)

A persistência utiliza **Room Multiplatform** com o driver nativo **`BundledSQLiteDriver`**, garantindo paridade total entre Android e iOS.

### Entidades do Banco:
* `catalog_products`: Informações cadastrais de produtos com índice único no campo `ean`.
* `product_media`: Armazenamento de fotos de produtos em `BLOB` binário com chave estrangeira em cascata.
* `pantry_items`: Estoque físico com chaves estrangeiras para produtos e coluna `unit_price_in_cents`.
* `price_entries`: Histórico temporal de preços por loja.
* `shopping_lists`: Listas de compras com teto orçamentário (`budget_in_cents`).
* `shopping_items`: Itens com chave estrangeira `ON DELETE CASCADE` para a lista e `ON DELETE SET NULL` para o produto (permitindo preservar históricos mesmo se um produto for excluído do catálogo).
* `monthly_budgets`: Teto orçamentário mensal da família com chave primária composta `(year, month)`.
* `pantry_movements`: Ledger e auditoria de movimentações de estoque e despesas com índices otimizados em `(type, updatedAt)`, `(productId)` e `updatedAt`.
* `product_categories`: Categorias customizadas e padrão (`id`, `name`, `is_default`, `updated_at`, `is_deleted`).

---

## 🔁 Pipeline de Migrações SQLite

Para suportar atualizações sem perda de dados para os usuários, o banco possui um pipeline de migrações gerenciado em `AppDatabaseMigrations.kt`:

* **`MIGRATION_1_2`**: Criação da tabela `product_media`.
* **`MIGRATION_2_3`**: Adição das colunas `category` e `notes` no catálogo e criação da tabela `shopping_lists`.
* **`MIGRATION_3_4`**: Padronização para `snake_case` nas colunas (`budget_in_cents`, `list_id`, etc.), reconfiguração de constraints de chaves estrangeiras e índices únicos.
* **`MIGRATION_4_5`**: Adição da coluna `unit_price_in_cents` na tabela `pantry_items`.
* **`MIGRATION_5_6`**: Criação da tabela `monthly_budgets` com chave primária composta `(year, month)`.
* **`MIGRATION_6_7`**: Criação da tabela de auditoria e movimentações `pantry_movements`.
* **`MIGRATION_7_8`**: Criação da tabela `product_categories`, indexação por `name` e seeding de categorias padrão/customizadas.
* **`MIGRATION_8_9`**: Renomeação da coluna `timestamp` para `updatedAt` em `pantry_movements` e recriação dos índices correspondentes.
* **Migrações Diretas**: Saltos diretos para a versão 9 (`MIGRATION_1_9` até `MIGRATION_7_9`, além das migrações intermediárias).

---

## 🔒 Concorrência & Transações Atômicas

* **Last-Write-Wins (LWW):** Todas as atualizações verificam `updated_at < :updatedAt` nas queries SQL para evitar que dados defasados sobrescrevam modificações recentes.
* **Transações Imediatas:** Operações críticas (como `finalizePurchase` e `consumeBatch`) utilizam `useWriterConnection { conn -> conn.withTransaction(IMMEDIATE) { ... } }`, impedindo que leituras e escritas concorrentes corrompam o estoque.

---

## 🧪 Testes no Módulo (:data)

O módulo contém **160 testes automatizados** rodando com banco SQLite in-memory, cobrindo todas as migrações de versão (incluindo v6 e v7), operações LWW, agregação de despesas e transações atômicas.
