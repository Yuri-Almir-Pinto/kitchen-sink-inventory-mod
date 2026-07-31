
## Polimentos finos
- Adicionar usar o rolamento do mouse para dar quick move de itens individuais de e para o slotless storage (Enquanto segura shift)
- Adicionar usar o rolamento do mouse para selecionar itens que estão abaixo em uma pilha (Padrão)
- Adicionar uma pia como slotless storage, hahaha, I'm so **funni**
- Adicionar descrição nos botões da área slotless

## Refatoração
- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.

## Melhorias
- Alterar para o mod jogar *todos* os slots no inventário principal para slotless storage, e fazer com que os slots livres apenas se mantenham livres *caso* o usuário insira um item lá dentro *diretamente*. O slot então permanece lockado apenas enquanto aquele stack especifico não for esvaziado.
- Implementar GUI no inventário para configuração e ações especiais:
  * Botão a esquerda da slotless area que permite dar resize na slotless area, escondendo ou mostrando slots a direita


## Bugs
- Ao segurar algum dos botões da hotbar para dar swap constante em um slotless storage, o item pode se desincronizar com o servidor, fazendo ele sumir ou duplicar para o cliente (Não parece fazer nada ao servidor). Nada parece acontecer se segurar F.
- Nitpick: ao pegar um item de uma área slotless com um click do mouse, as vezes o itemstack no mouse está piscando por um frame.
- O minecraft tem um limite de tamanho de packet e NBT do jogador de cerca de 2MB. O mod permite fácil superar isso. Alterar onde guarda o inventário slotless para o persistent storage, e referenciar ele usando um UUID. Além disso, para os packets de syncinc de inventário, quebrar eles em packets menores com base no tamanho em bytes do que vai ser enviado.