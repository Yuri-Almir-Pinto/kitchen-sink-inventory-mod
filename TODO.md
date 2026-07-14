

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Alterar o "lado" dos inventários slotless de uma string para uma Enum.

## Atualizações

- Implementar dropar direto do inventário slotless
- Implementar jogar direto pra hotbar (apretando 1-9) direto do inventário slotless
- Alterar para o mine jogar itens pegos diretos no inventário slotless para evitar spill over por quantidade excessiva de item
- Adicionar funcionalidade para os itens irem direto para o slotless quando pegando do chão, se existirem no slotless ao invés da hotbar
- Fazer com que o reposicionamento seja diferenciado do click com tanto a distância do click e release (atual), quanto pelo tempo levado para dar release do mouse (Não implementado)
- Fazer com que, ao dar shift-click nos slots livres do inventário com espaço na hotbar, o item vá para a hotbar ao invés do slotless storage
- Fazer com que shift-click da fornalha (E semelhantes) vá para os slotless storage se houver item do mesmo tipo lá, e para a hotbar se não houver.

## Bugs

- Consertar barrinha de durabilidade clippando para dentro da slotless area =w=' (Baixa prioridade)
- Consertar comando de clear e gamerule de keep inventory não funcionando
- Consertar inventário slotless não aparecendo no modo criativo
- Consertar não sendo possível craftar com shift click, e não sendo possível clicar rapidamente para craftar.
- O tooltip de outros mods não aparece quando o item está no slotless storage