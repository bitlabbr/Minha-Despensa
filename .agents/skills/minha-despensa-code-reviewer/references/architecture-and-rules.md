# Arquitetura e Regras de Negócio - Minha Despensa KMP

Este documento serve como referência de arquitetura, padrões e regras de negócio para revisões de código no projeto Minha Despensa.

---

## 1. Visão Geral da Arquitetura Modular (Clean Architecture)

O projeto é construído em Kotlin Multiplatform (Android + iOS Desktop/Web ready) dividido em quatro módulos com limites arquiteturais estritos:

```
┌─────────────────────────────────────────────────────────────┐
│                        :composeApp                          │
│        (Entry points, App Navigation, Theme Setup)          │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                         :uisystem                           │
│ (Compose Multiplatform, Design System, Screens, ViewModels) │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
               ▼                              │
┌──────────────────────────────┐              │
│            :data             │              │
│ (Room KMP, DAOs, Entities,   │              │
│  Repository Implementations) │              │
└──────────────┬───────────────┘              │
               │                              │
               ▼                              ▼
┌─────────────────────────────────────────────────────────────┐
│                            :core                            │
│     (Domain Models, Repository Interfaces, Use Cases)       │
└─────────────────────────────────────────────────────────────┘
```

### Regras de Dependência:
- **`:core`**: Não depende de **nenhum** outro módulo do projeto. Não contém dependências de Android (`android.*`) nem de UI (`androidx.compose.*`). Contém apenas lógica de domínio pura, interfaces de repositório, use cases, constantes e utilitários multiplataforma (`kotlinx.datetime`, `kotlinx.serialization`).
- **`:data`**: Depende apenas de `:core`. Implementa os repositórios definidos no `:core` usando Room KMP, SQLite e processadores de imagem multiplataforma. Não conhece a camada de apresentação (`:uisystem`).
- **`:uisystem`**: Depende de `:core` e de recursos de UI (`composeMultiplatform`). Contém o Design System reutilizável (componentes core e domain), ViewModels e telas. Não deve depender diretamente de classes concretas de `:data` (acesso a repositórios ocorre via interfaces de `:core` injetadas pelo Koin).
- **`:composeApp`**: Módulo agregador final. Configura a navegação principal (`NavHost`), injeção de dependências global (`initKoin`), splash e ciclo de vida do aplicativo em cada plataforma.

---

## 2. Camada de Apresentação e Padrão MVVM (:uisystem)

### 2.1 ViewModels
- Herdam de `androidx.lifecycle.ViewModel`.
- Expõem o estado da tela por meio de um único `StateFlow<UiState>` ou sub-estados tipados (`filterState`, `listState`).
- Usam o operador `stateIn` com escopo `viewModelScope`, estratégia `SharingStarted.WhileSubscribed(5000)` e valor inicial tipado.
- Subfluxos modais (sheets, scanners, confirmações) são modelados via **Sealed Interfaces / Sealed Classes** (ex: `PantrySubFlow`, `ShoppingAssistantSubFlow`).
- Tratam erros e mensagens por meio de `AppNotificationManager` com `UiText` desacoplado, evitando strings hardcoded na lógica de negócio.

### 2.2 Composables (Telas e Componentes)
- **Separação Screen vs Content/Widgets:**
  - `*Screen`: Obtém o ViewModel via `koinViewModel()`, coleta o `uiState` via `collectAsState()` e gerencia navegação e sheets.
  - Componentes/Widgets: Devem ser stateless sempre que possível, recebendo estados imutáveis e expondo eventos via callbacks lambdas (`onClick`, `onValueChange`).
- **Design System:** Usar prioritariamente os componentes de `components/core` (`ItemContainerGlassCard`, `MinhaDespensaButton`, `PrimaryContainerHeader`, `MinhaDespensaTopBar`) e estilos do `MinhaDespensaTheme`.
- **Internacionalização:** Todas as strings exibidas na UI devem vir de `Res.string.*` (KMP Compose Resources). Nunca utilize strings em português ou inglês hardcoded nos arquivos de UI.

---

## 3. Padrões de Persistência e Estratégia Offline-First (:data)

### 3.1 Padrão CRDT LWW (Last-Write-Wins)
O Minha Despensa foi projetado para funcionamento 100% offline com futura sincronização distribuída.
- **Campos Obrigatórios:** Todas as tabelas que representam dados do usuário (`catalog_products`, `pantry_items`, `shopping_lists`, `shopping_list_items`, `price_entries`) **devem** conter:
  - `updatedAt: Long`: timestamp em milissegundos UTC da última alteração (`Clock.System.now().toEpochMilliseconds()`).
  - `isDeleted: Boolean`: flag de exclusão lógica.
- **Soft-Delete Obrigatório:**
  - **Nunca** execute `DELETE FROM tabela` para itens sincronizáveis.
  - Exclusões devem ser feitas via `isDeleted = 1` e `updatedAt = now`.
- **Resolução de Conflitos em Escritas:**
  - DAOs devem usar queries condicionadas ao timestamp (ex: `WHERE updatedAt <= :updatedAt` ou `updateIfNewer`) para evitar que dados desatualizados sobrescrevam dados mais recentes.

### 3.2 Integridade Referencial
- Chaves estrangeiras (`ForeignKey`) devem ser definidas explicitamente nas Entities do Room com índices nas colunas indexadas (`@Index`) para garantir performance de query e integridade relacional.

---

## 4. Regras de Negócio Críticas

### 4.1 Despensa (Pantry)
- **Persistência Granular:** Cada compra ou inserção de produto gera um registro `PantryItem` individual no banco, preservando data de validade (`expirationDate`) e número de lote (`batchNumber`).
- **Agregação na Apresentação:** A tela da despensa consolida itens pelo `productId`:
  - Quantidades de lotes distintos são somadas (`sumOf`).
  - A data de validade exibida é a mais próxima / urgente (`minOrNull`).
  - Se algum lote estiver com validade expirada (`expirationDate < now`), o item consolidado recebe o status `isExpired = true`.
- **Consumo:** O abatimento de itens deve seguir a estratégia FEFO (First Expired, First Out) ou FIFO (First In, First Out).

### 4.2 Listas de Compras (Shopping Lists)
- **Ciclo de Vida do Status:** `ACTIVE` -> `COMPLETED` ou `CANCELLED` (soft-deleted).
- **Finalização de Compra (`finalizePurchase`):**
  - Executada dentro de transação atômica (`db.useWriterConnection`).
  - Cada item comprado e marcado (`isChecked = true`) é transferido para a tabela `pantry_items`.
  - Se houver preço registrado (`priceAtTime`), um novo `PriceEntry` é criado no histórico de preços.
  - Os itens são desmarcados e a lista passa para status `COMPLETED`.
- **Orçamento e Teto de Gastos:**
  - Cada lista possui um teto de gastos (`budgetCeilingInCents: Long?`).
  - A interface exibe em tempo real o valor atual do carrinho, o teto orçamentário e o saldo restante, sinalizando visualmente quando o teto for ultrapassado.
- **Inserção Flexível:** A adição de itens à lista permite leitura por código de barras (scanner), busca no catálogo de produtos e fallback para texto livre (produto ad-hoc).

### 4.3 Catálogo de Produtos (Catalog)
- Chave identificadora: `id` (UUID) e opcionalmente `ean` (código de barras EAN-13/UPC-A).
- Validação e higienização de EAN: aceita apenas dígitos válidos.
- Imagens de produtos são armazenadas compactadas como `ByteArray` e redimensionadas por processadores específicos de cada plataforma.

---

## 5. Diretrizes de Testes e Qualidade

- **Nenhum PR deve ser aprovado sem testes unitários correspondentes.**
- **Módulo `:core`:** Testar use cases com fakes/mocks de repositório, validando regras de negócio, limites e caminhos de erro.
- **Módulo `:data`:** Testar repositórios e queries Room usando banco de dados em memória (`in-memory database`) no `commonTest`.
- **Módulo `:uisystem`:** Testar ViewModels usando `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`, `advanceUntilIdle`) e `Turbine` para fluxos assíncronos.
- **Execução do build:** O comando `./gradlew testDebugUnitTest` deve passar com 100% de sucesso sem warnings de compilação ou regressões.
