# 🛒 Bem-vindo à Wiki do Minha Despensa

O **Minha Despensa** é uma aplicação **Kotlin Multiplatform (Android & iOS)** desenvolvida com foco em arquitetura Local-First para gerenciamento integrado de despensa doméstica, listas de compras, histórico de preços e controle orçamentário.

---

## 🎯 Por que o Minha Despensa existe?

Em vez de tratar a despensa e a lista de compras como ferramentas isoladas, o projeto unifica as decisões do ciclo de compras doméstico em um fluxo contínuo:

```text
Catálogo de Produtos ──► Lista de Compras ──► Finalizar Compra (Checkout Atômico)
                                                    │
                              ┌─────────────────────┴─────────────────────┐
                              ▼                                           ▼
                       Estoque na Despensa                         Histórico de Preços
                              │
                              ▼
                     Consumo & Controle de Validade
                              │
                              ▼
                    Sugestões de Reposição
```

---

## 🧭 Estrutura da Documentação

Navegue pelos tópicos usando o menu lateral ou pelos atalhos abaixo:

### 🏛️ Arquitetura & Módulos
* **[[Visão Geral de Arquitetura|Architecture-Overview]]:** Clean Architecture, MVVM em Compose Multiplatform, sincronização em nuvem e fluxo reativo.
* **[[Módulo :core|Module-Core]]:** Camada de domínio, 30 casos de uso, modelos puros de estoque, finanças, autenticação, casas compartilhadas e contratos de atualização.
* **[[Módulo :data|Module-Data]]:** Persistência Room KMP (Schema v9), ledger de movimentações, pipeline de migrations SQLite, motor de sincronização Outbox/LWW e clientes Ktor.
* **[[Módulo :uisystem|Module-UISystem]]:** Design System proprietário em Glassmorphism, tokens de tipografia, widgets financeiros, máquinas de estado, tela de conta familiar e seção de atualização de software.

### 🧪 Testes & Guias
* **[[Guia do Testador Alpha|Alpha-Testing-Guide]]:** Instruções de instalação do APK `v1.2.0-alpha` e roteiro completo com 12 cenários de teste detalhados (incluindo despensas familiares, sync em nuvem e atualização OTA in-app).
* **[[Roadmap & Próximos Passos|Roadmap]]:** Planejamento de inteligência de consumo, receitas e notificações nativas.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia Principal |
| :--- | :--- |
| **Linguagem** | Kotlin 2.x |
| **Multiplataforma** | Kotlin Multiplatform (KMP) |
| **Interface** | Compose Multiplatform (Android / iOS) |
| **Banco de Dados** | Room KMP v9 + Bundled SQLite Driver |
| **Networking & OTA** | Ktor Client (CIO / OkHttp / Darwin) + SSE/Streaming |
| **Injeção de Dependência** | Koin Multiplatform |
| **Programação Reativa** | Kotlin Coroutines + Flow / StateFlow |
| **Serialização** | kotlinx.serialization |
| **Datas & Tempo** | kotlinx.datetime |
| **Scanner de Código** | CameraX + Google ML Kit Barcode Scanning |
| **Testes Automatizados** | 553 testes unitários (Turbine, In-Memory SQLite, Fake Repositories) |
