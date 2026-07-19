

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Alterar o "lado" dos inventários slotless de uma ‘string’ para uma Enum.

## Melhorias

- Alterar para o mod jogar *todos* os slots no inventário principal para slotless storage, e fazer com que os slots livres apenas se mantenham livres *caso* o usuário insira um item lá dentro *diretamente*. O slot então permanece lockado apenas enquanto aquele stack especifico não for esvaziado.
- Remover o uso do mixin de Redirect em HandledScreenMixin para o método drawMouseoverTooltip (É um mixin perigoso)
- Implementar pixel picking (Ver como o subpocket fez) (Baixa prioridade. É complicado...)
- Implementar jogar item direto do inventário slotless para a offhand
- Implementar permitir mover multiplos itens ao mesmo tempo, se estiverem sobrepostos, através de alguma keybind.

## Bugs

- Consertar barrinha de durabilidade clippando para dentro da slotless area =w=' (Baixa prioridade)
- Consertar crafting jogando a quantidade errada de itens no slotless storage (no cliente)
- Consertar encher potes de água resultar em dois itens sendo adicionados no inventário