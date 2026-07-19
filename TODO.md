

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Alterar o "lado" dos inventários slotless de uma ‘string’ para uma Enum.

## Melhorias

- Alterar para o mod jogar *todos* os slots no inventário principal para slotless storage, e fazer com que os slots livres apenas se mantenham livres *caso* o usuário insira um item lá dentro *diretamente*. O slot então permanece lockado apenas enquanto aquele stack especifico não for esvaziado.
- Permitir dar shift click no slotless com um item no cursor para mover itens em massa.
- Remover o uso do mixin de Redirect em HandledScreenMixin para o método drawMouseoverTooltip (É um mixin perigoso)

## Bugs

- Consertar barrinha de durabilidade clippando para dentro da slotless area =w=' (Baixa prioridade)