
# Rede Neural MLP -> Multilayer Perceptron

Implementação de uma rede neural perceptron que pode ter várias camadas.
Desenvolvimento realizado pelo grupo de estudantes Gabriel Pissinin, Matheus Amaral e Matheus Antonucci. Para a disciplina de Inteligência Artificial 1 da faculdade Unoeste (Universidade do Oeste Paulista)-> FIPP (Faculdade de Informática de Presidente Prudente)

Uma rede neural multilayer perceptron possui diversas aplicações, dentre elas podemos citar o reconhecimento de padrões através de valores numéricos de treinamento obtidos previamente.

Abaixo a explicação de como executar o programa.
## Conhecimentos e Enciclopédia

- [Visualização Perceptron](https://vinizinho.net/projects/perceptron-viz/)
- [Simulador de Rede Neural MLP](https://playground.tensorflow.org/#activation=tanh&batchSize=10&dataset=circle&regDataset=reg-plane&learningRate=0.03&regularizationRate=0&noise=0&networkShape=4,2&seed=0.64365&showTestData=false&discretize=false&percTrainData=50&x=true&y=true&xTimesY=false&xSquared=false&ySquared=false&cosX=false&sinX=false&cosY=false&sinY=false&collectStats=false&problem=classification&initZero=false&hideText=false)
- [Vídeo explicativo BackPropagation parte 1](https://www.youtube.com/watch?v=L3lbrdCTK9w)
- [Vídeo explicativo BackPropagation parte 2](https://www.youtube.com/watch?v=nineTA2uYKA)
- [Vídeo explicativo BackPropagation parte 3](https://www.youtube.com/watch?v=eQakqjYKk94)
- [Vídeo explicativo BackPropagation parte 4](https://www.youtube.com/watch?v=CejYlFHP_JY)
- [Forward Propagation](https://www.geeksforgeeks.org/deep-learning/what-is-forward-propagation-in-neural-networks/)
## Rode localmente

- Instale o Intellij com esse link:
    - [Download do Intellij](https://www.jetbrains.com/idea/download/?section=linux)

- Clone o repositório:
```bash
  git clone https://github.com/GabrielPissininMenossi/RedeNeuralMLP.git
```

- Vá para o arquivo MainApplication.java no projeto, caminho:
```bash
  /src/main/java/fipp/muscleandiq/redeneuralmlp/MainApplication.java
```

- Rode o projeto com o atalho "Shift+F10" ou aperte para rodar com o mouse

- Abrindo arquivos:
    - Arquivos disponíveis na pasta: 
        ```bash
            /src/main/resources/arquivos
        ```
- Rode o treinamento depois de subir o arquivo de teste
- Rode os testes depois de subir o arquivo de teste

## Como usar

📁 1. Carregar Arquivo de Treino e o de Testes

- Sem carregar o arquivo, não é possível iniciar o TREINAMENTO.

- Sem carregar o arquivo, também não é possível realizar TESTES.

📁 1 (OPCIONAL). Carregar apenas um arquivo, particionando o percentual de treino e de Testes
- Ao carregar um arquivo, o sistema habilita a função de particionamento.

- Você escolhe a porcentagem de linhas destinadas ao treinamento.

- O restante (para fechar 100%) será automaticamente considerado como linhas de teste.
  - Exemplo:
    Se você escolher 70% para treino → 30% serão usados para teste.

⚙️ 3. Treinar a Rede MLP

- Após o arquivo ser carregado, você pode iniciar o treinamento.

- Regras importantes:

  - Não é possível treinar antes de carregar o arquivo de teste.

  - Durante o treinamento, a rede pode atingir um estado de platô (estagnação do aprendizado). Quando isso ocorre, o sistema exibirá 3 opções:

    - Continuar o treinamento com a taxa de aprendizado atual.

    - Reduzir a taxa de aprendizado em 10% e continuar.

    - Interromper o treinamento imediatamente.

      Essas opções permitem melhor controle sobre o processo de otimização.

🧪 4. Testar a Rede

- Após treinar a rede, você poderá realizar testes com os dados de teste.

- Regras importantes:

  - Não é possível testar sem:

    - Ter carregado o arquivo de teste.

    - Ter realizado o treinamento previamente.

  - Se você alterar a função de transferência, não poderá testar com essa nova função até treinar novamente usando ela.

🔄 5. Mudança de Função de Transferência

- O sistema permite testar diferentes funções de ativação.

- Contudo, não é permitido testar usando uma função de transferência diferente daquela usada no último treinamento.

  Portanto, ao alterar a função, você deve treinar novamente antes de testar.
## Autores

- [Gabriel Pissinin Menossi](https://github.com/GabrielPissininMenossi)
- [Matheus Amaral Matos](https://github.com/MthsAmaral)
- [Matheus Antonucci Mendonça](https://github.com/matheusmen1)

## Suporte

Para suporte, acessar os contatos disponíveis no link:
- [Link com contatos](https://linktr.ee/gabrielpissinin?utm_source=ig&utm_medium=social&utm_content=link_in_bio)
