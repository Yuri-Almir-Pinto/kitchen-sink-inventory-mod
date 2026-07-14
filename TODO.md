

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Alterar o "lado" dos inventários slotless de uma string para uma Enum.

## Atualizações

- Implementar dropar direto do inventário slotless
- Implementar jogar direto pra hotbar (apretando 1-9) direto do inventário slotless
- Alterar para o mine jogar itens pegos diretos no inventário slotless para evitar spill over por quantidade excessiva de item
- Adicionar funcionalidade para os itens irem direto para o slotless quando pegando do chão, se existirem no slotless ao invés da hotbar

## Bugs

- Consertar a barrinha de durabilidade não aparecendo na area slotless
- Consertar comando de clear e gamerule de keep inventory não funcionando
- Consertar inventário slotless não aparecendo no modo criativo