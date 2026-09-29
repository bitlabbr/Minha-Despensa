# 🗄️ Módulo :data (Persistência & Room KMP)

O módulo `:data` é o responsável por toda a infraestrutura de dados local da aplicação, implementando as interfaces de repositório declaradas no `:core`.

---

## 🗃️ Base de Dados Room (Schema v4)

A persistência utiliza **Room Multiplatform** com o driver nativo **`BundledSQLiteDriver`**, garantindo paridade total entre Android e iOS.

### Entidades do Banco:
* `catalog_products`: Informações cadastrais de produtos com índice único no campo `ean`.
* `product_media`: Armazenamento de fotos de produtos em `BLOB` binário com chave estrangeira em cascata.
* `pantry_items`: Estoque físico com chaves estrangeiras para produtos.
* `price_entries`: Histórico temporal de preços por loja.
* `shopping_lists`: Listas de compras com teto orçamentário (`budget_in_cents`).
* `shopping_items`: Itens com chave estrangeira `ON DELETE CASCADE` para a lista e `ON DELETE SET NULL` para o produto (permitindo preservar históricos mesmo se um produto for excluído do catálogo).

---

## 🔁 Pipeline de Migrações SQLite

Para suportar atualizações sem perda de dados para os usuários, o banco possui um pipeline de migrações gerenciado em `AppDatabaseMigrations.kt`:

* **`MIGRATION_1_2`**: Criação da tabela `product_media`.
* **`MIGRATION_2_3`**: Adição das colunas `category` e `notes` no catálogo e criação da tabela `shopping_lists`.
* **`MIGRATION_3_4`**: Padronização para `snake_case` nas colunas (`budget_in_cents`, `list_id`, etc.), reconfiguração de constraints de chaves estrangeiras e índices únicos.
* **`MIGRATION_1_4` e `MIGRATION_2_4`**: Caminhos diretos de salto de versão.

---

## 🔒 Concorrência & Transações Atômicas

* **Last-Write-Wins (LWW):** Todas as atualizações verificam `updated_at < :updatedAt` nas queries SQL para evitar que dados defasados sobrescrevam modificações recentes.
* **Transações Imediatas:** Operações críticas (como `finalizePurchase` e `consumeBatch`) utilizam `useWriterConnection { conn -> conn.withTransaction(IMMEDIATE) { ... } }`, impedindo que leituras e escritas concorrentes corrompam o estoque.

---

## 🧪 Testes no Módulo (:data)

O módulo contém **132 testes automatizados** rodando com banco SQLite in-memory, cobrindo todas as migrações de versão, operações LWW e transações atômicas.
