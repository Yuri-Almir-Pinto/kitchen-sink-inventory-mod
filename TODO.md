
## Polimentos finos
- Implementar compatibilidade com o livro de receitas do vanilla 
- Tentar implementar compatibilidade com JEI e similares (para poder usar U e R direto no slotless)
- Implementar pixel picking (Ver como o subpocket fez) (Baixa prioridade. É complicado...)
- Implementar permitir mover multiplos itens ao mesmo tempo, se estiverem sobrepostos, através de alguma keybind.
- Adicionar usar o rolamento do mouse para dar quick move de itens individuais de e para o slotless storage
- Adicionar usar o rolamento do mouse para baixo enquanto move um item para colocar esse item abaixo do item em que o mouse está em cima.
- Substituir o uso da textura da GUI da shulker box na GUI slotless por uma textura própria
- Adicionar um item que guarda slotless items, para que quando o mod drope itens de um inventário slotless, ele drope apenas um item ao invés de um mar de itens que derruba um servidor

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Atualizar os arquivos do NeoForge e Fabric para não usarem o nome do example mod.
- Alterar a versão para ser um beta ou alfa e não release (1.0.0 o caralho)
- Remover o uso do mixin de Redirect em HandledScreenMixin para o método drawMouseoverTooltip (É um mixin perigoso)

## Melhorias

- Alterar para o mod jogar *todos* os slots no inventário principal para slotless storage, e fazer com que os slots livres apenas se mantenham livres *caso* o usuário insira um item lá dentro *diretamente*. O slot então permanece lockado apenas enquanto aquele stack especifico não for esvaziado.
- Implementar GUI no inventário para configuração e ações especiais:
  * Botão a esquerda da slotless area que permite dar resize na slotless area, escondendo ou mostrando slots a direita
- Remover o markDirty to inventário, e jogar essas responsabilidade para os packets, para evitar com que markDirty seja chamado multiplas vezes, já que ele pode dar trigger em block updates.
- Ver o que fazer quanto a transferência direta de slotless item de uma área para outra (Estava pensando em fazer uma scissor the permite mostrar o item de uma área em outra área, mas pelo visto isso não da certo).

## Bugs

- O access violation não era causado pelo hotswap. Fazer uma cópia do inventário slotless uma vez a cada mutação e renderizar apenas essa snapshop para evitar erros de concorrência.
- Botões do slotless GUI estão aparecendo no modo criativo, o que não deve acontecer.
- Ao segurar algum dos botões da hotbar para dar swap constante em um slotless storage, o item pode se desincronizar com o servidor, fazendo ele sumir ou duplicar para o cliente (Não parece fazer nada ao servidor). Nada parece acontecer se segurar F.
- Pegar muitas water bottles do chão está dando spill over para slotes que não deveria. InsertStack to inventário é quem está inserindo nesses slots. Verificar porque ele foi implementado daquela forma.
- Abrir o slotless block entity no criativo não funciona.