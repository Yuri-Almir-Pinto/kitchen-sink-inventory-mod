
## Polimentos finos
- Implementar compatibilidade com o livro de receitas do vanilla 
- Tentar implementar compatibilidade com JEI e similares (para poder usar U e R direto no slotless)
- Implementar pixel picking (Ver como o subpocket fez) (Baixa prioridade. É complicado...)
- Implementar permitir mover multiplos itens ao mesmo tempo, se estiverem sobrepostos, através de alguma keybind.
- Adicionar usar o rolamento do mouse para dar quick move de itens individuais de e para o slotless storage
- Adicionar usar o rolamento do mouse para baixo enquanto move um item para colocar esse item abaixo do item em que o mouse está em cima.
- Substituir o uso da textura da GUI da shulker box na GUI slotless por uma textura própria

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Alterar o "lado" dos inventários slotless de uma ‘string’ para uma Enum.
- Atualizar os arquivos do NeoForge e Fabric para não usarem o nome do example mod.
- Alterar a versão para ser um beta ou alfa e não release (1.0.0 o caralho)
- Remover o uso do mixin de Redirect em HandledScreenMixin para o método drawMouseoverTooltip (É um mixin perigoso)

## Melhorias

- Alterar para o mod jogar *todos* os slots no inventário principal para slotless storage, e fazer com que os slots livres apenas se mantenham livres *caso* o usuário insira um item lá dentro *diretamente*. O slot então permanece lockado apenas enquanto aquele stack especifico não for esvaziado.
- Implementar GUI no inventário para configuração e ações especiais:
  * Botão a esquerda da slotless area que permite dar resize na slotless area, escondendo ou mostrando slots a direita
- Implementar sincronização das modificações de um container slotless (para que multiplos jogadores em multiplayer consigam usar o container ao mesmo tempo):
  * ResetPositionsSlotless
- Decidir se eu vou criar containers próprios do mod para a block entity slotless, ou se eu vou permitir tornar blocos vanilla slotless...
- Remover o markDirty to inventário, e jogar essas responsabilidade para os packets, para evitar com que markDirty seja chamado multiplas vezes, já que ele pode dar trigger em block updates.
- Ver o que fazer quanto a transferência direta de slotless item de uma área para outra (Estava pensando em fazer uma scissor the permite mostrar o item de uma área em outra área, mas pelo visto isso não da certo).

## Bugs

- O access violation não era causado pelo hotswap. Fazer uma cópia do inventário slotless uma vez a cada mutação e renderizar apenas essa snapshop para evitar erros de concorrência.
- Botões do slotless GUI estão aparecendo no modo criativo, o que não deve acontecer.
- Consertar a randomização da posição dos slotless items não sendo centralizada em slotless areas diferentes de '2746'.
- Ao segurar algum dos botões da hotbar para dar swap constante em um slotless storage, o item pode se desincronizar com o servidor, fazendo ele sumir ou duplicar para o cliente (Não parece fazer nada ao servidor). Nada parece acontecer se segurar F.