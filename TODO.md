
## Polimentos finos
- Implementar permitir mover multiplos itens ao mesmo tempo, se estiverem sobrepostos, através de alguma keybind.
- Adicionar usar o rolamento do mouse para dar quick move de itens individuais de e para o slotless storage (Enquanto segura shift)
- Adicionar usar o rolamento do mouse para selecionar itens que estão abaixo em uma pilha (Padrão)
- Adicionar uma pia como slotless storage, hahaha, I'm so **funni**
- Adicionar descrição nos botões da área slotless
- Considerar alterar o comportamento do shift click em um bloco slotless, para ao invés de mandar todo o item slotless de um container para o outro, mandar apenas um stack (E talvez segurar ctrl para mandar todo o item slotless)

## Refatoração
- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.

## Melhorias
- Alterar para o mod jogar *todos* os slots no inventário principal para slotless storage, e fazer com que os slots livres apenas se mantenham livres *caso* o usuário insira um item lá dentro *diretamente*. O slot então permanece lockado apenas enquanto aquele stack especifico não for esvaziado.
- Implementar GUI no inventário para configuração e ações especiais:
  * Botão a esquerda da slotless area que permite dar resize na slotless area, escondendo ou mostrando slots a direita


## Bugs
- Ao segurar algum dos botões da hotbar para dar swap constante em um slotless storage, o item pode se desincronizar com o servidor, fazendo ele sumir ou duplicar para o cliente (Não parece fazer nada ao servidor). Nada parece acontecer se segurar F.
- Nitpick: ao pegar um item de uma área slotless com um click do mouse, as vezes o itemstack no mouse está piscando por um frame.w
- Mod está mostrando e selecionando itens mesmo quando movendo um item com o mouse, e mostrando itens fantasma (Provavelmente o último que não está sendo renderizado)