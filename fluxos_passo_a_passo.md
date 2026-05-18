# Fluxos passo a passo do projeto GED

Este documento descreve, de forma objetiva, todos os fluxos principais do projeto e o que acontece em cada etapa. Ele também aponta onde o dataset real fica salvo para que a visualização não dependa de exemplos hardcoded.

## Saídas geradas

- `out/dataset.pb`: dataset sintético serializado em protobuf.
- `out/visualizer_data.json`: versão em JSON usada pelo dashboard.
- `dashboard.html`: dashboard final com os dados embutidos.

## Fluxo 1: Compilar o projeto

1. Abra o terminal na raiz do projeto.
2. Crie a pasta de saída, se ela não existir.
3. Compile todos os arquivos Java com `javac`.
4. Verifique se não houve erro de compilação antes de seguir.

Comando típico:

```powershell
if (!(Test-Path out)) { New-Item -ItemType Directory -Path out | Out-Null }
$sources = @(Get-ChildItem -Recurse src -Filter *.java | Select-Object -ExpandProperty FullName)
javac -encoding UTF-8 -d out $sources
```

## Fluxo 2: Gerar o dataset protobuf

1. Execute o ponto de entrada principal em modo de geração.
2. O gerador cria amostras sintéticas para as classes `b`, `d`, `h` e `k`.
3. O dataset completo é gravado em `out/dataset.pb`.
4. Esse arquivo é a referência real do conjunto gerado.

Comando típico:

```powershell
java -cp out app.Main generate-dataset 1000 out/dataset.pb
```

O que esse fluxo entrega:

- dados sintéticos reproduzíveis por semente fixa;
- arquivo protobuf pronto para entrega;
- base para a visualização e para análises posteriores.

## Fluxo 3: Gerar o dashboard visual

1. Execute o exportador do dashboard.
2. Ele gera o dataset em memória.
3. Ele grava esse dataset em `out/dataset.pb`.
4. Ele monta o JSON de apoio em `out/visualizer_data.json`.
5. Ele injeta os dados reais no template e escreve `dashboard.html`.
6. Abra `dashboard.html` no navegador.

Comando típico:

```powershell
java -cp out app.VisualizerExporter
```

O que mostrar no dashboard:

- lista de amostras geradas;
- grafo de entrada da amostra selecionada;
- protótipos comparados em cada amostra;
- menor distância encontrada e classe prevista;
- acerto ou erro da classificação.

## Fluxo 4: Rodar a demonstração no terminal

1. Execute o modo `demo` da aplicação principal.
2. O programa gera algumas amostras sintéticas.
3. Para cada amostra, calcula a distância para os protótipos.
4. O terminal mostra a classe esperada, a classe prevista e a distância.

Comando típico:

```powershell
java -cp out app.Main demo
```

Esse fluxo é útil quando você quer explicar a lógica sem depender da interface gráfica.

## Fluxo 5: Rodar os testes de sanidade

1. Execute a suíte simples de testes.
2. O projeto verifica propriedades básicas da distância entre grafos.
3. Use esse fluxo para confirmar que a implementação continua coerente depois de mudanças.

Comando típico:

```powershell
java -cp out app.TestCases
```

## Fluxo 6: Visualizar o resultado final

1. Gere o dashboard com o exportador.
2. Abra `dashboard.html`.
3. Escolha uma amostra na lateral.
4. Observe o grafo de entrada e os protótipos correspondentes.
5. Mostre como o menor valor de distância determina a previsão.

## Roteiro curto para apresentação

1. Explicar que o projeto cria grafos sintéticos com ruído controlado.
2. Mostrar a geração do protobuf em `out/dataset.pb`.
3. Abrir o dashboard e selecionar algumas amostras.
4. Comparar a classe esperada com a classe prevista.
5. Encerrar destacando que a visualização vem do dataset gerado, e não de exemplos fixos.
