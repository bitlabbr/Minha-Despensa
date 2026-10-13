# 🧪 Guia do Testador Alpha (Release 1.0.1-alpha)

Se você recebeu o pacote de teste do **Minha Despensa**, este guia explica como instalar e quais cenários você deve experimentar passo a passo para validar as correções e novas funcionalidades!

---

## 📲 Como Instalar no Android

1. Baixe o arquivo **`composeApp-release.apk`** da aba [Releases](https://github.com/bitlabbr/Minha-Despensa/releases).
2. No seu celular Android, abra o arquivo baixado.
3. Se for solicitado, autorize a instalação de fontes desconhecidas para o navegador/gerenciador de arquivos.
4. Conclua a instalação e abra o aplicativo **Minha Despensa**.

---

## 🎯 Roteiro de Testes Detalhado

---

### 📦 Cenário 1: Cadastro, Busca, Filtro e Edição de Produto

**Objetivo:** Validar o ciclo completo de gerenciamento de um item no Catálogo: criação via formulário, persistência, busca textual, filtros por categoria e edição de dados.

#### 1. Criação do Produto
1. Acesse a aba **Catálogo**.
2. Localize e clique no botão **`Registrar Produto`**.
3. Um *Bottom Sheet* (painel deslizante inferior) será exibido. Preencha todos os dados solicitados do produto (ex.: nome, código de barras, marca, categoria, unidade de medida, etc.).
4. Clique no botão **`Salvar Produto`**.
5. **O que validar:**
   - O *Bottom Sheet* deve fechar automaticamente.
   - O produto recém-cadastrado deve aparecer imediatamente na listagem do Catálogo.

#### 2. Validação dos Detalhes
1. Clique no cartão do produto recém-criado na listagem.
2. **O que validar:**
   - Verifique se todas as informações exibidas nos detalhes conferem rigorosamente com os dados que você preencheu ao cadastrar.

#### 3. Teste da Barra de Pesquisa
1. No topo da tela do Catálogo, utilize o campo de busca:
   - Digite o **nome completo** do produto e verifique se ele é retornado.
   - Apague e digite apenas um **pedaço (trecho) do nome** do produto e valide o resultado.
   - Apague e busque pela **marca** do produto e valide o resultado.
2. **O que validar:**
   - O produto deve ser filtrado e exibido com precisão nas três tentativas de busca.

#### 4. Teste dos Filtros de Categoria
1. Com a barra de pesquisa limpa, navegue pelos botões/chips de categorias na tela do Catálogo.
2. Clique na **categoria correta** em que o produto foi cadastrado.
3. Clique em **outras categorias**.
4. **O que validar:**
   - O produto deve ser exibido ao selecionar a categoria à qual pertence.
   - O produto **não** deve aparecer ao selecionar outras categorias distintas.

#### 5. Edição do Produto e Revalidação
1. Acesse a opção de **Editar** no produto cadastrado.
2. Altere o **Nome** e mude a **Categoria** para uma categoria diferente.
3. Salve as alterações.
4. **O que validar:**
   - O produto deve exibir os novos dados atualizados na listagem e na tela de detalhes.
   - Ao buscar na barra de pesquisa pelo **novo nome**, o produto deve ser localizado.
   - Ao filtrar pela **nova categoria**, o produto deve aparecer nela (e não mais na categoria anterior).

---

### 🥫 Cenário 2: Gestão de Estoque na Despensa (Adição, Busca, Edição e Consumo)

**Objetivo:** Validar todos os modos de inclusão de produtos no estoque da despensa (via código de barras, busca no catálogo e cadastro de novo produto durante a inserção), além de testar a busca, edição de estoque e o fluxo de consumo até a baixa completa sem perda cadastral no catálogo.

---

#### 2.1 Adicionar à Despensa via Leitor de Código de Barras
1. Acesse a aba **Minha Despensa**.
2. Localize e clique no botão **`Adicionar à Despensa`**.
3. No *Bottom Sheet* de seleção que se abre, clique na opção **`Escanear código de barras`**.
4. Com o scanner aberto, aponte a câmera para o código de barras do produto.
5. **O que validar:**
   - O aplicativo deve escanear e reconhecer o produto cadastrado.
   - Um novo *Bottom Sheet* deve se abrir automaticamente trazendo as informações do produto identificado.
6. No painel de entrada de estoque, preencha:
   - **Quantidade** do produto a ser estocada.
   - **Validade** (experimente os botões rápidos de atalho: **`7d`**, **`15d`**, **`30d`**, **`60d`** ou seleção manual de data).
   - **Número do Lote**.
7. Clique no botão **`Adicionar à Despensa`**.
8. **O que validar:**
   - As páginas/painéis abertos devem se fechar automaticamente, retornando à tela da **Minha Despensa**.
   - O produto deve ser exibido imediatamente na lista da despensa com as quantidades e prazos cadastrados.

#### 2.2 Busca e Edição do Item na Despensa
1. Na tela de **Minha Despensa**, utilize o campo de busca:
   - Digite o nome do produto adicionado e verifique se ele é filtrado com precisão.
2. Clique sobre o item na despensa para abrir seus detalhes ou selecione a opção de **Editar**.
3. Modifique os dados de estoque (ex.: ajuste a quantidade ou lote/validade).
4. Salve a alteração.
5. **O que validar:**
   - O item deve refletir as informações atualizadas na listagem da despensa.

---

#### 2.3 Adicionar à Despensa a partir do Catálogo (`Buscar no Catálogo`)
1. Na tela **Minha Despensa**, clique no botão **`Adicionar à Despensa`**.
2. No *Bottom Sheet*, selecione a opção **`Buscar no Catálogo`**.
3. Escolha um produto já cadastrado na listagem do catálogo.
4. Preencha os dados de estoque (Quantidade, Validade e Lote).
5. Clique no botão **`Adicionar à Despensa`**.
6. **O que validar:**
   - O *Bottom Sheet* fecha e o item selecionado aparece devidamente adicionado ao estoque da despensa.

---

#### 2.4 Cadastrar Novo Produto Diretamente da Despensa
1. Na tela **Minha Despensa**, clique no botão **`Adicionar à Despensa`**.
2. No *Bottom Sheet*, selecione a opção **`Cadastrar novo produto`**.
3. Preencha todas as informações do novo produto (nome, marca, categoria, unidade de medida, etc.) e salve.
4. Em seguida, preencha as informações de entrada de estoque (quantidade, validade, lote) e conclua a adição.
5. **O que validar:**
   - O novo produto deve ser adicionado ao estoque e aparecer imediatamente na tela de **Minha Despensa**.
   - Acesse a aba **Catálogo**: certifique-se de que o produto também foi devidamente registrado no catálogo geral para reaproveitamento futuro.

---

#### 2.5 Consumo de Itens e Baixa Total de Estoque
1. Na tela de **Minha Despensa**, clique sobre um item em estoque para abrir seus detalhes.
2. Role a tela para baixo até localizar o botão **`Consumir`**.
3. Clique sucessivamente no botão **`Consumir`** até esgotar a quantidade total do produto (consumir tudo).
4. Volte à listagem da **Minha Despensa**.
5. **O que validar:**
   - O item deve desaparecer completamente da listagem da despensa (estoque zerado).
   - Navegue até a aba **Catálogo**: verifique se o produto **permanece cadastrado no catálogo geral**, confirmando que o consumo dá baixa apenas no estoque da despensa sem deletar o cadastro do produto.

---

### 🛒 Cenário 3: Criação de Listas de Compras

**Contexto e Compatibilidade:** O aplicativo permite criar listas de compras de formas flexíveis (texto livre, código de barras e catálogo). Esses métodos são **100% compatíveis e interoperáveis**, permitindo que itens de diferentes origens coexistam na mesma lista e alimentem o Catálogo e o Estoque da Despensa após a finalização da compra.

---

#### 3.1 Tipo 1: Compra Direta (Configuração de Orçamento e Adição Multimodal)

**Objetivo:** Iniciar uma sessão de compra direta sem planejamento prévio, configurar nome e teto de gastos da lista e preencher o carrinho combinando itens via leitor de código de barras, busca no catálogo e texto livre simples.

##### 1. Iniciar Compra Direta e Ajustar Metadados
1. Acesse a aba **Listas de Compras** (ícone de carrinho na barra de navegação).
2. Clique no botão de destaque **`Compra Direta`**.
3. O Assistente de Compras abrirá a sessão imediata.
4. **Editar Nome da Lista:**
   - Toque no título da lista no topo da tela.
   - Um diálogo de edição será exibido. Digite um novo nome personalizado (ex.: *"Compra Rápida de Sábado"*) e confirme.
5. **Editar Orçamento (Teto de Gastos):**
   - No painel financeiro (*dashboard*) ou no diálogo de edição, toque no valor do orçamento.
   - Defina um teto orçamentário (ex.: *R$ 200,00*) e salve.
6. **O que validar:**
   - O título no topo e o painel financeiro devem refletir imediatamente o novo nome e o limite orçamentário configurado.

##### 2. Adicionar Itens Combinando os Três Formatos (`+ Adicionar`)
No rodapé ou no centro da tela vazia, clique no botão **`+ Adicionar`** (ou `Adicionar Item`). O *Bottom Sheet* apresentará 3 opções de inclusão:

- **A. Adicionar via Código de Barras:**
  1. Selecione a opção **`Escanear código de barras`**.
  2. Aponte a câmera para o código de um produto da sua despensa ou casa.
  3. Preencha a quantidade e o preço unitário e confirme a inclusão.
  4. **O que validar:** O item aparece no carrinho com suas informações identificadas.

- **B. Adicionar via Catálogo:**
  1. Clique novamente em **`+ Adicionar`** e escolha **`Buscar no catálogo`**.
  2. Selecione um item da listagem de produtos previamente cadastrados no Catálogo.
  3. Preencha quantidade e preço e adicione à lista.
  4. **O que validar:** O produto do catálogo é listado no carrinho.

- **C. Adicionar como Texto Simples (Item Avulso):**
  1. Clique em **`+ Adicionar`** e escolha a opção de **`Texto simples`** (ou *Item avulso por texto*).
  2. Digite livremente o nome do produto (ex.: *"Pão Francês"* ou *"Cheiro Verde"*), defina quantidade e valor aproximado e confirme.
  3. **O que validar:** O item em texto simples é adicionado perfeitamente junto aos demais produtos.

##### 3. Validação Geral do Carrinho da Compra Direta
1. **O que validar:**
   - Todos os 3 itens (código de barras, catálogo e texto livre) devem estar visíveis e organizados na mesma lista sem qualquer conflito.
   - O painel financeiro superior deve calcular a soma dos itens cadastrados, exibindo o total acumulado e a porcentagem gasta em relação ao teto de gastos configurado.

### Cenário 4: No Supermercado (Assistente de Compras)
- Inicie a sessão no Assistente de Compras.
- Marque os itens conforme for colocando no carrinho e digite o preço que encontrou na gôndola.
- Veja o painel financeiro calcular o total da compra e o aviso de limite orçamentário.
- Clique em **"Finalizar Compra"**.

### Cenário 5: Conferindo a Despensa Pós-Compra (Estoque e Histórico)
- Acesse a aba **Despensa** e confira se os produtos comprados foram adicionados ao estoque automaticamente.
- Acesse a aba **Catálogo** e clique no produto para conferir o histórico de preços gerado pela compra.

---

## 🐛 Como Relatar um Problema ou Sugestão

Se você encontrar um comportamento estranho, tela travando, texto cortado ou tiver uma ideia de melhoria:
1. Anote o que aconteceu e o modelo do seu celular.
2. Tire um print ou grave a tela.
3. Abra uma issue no nosso [GitHub Issues](https://github.com/bitlabbr/Minha-Despensa/issues) ou envie diretamente para o desenvolvedor.
