package fipp.muscleandiq.redeneuralmlp;

import fipp.muscleandiq.redeneuralmlp.entities.Entrada;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML
    public RadioButton idLinear;
    @FXML
    public RadioButton idLogistica;
    @FXML
    public RadioButton idHiperbolica;
    @FXML
    private TextField tfCamadaEntrada;
    @FXML
    private TextField tfCamadaSaida;
    @FXML
    private TextField tfCamadaOculta;
    @FXML
    private TextField tfErro;
    @FXML
    private TextField tfN;
    @FXML
    private TextField tfNumIteracao;
    @FXML
    private TextField tfCaminhoArquivoTreino;
    @FXML
    private TextField tfCaminhoArquivoTeste;
    @FXML
    public TableView<Entrada> tableView;
    @FXML
    public TableView<int[]> tvConfusao; //fazer
    @FXML
    private LineChart<Number, Number> lcGrafico;

    private XYChart.Series<Number, Number> serieErro = new XYChart.Series<>();

    //variáveis
    private List<Entrada> entradaList = new ArrayList<>();//lista de treinamento

    // Min/max do treinamento da normalização, pois no teste deve ser utilizado esses valores
    private double[] minTreino;
    private double[] maxTreino;

    private List<String> saidasList = new ArrayList<>();
    private int atributos = 0; // qtde de entradas
    private int saidas = 0; // qtde de neuronios de saida
    private int qtdeNeuroniosOcultos = 0;
    private double[][] mEntradaOculta;
    private double[][] mOcultaSaida;
    private double[] vetNetOculto;
    private double[] vetIOculto;
    private double[] vetErroOculta;
    private double[] vetNetSaida;
    private double[] vetISaida;
    private double[] vetErroSaida;
    private double n;
    private int[][] matrizConfusao;

    @FXML
    public void initialize()
    {
        //inicializar com valores default
        tfErro.setText("0.00001");
        tfNumIteracao.setText("5000");
        tfN.setText("0.1");
        idLinear.setSelected(true);

        //inicializar gráfico
        serieErro.setName("Erro por época");
        lcGrafico.getData().add(serieErro);
    }

    //inicializar metrizes de arestas e vetores
    private void gerarMatrizes()
    {
        mEntradaOculta = new double[atributos][qtdeNeuroniosOcultos];
        mOcultaSaida = new double[qtdeNeuroniosOcultos][saidas];
        vetNetOculto = new double[qtdeNeuroniosOcultos];
        vetNetSaida = new double[saidas];
        vetIOculto = new double[qtdeNeuroniosOcultos];
        vetISaida = new double[saidas];
        vetErroSaida = new double[saidas];
        vetErroOculta = new double[qtdeNeuroniosOcultos];

        //preencher as arestas de entrada para a camada oculta
        for (int i = 0; i < atributos; i++)
        {
            for (int j = 0; j < qtdeNeuroniosOcultos; j++)
            {
                //intervalo de -1 a 1
                mEntradaOculta[i][j] = Math.random()*2 - 1;
            }
        }

        //preencher as arestas da camada oculta para a camada de saida
        for (int i = 0; i < qtdeNeuroniosOcultos; i++)
        {
            for (int j = 0; j < saidas; j++)
            {
                //intervalo de -1 a 1
                mOcultaSaida[i][j] = Math.random()*2 - 1;
            }
        }
    }

    private double calculaErro(Entrada entrada)
    {
        double erro = 0;
        for (int i = 0; i < vetErroSaida.length; i++)
        {
            //erro += Math.pow(vetErroSaida[i],2); -> oq entendemos

            //do professor...
            int pos = buscarIndice(entrada.getClasse());
            int desejado;
            if (pos == i)
                desejado = 1;
            else
                desejado = 0;

            erro += Math.pow((desejado - vetISaida[i]), 2);
        }
        return 0.5 * erro;
    }

    private int buscarIndice(String classe)
    {
        int i = 0;
        while(i < saidasList.size() && !saidasList.get(i).equals(classe))
            i++;
        return i;
    }

    // Funções de Transferência
    // normal
    private double fnet(double net)
    {
        if (idLinear.isSelected()) {
            return net/10.0;
        }
        else if (idLogistica.isSelected()) {
            return 1/(1 + Math.exp(-net));
        }
        //se chegou aqui, então é HIPERBÓLICA
        return (1 - Math.exp(-2 * net)) / (1 + Math.exp(-2 * net));
    }

    // derivada
    private double fnetDerivada(double net)
    {
        if (idLinear.isSelected()) {
            return 1/10.0;
        }
        else if (idLogistica.isSelected()) {
            return fnet(net) * (1 - fnet(net));
        }
        //se chegou aqui, então é HIPERBÓLICA
        return 1 - (Math.pow(fnet(net), 2));
    }

    private void treinarLinha(Entrada entrada)
    {
        double soma;
        List<Double> entradas = entrada.getEntradas();

        //passos 2 e 3 -> calcula net da camada oculta
        // net e i camada oculta
        for (int c = 0; c < qtdeNeuroniosOcultos; c++)
        {
            soma = 0;
            //somatório das arestas multiplicadas pela entrada de cada neurônio
            for (int l = 0; l < atributos; l++)
            {
                soma += entradas.get(l) * mEntradaOculta[l][c];
            }
            vetNetOculto[c] = soma;
            vetIOculto[c] = fnet(soma);
        }

        //passos 4 e 5 -> calcula net e saída da camada de saída
        // net e i da camada saida
        for (int c = 0; c < saidas; c++)
        {
            soma = 0;
            for (int l = 0; l < qtdeNeuroniosOcultos; l++)
            {
                soma += vetIOculto[l] * mOcultaSaida[l][c];
            }
            vetNetSaida[c] = soma;
            vetISaida[c] = fnet(soma);
        }

        //passo 6 -> calcular erra da camada de saída
        //erro neurônios camada de saída
        int pos;
        double erro, desejado;
        for(int c = 0; c < saidas; c++)
        {
            pos = buscarIndice(entrada.getClasse());
            if (pos == c)
                desejado = 1;
            else
                desejado = 0;
            //desejado = fnetDerivada(vetNetSaida[c]); //temporário
            erro = (desejado - vetISaida[c]) * fnetDerivada(vetNetSaida[c]);
            vetErroSaida[c] = erro;
        }

        //passo 7 -> calcular erro dos neurônios da camada oculta
        //erro neuronios camada oculta
        for (int l = 0; l < qtdeNeuroniosOcultos; l++)
        {
            erro = 0;
            for (int c = 0; c < saidas; c++)
            {
                erro += (vetErroSaida[c] * mOcultaSaida[l][c]);
            }
            vetErroOculta[l] = erro * fnetDerivada(vetNetOculto[l]);
        }

        //passo 8 -> atualiza pesos arestas de camada oculta para a de saída
        // atualizar pesos das arestas da camada de oculta para saida
        for (int i = 0; i < qtdeNeuroniosOcultos; i++)
        {
            for (int j = 0; j < saidas; j++)
            {
                mOcultaSaida[i][j] = mOcultaSaida[i][j] + n * vetErroSaida[j] * vetIOculto[i];
            }
        }

        //passo 9 -> atualiza pesos arestas de camada de entrada e oculta
        // atualizar pesos das arestas da camada de entrada para oculta
        for (int i = 0; i < atributos; i++)
        {
            for(int j = 0; j < qtdeNeuroniosOcultos; j++)
            {
                mEntradaOculta[i][j] = mEntradaOculta[i][j] + n * vetErroOculta[j] * entradas.get(i);
            }
        }
    }

    private void exibirMatrizEntradaOculta()
    {
        for (int i = 0; i < atributos; i++)
        {
            for (int j = 0; j < qtdeNeuroniosOcultos; j++)
            {
                System.out.printf("%.4f ", mEntradaOculta[i][j]);
            }
            System.out.print("\n");
        }
    }

    private void exibirMatrizOcultaSaida()
    {
        for (int i = 0; i < qtdeNeuroniosOcultos; i++)
        {
            for (int j = 0; j < saidas; j++)
            {
                System.out.printf("%.4f ", mOcultaSaida[i][j]);
            }
            System.out.print("\n");
        }
    }

    private void exibirMatrizConfusao(){
        for (int l = 0; l < saidas; l++)
        {
            for (int c = 0; c < saidas; c++)
            {
                System.out.printf("%d ", matrizConfusao[l][c]);
            }
            System.out.print("\n");
        }
    }

    //treinamento da rede neural
//    private void treinamento()
//    {
//        gerarMatrizes(); //passo 0
//        int i = 0;
//        double erroEpoca = 1.0; //deixa um erro grande para
//        double erroEsperado = Double.parseDouble(tfErro.getText().toString()); //pega o erro setado no front
//        int epocas = Integer.parseInt(tfNumIteracao.getText().toString());
//        n = Double.parseDouble(tfN.getText().toString());
//
//        // fica treinando enquanto o erro for maior, ou a quantidade de épocas ainda n foi atingida
//        while (i < epocas && erroEpoca > erroEsperado)
//        {
//            int j = 0;
//            double erroTotalEpoca = 0;
//            while (j < entradaList.size())
//            {
//                // passo 1 -> pega as entradas
//                Entrada entrada = entradaList.get(j);
//
//                //passos 2 até 9
//                treinarLinha(entrada);
//
//                //passo 10 -> calcula o erra da rede
//                erroTotalEpoca = erroTotalEpoca + calculaErro(entrada);
//                j++;
//            }
//            erroEpoca = erroTotalEpoca / entradaList.size(); //atualizar o erro gerado na época
//            System.out.printf("Epoca: %d Erro: %f\n",i, erroEpoca);
//            i++;
//        }
//
//        // exibições
//        //System.out.println(entradaList.size());
//        System.out.printf("Epoca: %d Erro: %f\n",i, erroEpoca);
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setContentText("Treinamento Finalizado");
//        alert.showAndWait();
//    }

    private void treinamento()
    {
        new Thread(() -> {
            //limpa o gráfico para não exibir dados do treinamento anterior
            Platform.runLater(() -> {
                serieErro.getData().clear();
            });
            gerarMatrizes();
            int i = 0;
            double erroEpoca = 1.0;
            double erroEsperado = Double.parseDouble(tfErro.getText());
            int epocas = Integer.parseInt(tfNumIteracao.getText());
            n = Double.parseDouble(tfN.getText());

            while (i < epocas && erroEpoca > erroEsperado) {

                double erroTotalEpoca = 0;
                int j=0;
                while(j<entradaList.size())
                {
                    // passo 1 -> pega as entradas
                    Entrada entrada = entradaList.get(j);

                    //passos 2 até 9
                    treinarLinha(entrada);

                    //passo 10 -> calcula o erra da rede
                    erroTotalEpoca += calculaErro(entrada);

                    System.out.printf("Epoca: %d Erro: %f\n",i, erroEpoca);
                    j++;
                }

                erroEpoca = erroTotalEpoca / entradaList.size();

                int finalI = i;
                double finalErroEpoca = erroEpoca;

                // Atualiza o gráfico na UI Thread
                Platform.runLater(() -> {
                    serieErro.getData().add(new XYChart.Data<>(finalI, finalErroEpoca));
                });

                i++;
            }

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Treinamento Finalizado");
                alert.showAndWait();
            });

        }).start();
    }

    private void atualizarTabelaConfusao() {

        if (matrizConfusao == null || matrizConfusao.length == 0)
            return;

        // Se a tabela não tem colunas ou se o tamanho mudou → recria colunas
        if (tvConfusao.getColumns().isEmpty() ||
                tvConfusao.getColumns().size() != matrizConfusao[0].length) {

            tvConfusao.getColumns().clear();

            int colunas = matrizConfusao[0].length;

            for (int c = 0; c < colunas; c++) {
                final int index = c;

                TableColumn<int[], Integer> coluna =
                        new TableColumn<>("C" + c);

                coluna.setCellValueFactory(
                        data -> new SimpleIntegerProperty(
                                data.getValue()[index]
                        ).asObject()
                );

                tvConfusao.getColumns().add(coluna);
            }
        }

        // Carrega linhas
        ObservableList<int[]> linhas = FXCollections.observableArrayList();
        for (int[] linha : matrizConfusao) {
            linhas.add(linha);
        }

        tvConfusao.setItems(linhas);
    }

    private void testarEntradas()
    {
        gerarMatrizConfusao(); //para visualizar os erros e acertos do treinamento da rede neural
        int i=0;
        int posClasse, classeResultado;
        while(i < entradaList.size())
        {
            //pegar a linha atual
            Entrada entrada = entradaList.get(i);
            //encontrar a posição na linha da matriz de confusão
            posClasse = buscarIndice(entrada.getClasse());
            classeResultado = testarLinha(entrada); //essa função que contém toda a complexidade de um teste da rede neural
            //realizo a soma do que conseguiu chegar de resultado
            matrizConfusao[posClasse][classeResultado] = matrizConfusao[posClasse][classeResultado]+1;
            i++;
        }
        exibirMatrizConfusao();
        Platform.runLater(() -> atualizarTabelaConfusao());

    }

    private void gerarMatrizConfusao()
    {
        matrizConfusao = new int[saidas][saidas];
    }

    private int testarLinha(Entrada entrada)
    {
        List<Double> entradas = entrada.getEntradas();
        double soma;

        // ---- PASSO 1: Calcular nets da camada oculta ----
        for (int c = 0; c < qtdeNeuroniosOcultos; c++)
        {
            soma = 0;
            for (int l = 0; l < atributos; l++)
            {
                soma += entradas.get(l) * mEntradaOculta[l][c];
            }
            vetNetOculto[c] = soma;
            vetIOculto[c] = fnet(soma);
        }

        // ---- PASSO 2: Calcular nets da camada de saída ----
        for (int c = 0; c < saidas; c++)
        {
            soma = 0;
            for (int l = 0; l < qtdeNeuroniosOcultos; l++)
            {
                soma += vetIOculto[l] * mOcultaSaida[l][c];
            }
            vetNetSaida[c] = soma;
            vetISaida[c] = fnet(soma);
        }


        // ---- PASSO 3: Achar o índice do neurônio com maior saída ----
        int indiceMaior = 0;
        double maior = vetISaida[0];

        for (int k = 1; k < saidas; k++)
        {
            if (vetISaida[k] > maior)
            {
                maior = vetISaida[k];
                indiceMaior = k;
            }
        }

        return indiceMaior;
    }

    private void calcularQtdeNeuroniosOcultos()
    {
        qtdeNeuroniosOcultos = (int) Math.ceil((atributos + saidas)/2.0);
    }

    private void preencherTabela()
    {
        int i = 0, j;
        double valor, menorValor, maiorValor;

        while(i < entradaList.size())
        {
            Entrada entrada = entradaList.get(i);
            j = 0;
            while (j < entrada.getEntradas().size())
            {
                menorValor = buscarMenorColuna(j);
                maiorValor = buscarMaiorColuna(j);
                valor = entrada.getEntradas().get(j);
                valor = normalizarValor(valor, menorValor, maiorValor);
                entrada.getEntradas().set(j, valor);
                j++;
            }
            i++;
        }
        tableView.setItems(FXCollections.observableList(entradaList));
    }

    private double normalizarValor(double valorAtributo, double menorValorAtributo, double maiorValorAtributo)
    {
        return (valorAtributo - menorValorAtributo)/(maiorValorAtributo - menorValorAtributo);
    }

    private double buscarMaiorColuna(int coluna)
    {
        double maior = 0;
        int i = 0;
        while (i < entradaList.size())
        {
            Entrada aux = entradaList.get(i);
            if (aux.getEntradas().get(coluna) > maior)
                maior = aux.getEntradas().get(coluna);

            i++;
        }
        return maior;
    }

    private double buscarMenorColuna(int coluna)
    {
        int i = 0;
        double menor = 999999;
        while (i < entradaList.size())
        {
            Entrada aux = entradaList.get(i);
            if (aux.getEntradas().get(coluna) < menor)
                menor = aux.getEntradas().get(coluna);
            i++;
        }
        return menor;
    }

    private void lerArquivo(File file, boolean isTreino)
    {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String linha = bufferedReader.readLine();
            String[] cabecalho = linha.split(",");

            // Só cria as colunas se for TREINO
            if (isTreino) {
                tableView.getItems().clear();
                tableView.getColumns().clear();
                atributos = 0;
                saidasList.clear();

                for (int i = 0; i < cabecalho.length; i++) {
                    final int index = i;
                    TableColumn<Entrada, String> column = new TableColumn<>(cabecalho[i]);

                    if (i == cabecalho.length - 1) {
                        column.setCellValueFactory(new PropertyValueFactory<>("classe"));
                    } else {
                        column.setCellValueFactory(param ->
                                new ReadOnlyStringWrapper(String.format("%.4f",
                                        param.getValue().getEntradas().get(index))));
                        atributos++;
                    }

                    column.prefWidthProperty().bind(tableView.widthProperty().divide(cabecalho.length));
                    tableView.getColumns().add(column);
                }
            }

            // Lê dados
            linha = bufferedReader.readLine();
            while (linha != null) {
                String[] partes = linha.split(",");
                List<Double> entradas = new ArrayList<>();

                for (int i = 0; i < partes.length - 1; i++) {
                    entradas.add(Double.parseDouble(partes[i]));
                }

                String classe = partes[partes.length - 1];

                if (isTreino) {
                    if (!saidasList.contains(classe))
                        saidasList.add(classe);

                    entradaList.add(new Entrada(entradas, classe));
                }
                else {
                    entradaList.add(new Entrada(entradas, classe));
                }

                linha = bufferedReader.readLine();
            }

            bufferedReader.close();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erro ao ler arquivo: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void calcularMinMaxTreino() {
        minTreino = new double[atributos];
        maxTreino = new double[atributos];

        for (int c = 0; c < atributos; c++) {
            minTreino[c] = Double.MAX_VALUE;
            maxTreino[c] = -Double.MAX_VALUE;
        }

        for (Entrada e : entradaList) {
            for (int c = 0; c < atributos; c++) {
                double v = e.getEntradas().get(c);

                if (v < minTreino[c]) minTreino[c] = v;
                if (v > maxTreino[c]) maxTreino[c] = v;
            }
        }
    }

    private void normalizarEntradas(boolean treino)
    {
        for (Entrada e : entradaList) {
            for (int c = 0; c < atributos; c++) {
                double v = e.getEntradas().get(c);
                double n = (v - minTreino[c]) / (maxTreino[c] - minTreino[c]);
                e.getEntradas().set(c, n);
            }
        }
        if(treino)
            tableView.setItems(FXCollections.observableArrayList(entradaList));
    }

    public void onAbrirTreino(ActionEvent actionEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null)
        {
            tfCaminhoArquivoTreino.setText(file.getAbsolutePath());
            entradaList.clear();

            lerArquivo(file, true);
            calcularMinMaxTreino();
            normalizarEntradas(true);

            saidas = saidasList.size();
            calcularQtdeNeuroniosOcultos();

            tfCamadaEntrada.setText("" + atributos);
            tfCamadaSaida.setText("" + saidas);
            tfCamadaOculta.setText("" + qtdeNeuroniosOcultos);
        }
    }

    public void onAbrirTeste(ActionEvent actionEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null)
        {
            tfCaminhoArquivoTeste.setText(file.getAbsolutePath());
            entradaList.clear();

            lerArquivo(file, false);
            normalizarEntradas(false);
        }
    }

    public void onAvancar(ActionEvent actionEvent)
    {
        treinamento();
    }

    public void onTestarEntrada(ActionEvent actionEvent)
    {
        testarEntradas();
    }
}
