

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
- Consertar item invisível que fica quando puxando um item do slotless storage para a hotbar usando os números (1-9). Isso impede de pegar um item até colocar e tirar o item tirado novamente (provavelmente porque o item vazio então some).
- Consertar encher potes de água resultar em dois itens sendo adicionados no inventário
- Identificar e consertar possível incompatibilidade com algum mod do modpack de teste, onde shift + click para quick move do slotless storage para o inventário não funciona (Do slotless storage para um container funciona) (Aparentemente está acontecendo no mundo de testes agora também. Que bom, significa que não é incompatibilidade e da para testar de boa)