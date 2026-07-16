

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Alterar o "lado" dos inventários slotless de uma ‘string’ para uma Enum.

## Atualizações

- Alterar para o mod jogar *todos* os slots no inventário principal para slotless storage, e fazer com que os slots livres apenas se mantenham livres *caso* o usuário insira um item lá dentro *diretamente*. O slot então permanece lockado apenas enquanto aquele stack especifico não for esvaziado.

## Bugs

- Consertar barrinha de durabilidade clippando para dentro da slotless area =w=' (Baixa prioridade)
- Consertar comando de clear e gamerule de keep inventory não funcionando
- Consertar inventário slotless não aparecendo no modo criativo
- Consertar ao passar todos os itens de um container para um inventário, eles estarem indo para slots que não deviam também, além de não puxar *todos* os itens (Espaço é infinito afinal, deve puxar todos)