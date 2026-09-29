# 🏛️ Visão Geral de Arquitetura

O **Minha Despensa** foi projetado seguindo rigorosamente os princípios de **Clean Architecture**, combinado com o padrão **MVVM** na camada de apresentação e um fluxo reativo unidirecional (*Unidirectional Data Flow - UDF*).

---

## 🧩 Modularização do Projeto

O projeto é dividido em módulos Gradle independentes, garantindo isolamento de responsabilidades e desacoplamento de frameworks externos:

```text
┌───────────────────────────────────────────────┐
│                  :composeApp                  │  Ponto de entrada (Android & iOS), DI bootstrap
└───────────────────────┬───────────────────────┘
                        ▼
┌───────────────────────────────────────────────┐
│                   :uisystem                   │  Compose Multiplatform, Tokens Glassmorphism,
│                                               │  Telas, ViewModels, Scanner ML Kit
└───────────────────────┬───────────────────────┘
                        ▼
┌───────────────────────────────────────────────┐
│                     :core                     │  Domínio puro: Modelos, 11 Use Cases, Contratos
│                                               │  Zero dependências em UI ou Banco de Dados
└───────────────────────▲───────────────────────┘
                        │ implementa
┌───────────────────────┴───────────────────────┐
│                     :data                     │  Room KMP v4, SQLite Bundled Driver,
│                                               │  DAOs, Entidades, Mappers, Migrations
└───────────────────────────────────────────────┘
```

---

## 🔄 Fluxo de Dados Reativo (UDF)

A comunicação entre a persistência e a interface gráfica é 100% reativa e orientada a fluxos assíncronos:

```text
SQLite (Room KMP)
       │
       ▼
DAO (Room @Query com Flow)
       │
       ▼
Repository (Converte Entidades para Modelos de Domínio)
       │
       │ Flow<DomainModel>
       ▼
ViewModel (Transforma e combina streams em UI States)
       │
       │ StateFlow<UiState>
       ▼
Compose Multiplatform UI (Observa e reage a mudanças de estado)
```

### Princípios Chave:
1. **Fonte Única da Verdade:** O banco de dados Room SQLite local é a única fonte autoritativa de dados da aplicação.
2. **Imutabilidade:** Modelos de domínio e `UiState` são data classes Kotlin imutáveis.
3. **Estado Previsível:** Máquinas de estado baseadas em sealed classes (`SubFlow`) impedem estados visuais inconsistentes.
