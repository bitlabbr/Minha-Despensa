# Plano Sub-atômico: Isolamento Total na Criação e Alternância de Casas (Workspaces)

**Módulo(s) Impactado(s):** `:core`, `:data`, `:uisystem`  
**Autor:** Engenheiro de Software Sênior (Tech Lead)  
**Data:** 23 de Outubro de 2026  
**Status:** Em Execução  

---

## 1. Entendimento do Requisito & Análise de Causa Raiz (RCA)

### Contexto / Problema
Ao criar uma nova casa (`ManageHouseholdUseCase.createHousehold`) no aplicativo móvel em um dispositivo autenticado, todos os dados locais da casa anterior (produtos do catálogo, lotes de despensa, movimentações, listas de compras e orçamento) foram indevidamente enfileirados na outbox da nova casa e sincronizados (`push`) para o backend sob o ID da nova casa. Como resultado, a nova casa tornou-se uma cópia poluída da casa anterior, mídias de produtos geraram erros 404 (isolamento de armazenamento no backend) e a alternância entre casas apresentou dados replicados.

### Causa Raiz
Em `ManageHouseholdUseCase.kt`:
1. `createHousehold()` chamava `householdRepository.switchActiveHousehold(household.id)` e, em seguida, executava explicitamente `syncRepository.enqueueAllLocalEntitiesForInitialSync().getOrThrow()`.
2. O método **nunca limpava o banco de dados Room local** antes de trocar para a nova casa (`clearLocalDataForHouseholdSwitch()`).
3. Por consequência, a rotina de sync lia as entidades remanescentes da casa antiga no Room, gerava mutações de outbox para a nova casa e subia os registros via push.
4. Em `JoinHouseholdByInviteCodeUseCase.kt`, faltava também descarregar alterações pendentes da casa anterior (`flushOutboxBeforeSwitch`) antes de limpar e ingressar na nova despensa.

---

## 2. Etapas Sub-atômicas de Implementação

### Etapa 1: Correção nos Use Cases de Domínio (`:core`)
* **`ManageHouseholdUseCase.kt`:**
  * Executar `syncRepository.flushOutboxBeforeSwitch(forceDiscardPending = false)` para garantir que qualquer alteração pendente da casa atual seja enviada antes da migração.
  * Criar a casa remota: `val household = householdRepository.createHousehold(trimmed).getOrThrow()`.
  * Limpar a persistência local da casa anterior: `syncRepository.clearLocalDataForHouseholdSwitch()`.
  * Atualizar o ID ativo na sessão: `householdRepository.switchActiveHousehold(household.id).getOrThrow()`.
  * Acionar sincronização limpa para a nova casa: `syncRepository.sync().getOrThrow()`.
  * **Remover** a chamada indevida `syncRepository.enqueueAllLocalEntitiesForInitialSync()`.
* **`JoinHouseholdByInviteCodeUseCase.kt`:**
  * Adicionar `syncRepository.flushOutboxBeforeSwitch(forceDiscardPending = false)` antes do switch e sync.

### Etapa 2: Atualização da Suíte de Testes Unitários (`:core`)
* Atualizar `AuthAndHouseholdUseCasesTest.kt` para assegurar que:
  * `createHousehold()` invoca `flushOutboxBeforeSwitch`, `clearLocalDataForHouseholdSwitch` e `sync`.
  * `createHousehold()` **não** invoca `enqueueAllLocalEntitiesForInitialSync`.
  * `joinHousehold()` descarrega outbox pendente antes do switch.

### Etapa 3: Validação da Camada e Sincronização de Documentação
* Executar `./gradlew :core:testDebugUnitTest`.
* Atualizar `core/core.md` documentando as garantias de isolamento em `ManageHouseholdUseCase` e `JoinHouseholdByInviteCodeUseCase`.
* Commit estruturado com as regras da skill `minha-despensa-feature-engineer`.

---

## 3. Critérios de Aceite e DoD
- [ ] Criação de nova casa deixa o banco local limpo antes de puxar dados remotos.
- [ ] 0 mutações de entidades antigas são enviadas para a nova casa.
- [ ] 100% dos testes unitários em `:core`, `:data` e `:uisystem` verdes.
- [ ] Documentação de módulo atualizada.
