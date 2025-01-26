# Game of Life
 

**Disciplina**: FGA0210 - PARADIGMAS DE PROGRAMAÇÃO - T01 <br>
**Nro do Grupo**: 03 <br>
**Paradigma**: SMA <br>

## Alunos
| Matrícula | Aluno                             |
| --------- | --------------------------------- |
| 190124997 | Amanda Nobre                      |
| 200017772 | Fellipe Pereira da Costa Silva    |
| 190028122 | Gabriel Sabanai Trindade          |
| 200037994 | Guilherme Barbosa Ferreira        |
| 190029731 | Ingrid da Silva Carvalho          |
| 190046848 | Laís Portela de Aguiar            |
| 221007653 | Luciano Ricardo da Silva Junior   |
| 190033681 | Luiz Henrique Fernandes Zamprogno |
| 200025449 | Natan Tavares Santana             |
| 200042416 | Pablo Christianno Silva Guedes    |


## Sobre 
<!-- Descreva o seu projeto em linhas gerais. 
Use referências, links, que permitam conhecer um pouco mais sobre o projeto.
Capriche nessa seção, pois ela é a primeira a ser lida pelos interessados no projeto. -->

Esse projeto corresponde a terceira entrega do Grupo3.

Seu principal objetivo é implementar o jogo Game of Live inventado pelo matematico John Conway da universidade de Cambridge



## Screenshots
<!-- Adicione 2 ou mais screenshots do projeto em termos de interface e/ou funcionamento. -->

## Instalação 
**Linguagens**: Java<br>
**Tecnologias**: Jade<br> 

<!-- Descreva os pré-requisitos para rodar o seu projeto e os comandos necessários.
Insira um manual ou um script para auxiliar ainda mais.
Gifs animados e outras ilustrações são bem-vindos! -->

1. Instale o Java, que pode ser baixado [aqui](https://www.oracle.com/java/technologies/downloads/).
2. Instale o Jade 4.6.0 ou superior, que pode ser baixado [aqui](https://jade.tilab.com/download/jade/).
3. Certifique-se de configurar as variaveis de ambiente do Java e do Jade.
4. Clone esse repositorio com o comando `git clone https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife.git`.

#### **Execução com o Eclipse**
1. Abra o projeto GameOfLife no Eclipse.
2. Clique com o botão direito no projeto e vá em **Run As > Run Configurations**.
3. No painel esquerdo, selecione **Java Application** e clique em **New Configuration**.
4. Na aba **Main**, no campo **Project**, selecione `2024.2_G3_SMA_GameOfLife`.
5. Na aba **Main** no campo **Main Class**, digite `jade.Boot`.
6. Na aba **Arguments**, em **Program arguments**, digite `-gui game:src.GameUIAgent`.
7. Clique em **Apply** e em seguida **Run**.

#### **Execução com o VSCode**
1. Abra o projeto GameOfLife no VSCode.
2. Certifique-se de ter a extensão `Extension Pack for Java` instalada.
3. Clique com o botão esquerdo no arquivo `SetupAgent.java` e clique em `Run Java`

## Uso 

<!-- Explique como usar seu projeto.
Procure ilustrar em passos, com apoio de telas do software, seja com base na interface gráfica, seja com base no terminal.
Nessa seção, deve-se revelar de forma clara sobre o funcionamento do software. -->


## Vídeo
<!-- Adicione 1 ou mais vídeos com a execução do projeto.
Procure: 
(i) Introduzir o projeto;
(ii) Mostrar passo a passo o código, explicando-o, e deixando claro o que é de terceiros, e o que é contribuição real da equipe;
(iii) Apresentar particularidades do Paradigma, da Linguagem, e das Tecnologias, e
(iV) Apresentar lições aprendidas, contribuições, pendências, e ideias para trabalhos futuros.
OBS: TODOS DEVEM PARTICIPAR, CONFERINDO PONTOS DE VISTA.
TEMPO: +/- 15min -->


## Participações

Aqui está a tabela com os nomes em ordem alfabética:

| Nome do Membro    | Contribuição                                                                                                                                                | Significância da Contribuição para o Projeto (Excelente/Boa/Regular/Ruim/Nula) | Comprobatórios                                                                                  |
| ----------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------- |
| Amanda Nobre      |        Desenvolvimento da camada de controle da aplicação, responsável por receber as regras de regras de negocio das informações dos 3 agentes e da manipular de forma que respeite a funcionalidade do jogo. Além disso, foi feita a complementada a respectiva documentação.                                                                                                                                                       |           Excelente                                                                     | [Nome do PR](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/XX)           |
| Fellipe Pereira   |                                                                                                                                                             |                                                                                | [Cellagent](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/8)           |
| Gabriel Sabanai   |                    Desenvolvimento da camada de controle da aplicação, responsável por receber as regras de regras de negocio das informações dos 3 agentes e da manipular de forma que respeite a funcionalidade do jogo. Além disso, foi feita a complementada a respectiva documentação.                                                                                                                                           |           Excelente                                                                     | [Nome do PR](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/XX)           |
| Guilherme Barbosa |                      Desenvolvimento da camada de controle da aplicação, responsável por receber as regras de regras de negocio das informações dos 3 agentes e da manipular de forma que respeite a funcionalidade do jogo. Além disso, foi feita a complementada a respectiva documentação.                                                                                                                                         |                Excelente                                                                | [Nome do PR](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/XX)           |
| Ingrid Carvalho   |                                                                                                                                                             |                                                                                | [Nome do PR](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/XX)           |
| Lais Portela      |                                                                                                                                                             |                                                                                | [Nome do PR](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/XX)           |
| Luciano Ricardo   | Estruturação da base de agentes, documentação inicial do diagrama de comunicação entre agentes, criação da Interface Gráfica e estados iniciais dos agentes | Excelente                                                                      | [Esqueleto dos Agentes](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/5) |
| Luiz Henrique     | Implementamos agentes que representam células no *Game of Life*, atendendo aos critérios de aceitação estabelecidos: os agentes possuem dois estados (*vivo* e *morto*), definidos por um booleano `isAlive`, e mudam de estado conforme as regras do *Game of Life*. Inicialmente, as células começam no estado de *morto* e são capazes de se comunicar com seus vizinhos (*CellAgent*) para obter seus estados atuais, além de enviar seus estados atuais para eles. Os agentes também podem calcular e enviar seus próximos estados, baseados nos estados dos vizinhos, para o *ControllerAgent*. Além disso, atualizamos a documentação do projeto. |          | [Cellagent](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/8)           |
| Natan Tavares     | Estruturação da base de agentes, documentação inicial do diagrama de comunicação entre agentes, criação da Interface Gráfica e estados iniciais dos agentes | Excelente                                                                      | [Esqueleto dos Agentes](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/5) |
| Pablo Christianno |                                                                                                                                                             |                                                                                | [Cellagent](https://github.com/UnBParadigmas2024-2/2024.2_G3_SMA_GameOfLife/pull/8)   |

## Outros 
Abaixo consta os relatos de cada membro da equipe, no que se diz respeito a lições aprendidas, contribuições, percepções, fragilidades e trabalhos futuros.

<details><summary>Amanda Nobre</summary>

### Lições Aprendidas  
- 

### Percepções  
- 

### Contribuições e Fragilidades  
- 

### Trabalhos Futuros  
- 

</details>

<details><summary>Fellipe Pereira</summary>

### Lições Aprendidas  
- 

### Percepções  
- 

### Contribuições e Fragilidades  
- 

### Trabalhos Futuros  
- 

</details>

<details><summary>Gabriel Sabanai</summary>

### Lições Aprendidas  
- 

### Percepções  
- 

### Contribuições e Fragilidades  
- 

### Trabalhos Futuros  
- 

</details>

<details><summary>Guilherme Barbosa</summary>

### Lições Aprendidas  
- 

### Percepções  
- 

### Contribuições e Fragilidades  
- 

### Trabalhos Futuros  
- 

</details>

<details><summary>Ingrid Carvalho</summary>

### Lições Aprendidas  
- 

### Percepções  
- 

### Contribuições e Fragilidades  
- 

### Trabalhos Futuros  
- 

</details>

<details><summary>Lais Portela</summary>

### Lições Aprendidas  
- 

### Percepções  
- 

### Contribuições e Fragilidades  
- 

### Trabalhos Futuros  
- 

</details>

<details><summary>Luciano Ricardo</summary>

### Lições Aprendidas  
- Aprendi muito sobre SMA e principalmente sobre a forma como eles se comunicam entre si. 

### Percepções  
- Achei um paradigma bastante dificil, já que um desafio simples resulta em um programa bem complexo. Porém me acho que a complexidade se paga na robustez e na qualidade do software.

### Contribuições e Fragilidades  
- Ajudei principalemte no design de interface gráfica e na construção da estrutura dos agentes. Dessa forma foi um grande desafio ficar pensando em como os agentes deveriam se comunicam de forma que o objetivo do projeto fosse alcançado. Minha maior dificulda foi entender os CyclicBehaviours e como eles ficam esperando por uma mensagem para executar uma ação.

### Trabalhos Futuros  
- Acho que poderiamos implementar uma funcionadade de passos que o jogo avance apenas um ciclo por vez.

</details>

<details><summary>Luiz Henrique</summary>

### Lições Aprendidas 

A implementação das regras de transição de estado foi mais desafiadora do que o esperado, já que envolvia cálculos baseados em estados dinâmicos e interação contínua com o controlador, nesse processo pude me desenvolver bastante e aprender mais sobre o paradigma.

### Percepções  
Percebi que a comunicação entre os agentes em sistemas distribuídos é um aspecto crucial para o sucesso do projeto, pois a troca de informações precisa ser rápida e confiável para garantir que as regras do *Game of Life* sejam aplicadas corretamente. Também entendi a importância de testar os estados iniciais e as transições entre estados de maneira eficiente, para evitar falhas lógicas que poderiam comprometer a execução do sistema. 

### Contribuições e Fragilidades  
Minha contribuição foi a implementação do Cellagent, garantindo que cada célula pudesse calcular e enviar seu próximo estado com base nas regras do *Game of Life* e nos estados dos vizinhos. 

### Trabalhos Futuros  
Futuras melhorias podem ser focadas na otimização da comunicação entre os agentes, buscando otimizar as trocas de dados. 

</details>

<details><summary>Natan Tavares</summary>

### Lições Aprendidas  
- Aprendi a estruturar um ambiente básico de multi agentes e a forma como devem comunicar entre si. Tive que ir atrás de como representar essa comunicação em forma de diagramas para os outros membros do grupo também

### Percepções  
- Achei complicado fazer o esqueleto do projeto e pensar o comportamento que cada agente deve ter. Mesmo estando confiante da estrutura feita, muito provavelmente terá problemas na hora da programação que irão revelar necessidades de mudanças na comunicação entre agentes

### Contribuições e Fragilidades  
- Acredito que usamos comportamentos e estruturas de mensagens bem básicas comparado ao que o JADE oferece, talvez haja uma forma mais eficiente de refazer este projeto

### Trabalhos Futuros  
- Deve haver alguma forma de tornar os agentes mais inteligentes e usar comportamentos mais complexos a fim de otimizat todo o sistema. 

</details>

<details><summary>Pablo Christianno</summary>

### Lições Aprendidas  
- 

### Percepções  
- 

### Contribuições e Fragilidades  
- 

### Trabalhos Futuros  
- 

</details>

## Fontes
[**Game of Life Online**](https://playgameoflife.com)

[**Game of Life Wiki**](https://pt.wikipedia.org/wiki/Jogo_da_vida)

