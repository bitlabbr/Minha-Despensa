# 🧪 Guia do Testador Alpha (Release v1.1.1-alpha)

Seja bem-vindo ao programa de testes da versão **v1.1.1-alpha** do **Minha Despensa**! 🛒✨

Este documento é o roteiro oficial passo a passo para validar tanto as funcionalidades consolidadas de catálogo, despensa e listas, quanto as grandes novidades de **Gestão Financeira Familiar**, **Orçamento Mensal**, **Valoração de Estoque em Reais** e **Analytics Anual de 12 Meses**.

---

## 📲 Como Instalar e Configurar no Android

1. Baixe o pacote oficial **`composeApp-release.apk`** na página da [Release v1.1.1-alpha](https://github.com/bitlabbr/Minha-Despensa/releases/tag/v1.1.1-alpha).
2. No celular Android (compatível com Android 8.0 / API 26 ou superior), toque na notificação de download ou abra o arquivo pelo gerenciador de arquivos.
3. Se o sistema exibir um aviso de segurança sobre fontes desconhecidas, autorize a instalação para o navegador ou gerenciador utilizado.
4. Conclua a instalação e abra o aplicativo **Minha Despensa**.
5. Quando solicitado pelo leitor de código de barras, conceda a **permissão de câmera** (necessária apenas para leitura de códigos EAN).

---

## 🧭 Sumário dos Cenários de Teste

* **[Cenário 1: Cadastro, Busca, Filtro e Edição de Produto no Catálogo](#-cenário-1-cadastro-busca-filtro-e-edição-de-produto)**
* **[Cenário 2: Gestão de Estoque na Despensa (Scanner, Lotes, Preços e Consumo)](#-cenário-2-gestão-de-estoque-na-despensa-adição-busca-edição-e-consumo)**
* **[Cenário 3: Criação de Listas de Compras (Direta, Planejada e Rascunho)](#-cenário-3-criação-de-listas-de-compras)**
* **[Cenário 4: No Supermercado com o Assistente de Compras](#-cenário-4-no-supermercado-com-o-assistente-de-compras)**
* **[Cenário 5: Conferência Pós-Compra (Estoque Automático, Histórico e Ledger)](#-cenário-5-conferência-pós-compra-estoque-histórico-e-despesas)**
* **[Cenário 6: Configuração de Orçamento Mensal na Home](#-cenário-6-configuração-de-orçamento-mensal-na-home-financialwidget)**
* **[Cenário 7: Valoração da Despensa & Analytics Financeiro Anual (12 Meses)](#-cenário-7-valoração-da-despensa--analytics-financeiro-anual)**
* **[Cenário 8: Edição de Lote e Preço Unitário na Despensa / Detalhes](#-cenário-8-edição-de-lote-e-preço-unitário-na-despensa--detalhes)**
* **[Cenário 9: Alertas de Vencimento e Tendências de Consumo na Home](#-cenário-9-alertas-de-vencimento-e-tendências-de-consumo-na-home)**

---

## 🎯 Roteiro de Testes Detalhado

---

### 📦 Cenário 1: Cadastro, Busca, Filtro e Edição de Produto

**Objetivo:** Validar o ciclo completo de gerenciamento de um item no Catálogo: criação via formulário unificado, persistência, busca com normalização de acentos, filtros por categoria e edição de dados com remoção de fotos.

#### 1. Criação do Produto
1. Acesse a aba **Catálogo** (ícone de categorias na barra inferior).
2. Clique no botão de ação **`Registrar Produto`**.
3. No painel inferior (*Bottom Sheet*), preencha os dados:
   * **Foto:** Tire uma foto ou selecione uma imagem da galeria (opcional).
   * **Nome:** ex.: *"Café Especial Torrado"*.
   * **Marca:** ex.: *"Orfeu"*.
   * **Categoria:** Selecione *"Bebidas"*.
   * **Unidade de Medida & Peso Líquido:** Selecione *"Grama (g)"* e preencha *"500"*.
   * **Código de Barras (EAN):** Digite um código válido de 8, 12, 13 ou 14 dígitos ou bipe com a câmera.
4. Clique no botão **`Salvar Produto`**.
5. **O que validar:**
   * O painel deve fechar com animação suave.
   * O produto deve aparecer imediatamente na listagem do Catálogo.

#### 2. Teste da Barra de Pesquisa com Acentos e Marca
1. No campo de busca do Catálogo:
   * Digite sem acento (ex.: *"cafe"*) e verifique se o produto *"Café Especial Torrado"* é localizado.
   * Digite apenas um trecho (ex.: *"especial"*).
   * Digite a marca (ex.: *"Orfeu"*).
2. **O que validar:**
   * A busca deve ser instantânea, insensível a maiúsculas/minúsculas e tolerante a diacríticos (acentos).

#### 3. Teste dos Chips de Categorias
1. Com o campo de busca limpo, clique no chip da categoria **Bebidas**.
2. Clique em outras categorias (ex.: *Limpeza*, *Grãos*).
3. **O que validar:**
   * O produto só deve ser exibido ao filtrar pela sua categoria correspondente.

#### 4. Edição do Produto e Remoção de Imagem
1. Clique no card do produto para abrir a tela de **Detalhes** e selecione a opção **Editar**.
2. Altere o nome (ex.: *"Café em Grãos Premium"*).
3. Se adicionou uma foto, toque no ícone de lixeira/remover foto.
4. Salve as alterações.
5. **O que validar:**
   * Os detalhes e o card no catálogo devem refletir o novo nome e o placeholder padrão de imagem.

---

### 🥫 Cenário 2: Gestão de Estoque na Despensa (Adição, Busca, Edição e Consumo)

**Objetivo:** Validar todos os modos de inclusão de produtos no estoque da despensa (via código de barras, busca no catálogo e cadastro de novo produto durante a inserção), incluindo **quantidade, validade, lote e preço unitário**, além do fluxo de baixa por consumo.

---

#### 2.1 Adicionar à Despensa via Leitor de Código de Barras
1. Acesse a aba **Minha Despensa** (ícone de cozinha na barra inferior).
2. Clique em **`Adicionar à Despensa`**.
3. No menu deslizante, escolha **`Escanear código de barras`**.
4. Aponte a câmera para o código de barras de um produto cadastrado.
5. **O que validar:**
   * O leitor centraliza a mira (filtro de ROI) e reconhece o código com estabilidade (sem leituras falsas).
   * Abre automaticamente o painel de entrada de estoque.
6. Preencha os campos de estoque:
   * **Quantidade:** ex.: *2*.
   * **Validade:** Experimente os chips de atalho rápido (**`+7d`**, **`+15d`**, **`+30d`**, **`+60d`**) ou escolha uma data no calendário.
   * **Número do Lote:** ex.: *"LOTE-A1"*.
   * **Preço Unitário (R$):** ex.: *R$ 14,50*.
7. Clique em **`Adicionar à Despensa`**.
8. **O que validar:**
   * O item passa a constar na listagem da Despensa exibindo a quantidade e a validade.
   * O preço unitário alimenta a valoração total da despensa.

#### 2.2 Adicionar à Despensa a partir do Catálogo
1. Na tela **Minha Despensa**, clique em **`Adicionar à Despensa`** e escolha **`Buscar no Catálogo`**.
2. Selecione um item da listagem existente.
3. Preencha quantidade, validade, lote e preço unitário.
4. Confirme a adição.
5. **O que validar:**
   * O item selecionado é adicionado ao estoque imediatamente.

#### 2.3 Cadastrar Novo Produto Diretamente da Despensa
1. Na tela **Minha Despensa**, clique em **`Adicionar à Despensa`** e selecione **`Cadastrar novo produto`**.
2. Preencha a ficha do produto (nome, marca, categoria, unidade) e salve.
3. Em seguida, preencha a quantidade, validade, lote e preço e conclua.
4. **O que validar:**
   * O produto entra no estoque da despensa e **também fica disponível no catálogo geral** para reaproveitamento futuro.

#### 2.4 Consumo de Itens e Baixa Total de Estoque
1. Na Despensa, toque em um item para abrir a tela de **Detalhes**.
2. Localize a seção de estoque e clique no botão **`Consumir`**.
3. Confirme o consumo de 1 unidade ou utilize o fluxo de baixa total até esgotar o estoque.
4. **O que validar:**
   * A quantidade estocada é reduzida imediatamente.
   * Ao atingir zero unidades, o item deixa de ser exibido na Despensa ativa.
   * O produto **continua existindo no Catálogo**, provando que o consumo dá baixa no estoque físico sem deletar o cadastro do produto.

---

### 🛒 Cenário 3: Criação de Listas de Compras

**Contexto:** O aplicativo oferece listas planejadas estruturadas (`PLANNED`), notas rápidas de texto livre (`SCRATCHPAD`) e sessões de compra direta (`ASSISTANT`). Todos os modos são totalmente integrados com a despensa e catálogo.

---

#### 3.1 Lista Planejada com Teto de Gastos
1. Acesse a aba **Listas de Compras** (ícone de carrinho).
2. Clique no botão de criação de **`Lista Planejada`**.
3. Defina um nome para a lista (ex.: *"Compras do Mês"*) e um orçamento limite (ex.: *R$ 350,00*).
4. No seletor de produtos do catálogo, selecione itens e ajuste quantidades com os botões `+` e `-`.
5. Utilize a barra de busca para filtrar produtos por nome ou marca.
6. Salve a lista.
7. **O que validar:**
   * A lista aparece na aba de listas ativas com a contagem correta de itens e o orçamento configurado.

#### 3.2 Lista Rápida (Texto Livre / Scratchpad)
1. Na aba **Listas**, escolha **`Lista Rápida`**.
2. Digite ou cole itens em linhas separadas (ex.: *"Detergente\nEsponja de Aço\nSabão em Pó"*).
3. Salve a lista.
4. **O que validar:**
   * As linhas de texto são convertidas automaticamente em itens avulsos da lista.

---

### 🛍️ Cenário 4: No Supermercado com o Assistente de Compras

**Objetivo:** Simular uma ida real ao supermercado, conferindo itens do carrinho, inserindo preços da gôndola, bipando produtos pela câmera e prevenindo duplicidades.

1. Na listagem de listas de compras, abra uma lista ativa e toque em **`Iniciar Compras`** (ou use **`Compra Direta`**).
2. Conforme você colocar itens no carrinho físico:
   * Toque no checkbox do item para marcá-lo como pego.
   * Digite o preço unitário que encontrou na prateleira (ex.: *R$ 8,90*).
3. **Painel Financeiro Superior:**
   * Observe o total acumulado do carrinho calculando em centavos em tempo real (`quantidade × preço`).
   * A barra de orçamento deve avançar proporcionalmente ao teto configurado.
4. **Adicionar Item com Câmera no Meio da Compra:**
   * Toque no botão de adicionar e use o leitor de código de barras.
   * Bipe um produto da prateleira, informe quantidade e preço.
5. **Teste de Detecção de Duplicados:**
   * Tente adicionar novamente um produto que já está no carrinho.
   * **O que validar:** O aplicativo deve exibir um banner de aviso informando que o item já existe no carrinho com X unidades e oferecer a consolidação automática das quantidades.
6. **Busca Rápida no Carrinho:**
   * Utilize a barra de pesquisa expansível no topo para filtrar itens por nome, categoria ou marca.
   * **O que validar:** A filtragem visual não altera os totais financeiros de compras já calculados.
7. **Substituição de Item:**
   * Caso não encontre um produto da lista, use a opção de substituir item para trocá-lo por outra marca ou produto similar.
8. Ao terminar a compra, clique no botão em destaque **`Finalizar Compra`**.
9. **O que validar:**
   * O checkout atômico executa com sucesso.
   * A lista passa para o status de concluída (`COMPLETED`).

---

### 📦 Cenário 5: Conferência Pós-Compra (Estoque, Histórico e Despesas)

**Objetivo:** Confirmar que a finalização da compra alimentou todas as camadas da aplicação de forma integrada e atômica.

1. **Aba Despensa:**
   * Navegue até a aba **Minha Despensa**.
   * **O que validar:** Todos os itens marcados na compra foram adicionados ao estoque da despensa com as quantidades e preços unitários pagos.
2. **Aba Catálogo & Histórico de Preços:**
   * Acesse a aba **Catálogo** e clique em um dos produtos comprados para abrir a tela de **Detalhes**.
   * **O que validar:** O gráfico de histórico de preços exibe um novo ponto na data de hoje com o preço exato registrado no supermercado.
3. **Aba Início (Home):**
   * Volte para a aba **Início**.
   * **O que validar:** O valor total gasto na compra foi automaticamente computado no resumo financeiro do mês corrente!

---

### 📊 Cenário 6: Configuração de Orçamento Mensal na Home (`FinancialWidget`)

**Objetivo:** Validar o planejamento financeiro familiar, a definição de teto orçamentário e a reação visual da interface em tempo real.

1. Acesse a aba **Início** (ícone Home).
2. Localize o card principal do **Resumo Financeiro**.
3. Se você ainda não definiu um orçamento para o mês corrente:
   * **O que validar:** O card exibe o total gasto e um botão convidativo **`Definir Meta Orçamentária`**.
4. Toque no botão para abrir o diálogo translúcido `SetMonthlyBudgetDialog`.
5. Digite um valor de orçamento (ex.: *R$ 600,00*) e confirme.
6. **O que validar:**
   * O card atualiza instantaneamente, exibindo:
     * O total gasto no mês (acumulado de compras e entradas).
     * O limite orçamentário definido (ex.: *R$ 600,00*).
     * O saldo restante disponível.
     * A barra de progresso visual (`InverseAnchoredGauge`) indicando a fatia já utilizada.
7. **Teste de Teto Estourado:**
   * Toque novamente sobre o card de finanças para editar a meta.
   * Defina um valor menor que as despesas já acumuladas (ex.: *R$ 10,00*).
   * Confirme a alteração.
   * **O que validar:** O status muda imediatamente para **"Estourado"**, destacando a barra e os textos em tom de alerta (vermelho/âmbar) e indicando a porcentagem excedente.
8. **Teste de Persistência Offline:**
   * Feche completamente o aplicativo (remova da lista de apps recentes) e reabra.
   * **O que validar:** O orçamento configurado e as despesas permanecem salvos com precisão (persistidos no SQLite via Room KMP).

---

### 🏷️ Cenário 7: Valoração da Despensa & Analytics Financeiro Anual

**Objetivo:** Conferir o patrimônio total estocado na despensa e navegar pelo histórico analítico de 12 meses.

1. Na aba **Início**, role até o card **`Valor Total em Estoque`** (`StockValueCard`).
2. **O que validar:**
   * Exibe o montante monetário total dos itens ativos na despensa em reais (`R$ X,XX`).
   * Exibe a contagem de itens avaliados versus total (ex.: *"12 de 15 itens com preço informado"*).
   * Exibe o custo médio unitário em estoque.
3. Toque no card de estoque ou no botão de analytics (ícone de gráfico no topo do resumo financeiro).
4. O modal **`Analytics Financeiro`** (`FinancialAnalyticsDialog`) será aberto.
5. **O que validar no modal:**
   * **Topo:** Card de estoque avaliado com resumo patrimonial.
   * **Indicadores Anuais:**
     * **Total Gasto no Ano:** Soma consolidada de todas as despesas do ano vigente.
     * **Média Mensal:** Gasto médio por mês decorrido.
     * **Custo Médio por Item:** Relação ponderada entre o valor total investido e o volume de produtos comprados.
   * **Evolução de 12 Meses (Janeiro a Dezembro):**
     * 12 barras mensais ilustrando a proporção de gastos em relação à meta de cada mês.
     * Badges de status para cada mês (**"Na meta"**, **"Estourado"** ou **"Sem meta"**).
     * Nomes dos meses devidamente traduzidos em português (Jan, Fev, Mar, Abr, Mai, Jun, Jul, Ago, Set, Out, Nov, Dez).
6. Toque no botão **`Fechar`** ou fora do modal para dispensá-lo.

---

### ✏️ Cenário 8: Edição de Lote e Preço Unitário na Despensa / Detalhes

**Objetivo:** Validar a edição in-place de preços unitários em lotes da despensa e comprovar que as métricas de menor/maior preço são recalculadas sem gerar duplicatas no banco de dados.

1. Acesse a aba **Minha Despensa** e clique em um produto para abrir a tela de **Detalhes**.
2. Na seção **Lotes na Despensa**:
   * Observe que lotes sem preço exibem a etiqueta informativa: `Sem valor informado • Toque p/ definir`.
   * Lotes com preço exibem o valor unitário (`R$ X,XX / un`) e o valor total do lote.
3. Toque em um lote para abrir o painel deslizante de edição (`EditPantryBatchSheet`).
4. Altere ou preencha o campo **Preço Unitário** (ex.: altere de *R$ 10,00* para *R$ 12,50*).
5. Experimente também alterar a data de validade usando os chips rápidos (`+15d`, `+30d`).
6. Clique em **`Salvar Alterações`**.
7. **O que validar:**
   * O lote na lista atualiza imediatamente com o novo valor unitário e o subtotal recalcula (`quantidade × novo preço unitário`).
   * O bloco de **Menor Preço** e **Maior Preço** no topo da tela de detalhes recalcula em tempo real.
   * O histórico de preços do produto não ganha linhas fantasmas repetidas (atualização *in-place* atômica).
   * Volte à aba **Início**: o card **`Valor Total em Estoque`** recalcula o patrimônio da despensa instantaneamente.

---

### ⏰ Cenário 9: Alertas de Vencimento e Tendências de Consumo na Home

**Objetivo:** Verificar a exibição em tempo real de produtos próximos ao vencimento e os itens mais consumidos da despensa.

1. Acesse a aba **Início**.
2. **Widget Itens Próximos do Vencimento (`ExpiringSoonWidget`):**
   * Se houver produtos cadastrados com validade nos próximos 7 dias:
     * O card deve listar os itens com seus ícones de categoria e prazo formatado dinamicamente: *"Vence hoje"*, *"Vence amanhã"*, *"Vence em X dias"* ou *"Vencido há X dias"*.
   * Se todos os itens estiverem com prazo longo:
     * O widget deve exibir o estado amigável: *"Tudo em dia na sua despensa!"*.
3. **Widget Tendências de Consumo (`ConsumptionTrendWidget`):**
   * Após consumir alguns itens na despensa (Cenário 2.4):
     * O card deve listar os produtos de maior saída no mês corrente, indicando a quantidade e unidade consumida.
   * Se não houver movimentações de consumo no mês:
     * Exibe o estado informativo de ausência de dados.

---

## 🐞 Como Relatar um Problema ou Sugestão

Se durante os testes você encontrar algum comportamento inesperado, erro visual, lentidão ou tiver uma sugestão de melhoria:

1. Anote o que aconteceu e o modelo do seu celular Android (marca, modelo e versão do Android).
2. Tire uma captura de tela (*screenshot*) ou grave a tela demonstrando o fluxo.
3. Abra uma issue diretamente no nosso repositório:  
   👉 **[Abrir Nova Issue no GitHub](https://github.com/bitlabbr/Minha-Despensa/issues/new)**  
   *(Ou envie as anotações e prints diretamente para o desenvolvedor responsável)*.

Obrigado por ajudar a construir o **Minha Despensa**! 💙🚀
