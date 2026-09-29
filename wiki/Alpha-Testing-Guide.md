# 🧪 Guia do Testador Alpha (Release 1.0.0-alpha)

Se você recebeu o pacote de teste do **Minha Despensa**, este guia explica como instalar e quais cenários você pode experimentar para nos ajudar a encontrar melhorias!

---

## 📲 Como Instalar no Android

1. Baixe o arquivo **`composeApp-release.apk`** da aba [Releases](https://github.com/bitlabbr/Minha-Despensa/releases).
2. No seu celular Android, abra o arquivo baixado.
3. Se for solicitado, autorize a instalação de fontes desconhecidas para o navegador/gerenciador de arquivos.
4. Conclua a instalação e abra o aplicativo **Minha Despensa**.

---

## 🎯 Roteiro de Testes Recomendado

Experimente realizar o "caminho feliz" do app no seu dia a dia:

### Cenário 1: Cadastro de Produtos
- Tente cadastrar um produto novo usando a câmera para escanear o código de barras de um item da sua casa.
- Tente cadastrar um produto sem código de barras (ex: hortifrúti).
- Adicione uma foto da câmera ou galeria.

### Cenário 2: Montando sua Lista
- Crie uma lista de compras rápida digitando itens linha a linha (ex: `arroz`, `leite`, `café`).
- Crie uma lista planejada com teto de gastos (ex: R$ 150,00) selecionando produtos do seu catálogo.

### Cenário 3: No Supermercado (Assistente de Compras)
- Inicie a sessão no Assistente de Compras.
- Marque os itens conforme for colocando no carrinho e digite o preço que encontrou na gôndola.
- Veja o painel financeiro calcular o total da compra e o aviso de limite orçamentário.
- Clique em **"Finalizar Compra"**.

### Cenário 4: Conferindo a Despensa
- Acesse a aba **Despensa** e confira se os produtos comprados foram adicionados ao estoque automaticamente.
- Acesse a aba **Catálogo** e clique no produto para conferir o histórico de preços gerado pela compra.
- Faça o consumo de 1 unidade de um item na despensa.

---

## 🐛 Como Relatar um Problema ou Sugestão

Se você encontrar um comportamento estranho, tela travando, texto cortado ou tiver uma ideia de melhoria:
1. Anote o que aconteceu e o modelo do seu celular.
2. Tire um print ou grave a tela.
3. Abra uma issue no nosso [GitHub Issues](https://github.com/bitlabbr/Minha-Despensa/issues) ou envie diretamente para o desenvolvedor.
