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
* **[[Visão Geral de Arquitetura|Architecture-Overview]]:** Clean Architecture, MVVM em Compose Multiplatform e fluxo reativo.
* **[[Módulo :core|Module-Core]]:** Camada de domínio, 11 casos de uso, modelos puros e regras de validação.
* **[[Módulo :data|Module-Data]]:** Persistência Room KMP (Schema v4), pipeline de migrations SQLite e integridade Last-Write-Wins (LWW).
* **[[Módulo :uisystem|Module-UISystem]]:** Design System proprietário em Glassmorphism, tokens de tipografia, máquinas de estado e telas do app.

### 🧪 Testes & Guias
* **[[Guia do Testador Alpha|Alpha-Testing-Guide]]:** Instruções de instalação do APK `v1.0.0-alpha` para amigos e roteiro de testes.
* **[[Roadmap & Próximos Passos|Roadmap]]:** Planejamento de inteligência de consumo, receitas e notificações nativas.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia Principal |
| :--- | :--- |
| **Linguagem** | Kotlin 2.x |
| **Multiplataforma** | Kotlin Multiplatform (KMP) |
| **Interface** | Compose Multiplatform (Android / iOS) |
| **Banco de Dados** | Room KMP v4 + Bundled SQLite Driver |
| **Injeção de Dependência** | Koin Multiplatform |
| **Programação Reativa** | Kotlin Coroutines + Flow / StateFlow |
| **Serialização** | kotlinx.serialization |
| **Datas & Tempo** | kotlinx.datetime |
| **Scanner de Código** | CameraX + Google ML Kit Barcode Scanning |
| **Testes Automatizados** | 279 testes unitários (Turbine, In-Memory SQLite, Fake Repositories) |
