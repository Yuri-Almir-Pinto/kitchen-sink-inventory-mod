
## Polimentos finos
- Implementar compatibilidade com o livro de receitas do vanilla 
- Tentar implementar compatibilidade com JEI e similares (para poder usar U e R direto no slotless)
- Implementar pixel picking (Ver como o subpocket fez) (Baixa prioridade. É complicado...)
- Implementar permitir mover multiplos itens ao mesmo tempo, se estiverem sobrepostos, através de alguma keybind.
- Adicionar usar o rolamento do mouse para dar quick move de itens individuais de e para o slotless storage (Enquanto segura shift)
- Adicionar usar o rolamento do mouse para selecionar itens que estão abaixo em uma pilha (Padrão)
- Substituir o uso da textura da GUI da shulker box na GUI slotless por uma textura própria
- Adicionar uma pia como slotless storage, hahaha, I'm so **funni**
- Adicionar descrição nos botões da área slotless
- Adicionar uma área de scissor ao redor do inventário quando estiver movendo um item (para não ser possível visualmente arrastar algo pra fora do inventário, o que pode ser confuso)

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Atualizar os arquivos do NeoForge e Fabric para não usarem o nome do example mod.
- Alterar a versão para ser um beta ou alfa e não release (1.0.0 o caralho)
- Remover o uso do mixin de Redirect em HandledScreenMixin para o método drawMouseoverTooltip (É um mixin perigoso)

## Melhorias

- Alterar para o mod jogar *todos* os slots no inventário principal para slotless storage, e fazer com que os slots livres apenas se mantenham livres *caso* o usuário insira um item lá dentro *diretamente*. O slot então permanece lockado apenas enquanto aquele stack especifico não for esvaziado.
- Implementar GUI no inventário para configuração e ações especiais:
  * Botão a esquerda da slotless area que permite dar resize na slotless area, escondendo ou mostrando slots a direita


## Bugs

- Ao segurar algum dos botões da hotbar para dar swap constante em um slotless storage, o item pode se desincronizar com o servidor, fazendo ele sumir ou duplicar para o cliente (Não parece fazer nada ao servidor). Nada parece acontecer se segurar F.
- Nitpick: ao pegar um item de uma área slotless com um click do mouse, as vezes o itemstack no mouse está piscando por um frame.