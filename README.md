# Distance Measure Between Attributed Relational Graphs

Reprodução em Java do algoritmo descrito por Sanfeliu & Fu, IEEE 1983, com busca por configurações, poda branch-and-bound e dataset sintético serializado em protobuf.
https://ieeexplore.ieee.org/document/6313167/

## O que foi reconstruído

O paper define a distância como uma minimização sobre configurações possíveis entre um grafo de entrada e um grafo de referência:

$$
d(g_i, g_j) = \min_{configura\c{c}\~oes} \left(
w_{nr} C_{nr} + w_{ni} C_{ni} + w_{nd} C_{nd} + w_{bi} C_{bi} + w_{bd} C_{bd} + w_{ns} C_{ns} + w_{bs} C_{bs}
\right)
$$

Nesta implementação:

- `Cnr` mede similaridade estrutural dos nós por grau e atributos.
- `Cni` e `Cnd` penalizam inserção e deleção de nós.
- `Cbi` e `Cbd` penalizam inserção e deleção de arestas.
- `Cns` e `Cbs` penalizam substituição de labels de nós e arestas.

O algoritmo segue o fluxo descrito no artigo:

1. reconhecimento de candidatos por custo.
2. seleção de configurações possíveis de nós.
3. busca de configurações com poda branch-and-bound.
4. validação de atributos da configuração.
5. cálculo do custo de transformação.
6. escolha da distância mínima.

## Ambiguidades adotadas

O artigo usa uma gramática de descrição de grafos e custos definidos de forma contextual ao domínio de caracteres manuscritos. Como o texto não fixa uma gramática universal para qualquer domínio, esta reprodução adota as seguintes decisões:

- O grafo é tratado como não direcionado para a correspondência estrutural.
- As labels de nós e arestas são simbólicas e contribuem para os custos de substituição.
- A validação de atributos é conservadora, mas não bloqueia uma substituição válida com custo.
- A busca é exata no espaço das configurações consideradas, mas com poda heurística por ordem de expansão e lower bound simples.

## Estrutura

- `src/graph` modela nós, arestas e grafos atribuidos.
- `src/distance` contém custo, validação, busca e distância.
- `src/dataset` gera amostras sintéticas e escreve protobuf manualmente.
- `src/app` traz demo e testes simples.
- `proto/dataset.proto` define o schema solicitado.

## Como compilar

O ambiente aqui não tem Maven instalado, então o projeto foi mantido sem dependências externas. Compile com `javac`:

```powershell
$sources = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $sources
```

## Como executar

Demo de classificação por distância:

```powershell
java -cp out app.Main demo
```

Gerar dataset protobuf:

```powershell
java -cp out app.Main generate-dataset 1000 out/dataset.pb
```

Rodar verificações simples:

```powershell
java -cp out app.TestCases
```

## Complexidade

A busca exata é combinatória. No pior caso, o número de configurações cresce como uma função fatorial do número de nós, pois cada nó de entrada pode ser associado a um nó de referência, deletado, ou deixar um nó de referência para inserção posterior.

A poda branch-and-bound reduz o espaço de busca ao expandir nós com maior grau primeiro e descartar ramos cujo custo parcial já excede a melhor solução atual. Ainda assim, a complexidade assintótica permanece exponencial no pior caso, como esperado do problema original do paper.