

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Remover a utilização de UUIDs nos inventários slotless (Identificar eles pelo "lado")
- Alterar o "lado" dos inventários slotless de uma string para uma Enum.

## Atualizações

- Conectar o inventário slotless ao ciclo de vida do inventário (Tick updates, clear, clean, etc)
- Fazer o slotless funcionar entre mortes e dimensões (Ser perdido e ser restaurado)
- Implementar dropar direto do inventário slotless
- Implementar jogar direto pra hotbar (apretando 1-9) direto do inventário slotless

## Bugs

- Consertar a barrinha de durabilidade não aparecendo na area slotless