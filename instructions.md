# Guia do Projeto: Distância entre Grafos (GED)

Este projeto implementa uma reprodução do algoritmo de Sanfeliu & Fu (1983) para calcular a distância entre grafos relacionais atribuídos, com busca branch-and-bound e um dashboard visual moderno.

---

## 🚀 Como começar (Passo a Passo)

### 1. Preparação
Certifique-se de estar na pasta raiz do projeto no seu terminal PowerShell:
`C:\Users\danie\grafos-av2`

### 2. Compilação
Como o projeto não utiliza Maven/Gradle, compilamos diretamente com o `javac`:
```powershell
# Cria a pasta de saída e compila todos os fontes
if (!(Test-Path out)) { New-Item -ItemType Directory -Path out | Out-Null }
$sources = @(Get-ChildItem -Recurse src -Filter *.java | Select-Object -ExpandProperty FullName)
javac -encoding UTF-8 -d out $sources
```

### 3. Execução do Dashboard (Recomendado)
Esta é a melhor forma de analisar os resultados. O comando abaixo gera um arquivo HTML interativo com os grafos renderizados:
```powershell
java -cp out app.VisualizerExporter
```
**Ação:** Após rodar, abra o arquivo `dashboard.html` no seu navegador.

---

## 🛠️ Outras Funcionalidades

### Rodar Testes de Sanidade
Verifica se as regras básicas da distância (GED) estão funcionando corretamente:
```powershell
java -cp out app.TestCases
```

### Demonstração via Terminal
Mostra as distâncias e previsões de classe diretamente no console:
```powershell
java -cp out app.Main demo
```

### Gerar Dataset Protobuf
Gera o arquivo binário solicitado para entrega, contendo amostras sintéticas:
```powershell
# Gera 1000 amostras por classe e salva em out/dataset.pb
java -cp out app.Main generate-dataset 1000 out/dataset.pb
```

---

## 📊 Como analisar os resultados?

Ao abrir o `dashboard.html` ou olhar a saída do terminal, foque nestes três pontos:

1.  **Expected vs Predicted**: 
    *   `Expected`: A classe original (ex: 'b').
    *   `Predicted`: A classe que o algoritmo achou ser a mais próxima.
    *   Se forem iguais, o algoritmo acertou a classificação.

2.  **Valor da Distância**:
    *   **0.0**: Os grafos são estruturalmente idênticos.
    *   **Quanto menor o valor**, mais parecidos são os grafos.
    *   Valores altos indicam grandes diferenças (muitas inserções, deleções ou trocas de labels).

3.  **Visualização dos Grafos (no Dashboard)**:
    *   Observe como o ruído (nós extras ou labels trocadas) afeta a distância.
    *   Você pode arrastar os círculos (nós) para organizar a visualização como desejar.

---

## 📁 Estrutura do Projeto

*   `src/graph/`: Modelagem de Nós, Arestas e Grafos.
*   `src/distance/`: O "coração" do projeto (Cálculo de custos e busca Branch-and-Bound).
*   `src/dataset/`: Gerador de grafos sintéticos e exportador Protobuf.
*   `src/app/`: Pontos de entrada (Main, Testes e Exibição).
*   `dashboard.html`: Interface visual para análise de resultados.
*   `proto/dataset.proto`: Definição do schema Protobuf.
