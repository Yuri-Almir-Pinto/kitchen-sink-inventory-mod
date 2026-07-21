
## Polimentos finos
- Implementar compatibilidade com o livro de receitas do vanilla 
- Tentar implementar compatibilidade com JEI e similares (para poder usar U e R direto no slotless)
- Implementar pixel picking (Ver como o subpocket fez) (Baixa prioridade. É complicado...)
- Implementar permitir mover multiplos itens ao mesmo tempo, se estiverem sobrepostos, através de alguma keybind.
- Adicionar usar o rolamento do mouse para dar quick move de itens individuais de e para o slotless storage
- Adicionar usar o rolamento do mouse para baixo enquanto move um item para colocar esse item abaixo do item em que o mouse está em cima.

## Refatoração

- Atualizar o packet de mover item para receber o index, x e y no lugar do SlotlessItem completo.
- Alterar o "lado" dos inventários slotless de uma ‘string’ para uma Enum.
- Atualizar os arquivos do NeoForge e Fabric para não usarem o nome do example mod.
- Alterar a versão para ser um beta ou alfa e não release (1.0.0 o caralho)
- Remover o uso do mixin de Redirect em HandledScreenMixin para o método drawMouseoverTooltip (É um mixin perigoso)

## Melhorias

- Alterar para o mod jogar *todos* os slots no inventário principal para slotless storage, e fazer com que os slots livres apenas se mantenham livres *caso* o usuário insira um item lá dentro *diretamente*. O slot então permanece lockado apenas enquanto aquele stack especifico não for esvaziado.
- Implementar GUI no inventário para configuração e ações especiais:
  * Botão superior esquerdo que abre outros botões na lateral esquerda e em cima (Nenhum outro botão aparece sem esse ter sido pressionado)
  * Botão para resetar itens fora da grid para o centro da grid (caso clique com o shift apertado, restaura *todos* os itens para o centro da grid)
  * Botão a esquerda da slotless area que permite dar resize na slotless area, escondendo ou mostrando slots a direita
- Implementar slotless storage para containers (Implementar como blocos próprios, para evitar problemas...)

## Bugs

- Consertar barrinha de durabilidade clippando para dentro da slotless area =w=' (Baixa prioridade)
- Shift click de um container não ta jogando itens pra off-hand quando tem. Verificar se esse é o comportamento vanilla, e se for, consertar (Notar que no container, o slot da off-hand não aparece, então provavelmente não é o comportamento vanilla). Se não, foda-se kk
- O access violation não era causado pelo hotswap. Fazer uma cópia do inventário slotless uma vez a cada mutação e renderizar apenas essa snapshop para evitar erros de concorrência.