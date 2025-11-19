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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MainController {
    @FXML
    public RadioButton idLinear;
    @FXML
    public RadioButton idLogistica;
    @FXML
    public RadioButton idHiperbolica;


    public Label lbEpocaAtual;
    public Label lbErroEpoca;
    public Label lbErroMinimo;
    public Label lbErroMaximo;
    public Label lbAcuracia;

    @FXML
    private TextField tfCaminhoArquivoTreinoTeste;
    @FXML
    private Button btCarregarTreinoTeste;
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

    @FXML
    private Button btCarregarTeste;
    @FXML
    private Button btCarregarTreino;
    @FXML
    private Button btTreinar;
    @FXML
    private Button btTestar;


    private XYChart.Series<Number, Number> serieErro = new XYChart.Series<>();

    // flag
    private int flagTreinoTesteCarregado = 0;
    private int flagTreinoCarregado = 0;
    private int flagTesteCarregado = 0;
    private int flagTreinoTreinado = 0;
    private int flag = 0;
    private String funcaoTreinada;

    //variáveis
    private List<Entrada> entradaTreinoList = new ArrayList<>(); //lista de treinamento
    private List<Entrada> entradaTesteList = new ArrayList<>(); //lista para os testes

    // Min/max do treinamento da normalização, pois no teste deve ser utilizado esses valores
    private double[] minTreino;
    private double[] maxTreino;
    private List<Double> ultimas10Epocas = new ArrayList<>();
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
    private double valorTreinamento;
    private int[][] matrizConfusao;

    @FXML
    public void initialize()
    {
        //inicializar com valores default
        tfErro.setText("0.00001");
        tfNumIteracao.setText("1000");
        tfN.setText("0.1");
        idLinear.setSelected(true);

        //inicializar gráfico
        serieErro.setName("Erro por época");
        lcGrafico.getData().add(serieErro);
        
        //teste do botão
        //btCarregarTeste.setDisable(true);
    }
    private void exibirTeste()
    {
        int i = 0;
        while (i < entradaTesteList.size())
        {
            System.out.println();
            i++;
        }
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
    private void adicionarEpoca(double erro)
    {
        if (ultimas10Epocas.size() == 100)
        {
            ultimas10Epocas.remove(0);
        }
        ultimas10Epocas.add(erro);
    }
    private double desvioPadrao()
    {
        double soma = 0;
        double media;
        for (int i = 0; i < ultimas10Epocas.size();i++)
        {
            soma = soma + ultimas10Epocas.get(i);
        }
        media = soma/ultimas10Epocas.size();
        soma = 0;
        for (int i = 0; i < ultimas10Epocas.size(); i++)
        {
            soma = soma + Math.pow(ultimas10Epocas.get(i) - media, 2);
        }
        soma = soma/(ultimas10Epocas.size() - 1);
        return Math.sqrt(soma);
    }
    private boolean isPlato()
    {
        if (ultimas10Epocas.size() < 100)
            return false;
        else
        if (desvioPadrao() >= 0 && desvioPadrao() <= 0.00001)
            return true;

        return false;
    }
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
            flag = 0;
            ultimas10Epocas.clear();
            double erroMinimo = 9999, erroMaximo = 0.0;
            while (i < epocas && erroEpoca > erroEsperado && flag != 1)
            {

                double erroTotalEpoca = 0;
                int j=0;
                while(j<entradaTreinoList.size())
                {
                    // passo 1 -> pega as entradas
                    Entrada entrada = entradaTreinoList.get(j);

                    //passos 2 até 9
                    treinarLinha(entrada);

                    //passo 10 -> calcula o erra da rede
                    erroTotalEpoca += calculaErro(entrada);


                    j++;
                }
                erroEpoca = erroTotalEpoca / entradaTreinoList.size();
                System.out.printf("Epoca: %d Erro: %f\n",i + 1, erroEpoca);
                if (erroEpoca > erroMaximo)
                    erroMaximo = erroEpoca;
                if (erroEpoca < erroMinimo)
                    erroMinimo = erroEpoca;

                int finalI = i + 1;
                double finalErroEpoca = erroEpoca;
                double finalErroMinimo = erroMinimo;
                double finalErroMaximo = erroMaximo;
                // Atualiza o gráfico na UI Thread
                Platform.runLater(() -> {
                    serieErro.getData().add(new XYChart.Data<>(finalI, finalErroEpoca));
                    lbEpocaAtual.setText(""+finalI);
                    lbErroEpoca.setText(String.format("%.6f", finalErroEpoca));
                    lbErroMaximo.setText(String.format("%.6f", finalErroMaximo));
                    lbErroMinimo.setText(String.format("%.6f", finalErroMinimo));
                });
                adicionarEpoca(finalErroEpoca);
                if (isPlato()) {

                    CountDownLatch latch = new CountDownLatch(1);

                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION);
                        alert1.setTitle("Sistema");
                        alert1.setHeaderText("Plato Encontrado");
                        alert1.setContentText("Deseja Interromper o Treinamento?");
                        ButtonType botaoContinuar = new ButtonType("Continuar");
                        ButtonType botaoReduzir = new ButtonType("Reduzir em 10%");
                        ButtonType botaoInterromper = new ButtonType("Interromper", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert1.getButtonTypes().setAll(botaoContinuar, botaoReduzir, botaoInterromper);
                        alert1.showAndWait().ifPresent(button -> {
                            if (button == botaoContinuar)
                            {
                                ultimas10Epocas.clear();
                            }
                            else
                            if(button == botaoReduzir)
                            {
                                n = n * 0.90;
                                ultimas10Epocas.clear();
                                Platform.runLater(() -> tfN.setText(String.format("%.4f", n)));

                            }
                            else
                            if (button == botaoInterromper)
                            {
                                flag = 1;
                            }

                            latch.countDown(); // libera o treinamento
                        });
                    });

                    // aqui o treinamento aguarda o usuário
                    try {
                        latch.await();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }

                }
                i++;
            }
            System.out.printf("==== FIM DO TREINAMENTO ====\n");
            System.out.printf("Última Época: %d\n", i);
            System.out.printf("Erro Final: %.6f\n", erroEpoca);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sistema");
                alert.setHeaderText("Finalizado");
                alert.setContentText("Treinamento Realizado");
                alert.showAndWait();
            });

        }).start();
    }
    private double calcularAcuracia() {
        if (matrizConfusao == null || matrizConfusao.length == 0)
            return 0;

        int acertos = 0;
        int total = 0;

        for (int i = 0; i < matrizConfusao.length; i++) {
            for (int j = 0; j < matrizConfusao[i].length; j++) {
                int valor = matrizConfusao[i][j];
                total += valor;

                if (i == j) { // diagonal → valores corretos
                    acertos += valor;
                }
            }
        }

        return total == 0 ? 0 : (double) acertos / total;
    }

    private void atualizarTabelaConfusao() {

        if (matrizConfusao == null || matrizConfusao.length == 0)
            return;

        // Se a tabela não tem colunas ou se o tamanho mudou → recria colunas
        if (tvConfusao.getColumns().isEmpty() ||
                tvConfusao.getColumns().size() != matrizConfusao[0].length) {

            tvConfusao.getColumns().clear();

            int colunas = saidasList.size();

            for (int c = 0; c < colunas; c++) {
                final int index = c;

                TableColumn<int[], Integer> coluna =
                        new TableColumn<>(saidasList.get(c));

                coluna.setCellValueFactory(
                        data -> new SimpleIntegerProperty(
                                data.getValue()[index]
                        ).asObject()
                );
                coluna.prefWidthProperty().bind(tvConfusao.widthProperty().divide(colunas + 0.1));
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
        while(i < entradaTesteList.size())
        {
            //pegar a linha atual
            Entrada entrada = entradaTesteList.get(i);
            //encontrar a posição na linha da matriz de confusão
            posClasse = buscarIndice(entrada.getClasse());
            classeResultado = testarLinha(entrada); //essa função que contém toda a complexidade de um teste da rede neural
            //realizo a soma do que conseguiu chegar de resultado
            matrizConfusao[posClasse][classeResultado] = matrizConfusao[posClasse][classeResultado]+1;
            i++;
        }
        exibirMatrizConfusao();
        Platform.runLater(() -> {
            atualizarTabelaConfusao();
            lbAcuracia.setText(String.format("%.2f %%", calcularAcuracia() * 100));
        });
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

                    entradaTreinoList.add(new Entrada(entradas, classe));
                }
                else {
                    entradaTesteList.add(new Entrada(entradas, classe));
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

        for (Entrada e : entradaTreinoList) {
            for (int c = 0; c < atributos; c++) {
                double v = e.getEntradas().get(c);

                if (v < minTreino[c])
                    minTreino[c] = v;
                if (v > maxTreino[c])
                    maxTreino[c] = v;
            }
        }
    }

    private void normalizarEntradasTreino()
    {
        for (Entrada e : entradaTreinoList) {
            for (int c = 0; c < atributos; c++) {
                double v = e.getEntradas().get(c);
                double n = (v - minTreino[c]) / (maxTreino[c] - minTreino[c]);
                e.getEntradas().set(c, n);
            }
        }
        tableView.setItems(FXCollections.observableArrayList(entradaTreinoList));
    }
    private void normalizarEntradasTeste()
    {
        for (Entrada e : entradaTesteList) {
            for (int c = 0; c < atributos; c++) {
                double v = e.getEntradas().get(c);
                double n = (v - minTreino[c]) / (maxTreino[c] - minTreino[c]);
                e.getEntradas().set(c, n);
            }
        }
    }

    public void onAbrirTreino(ActionEvent actionEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("D://"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        File file = fileChooser.showOpenDialog(null);

        if (file != null)
        {
            if(flagTreinoTesteCarregado == 1)
                flagTesteCarregado = 0;
            flagTreinoTesteCarregado = 0;
            flagTreinoCarregado = 1;
            flagTreinoTreinado = 0;

            tfCaminhoArquivoTreino.setText(file.getAbsolutePath());
            tfCaminhoArquivoTreinoTeste.setText("");
            entradaTreinoList.clear();
            serieErro.getData().clear();
            lcGrafico.getData().clear();
            lcGrafico.getData().add(serieErro);
            tvConfusao.getColumns().clear();
            inicializarValores();
            lerArquivo(file, true); //leio o arquivo
            calcularMinMaxTreino(); //calculo os mínimos e máximos
            normalizarEntradasTreino(); //normalizo as entradas

            saidas = saidasList.size();
            calcularQtdeNeuroniosOcultos();

            tfCamadaEntrada.setText("" + atributos);
            tfCamadaSaida.setText("" + saidas);
            tfCamadaOculta.setText("" + qtdeNeuroniosOcultos);
        }
    }

    public void onAbrirTeste(ActionEvent actionEvent)
    {
        if (flagTreinoCarregado == 1)
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("D://"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

            File file = fileChooser.showOpenDialog(null);
            if (file != null)
            {
                flagTesteCarregado = 1;
                tfCaminhoArquivoTeste.setText(file.getAbsolutePath());
                tfCaminhoArquivoTreinoTeste.setText("");
                entradaTesteList.clear();

                lerArquivo(file, false);
                normalizarEntradasTeste();
            }
        }
       else
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Arquivo Ainda Não Carregado");
            alert.setTitle("Erro");
            alert.setContentText("Carregue o Arquivo de Treino Antes");
            alert.showAndWait();
        }
    }

    private void desabilitarBotoes()
    {
        Platform.runLater(() -> {
            btCarregarTeste.setDisable(true);
            btCarregarTreino.setDisable(true);
            btTreinar.setDisable(true);
            btTestar.setDisable(true);
        });
    }

    private void habilitarBotoes()
    {
        Platform.runLater(() -> {
            btCarregarTeste.setDisable(false);
            btCarregarTreino.setDisable(false);
            btTreinar.setDisable(false);
            btTestar.setDisable(false);
        });
    }
    public String getFuncaoAtual()
    {
        String aux = "";
        if (idLinear.isSelected())
            aux = idLinear.getText();
        else
        if (idLogistica.isSelected())
            aux = idLogistica.getText();
        else
        if (idHiperbolica.isSelected())
            aux = idHiperbolica.getText();

        return aux;
    }
    public void onTreinar(ActionEvent actionEvent)
    {
        if (flagTreinoCarregado == 1 || flagTreinoTesteCarregado == 1)
        {
            funcaoTreinada = getFuncaoAtual();
            tfN.setText("0.1");
            tvConfusao.getColumns().clear();
            lbAcuracia.setText("0.0 %");
            desabilitarBotoes();
            treinamento();
            habilitarBotoes();
            flagTreinoTreinado = 1;
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Arquivo Ainda Não Carregado");
            alert.setTitle("Erro");
            alert.setContentText("Carregue o Arquivo de Treinamento Antes");
            alert.showAndWait();
        }
    }

    public void onTestarEntrada(ActionEvent actionEvent)
    {
        if (((flagTesteCarregado == 1 && flagTreinoTreinado == 1) || (flagTreinoTesteCarregado == 1 && flagTreinoTreinado == 1)) && funcaoTreinada.equals(getFuncaoAtual()))
        {
            System.out.println(funcaoTreinada);
            System.out.println(getFuncaoAtual());
            desabilitarBotoes();
            testarEntradas();
            habilitarBotoes();
        }
        else
        {
            if (flagTreinoTreinado == 0)
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Treino Ainda Não Realizado");
                alert.setTitle("Erro");
                alert.setContentText("Realize o Treinamento Antes");
                alert.showAndWait();
            }
            else
            if (flagTreinoTesteCarregado == 0 && flagTesteCarregado == 0)
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Arquivo Ainda Não Carregado");
                alert.setTitle("Erro");
                alert.setContentText("Carregue o Arquivo de Teste Antes");
                alert.showAndWait();
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Função de Transferência");
                alert.setTitle("Erro");
                alert.setContentText("Não Correspondente Com o Treino Realizado");
                alert.showAndWait();
            }
        }
    }
    private boolean isNumero (String string)
    {
        try{
            Double num = Double.parseDouble(string);
            System.out.println(num);
            return !num.isNaN();
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }
    public void onAbrirTreinoTeste(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        //fileChooser.setInitialDirectory(new File("D://"));
        fileChooser.setInitialDirectory(new File("/home/gabriel/Documents/faculdade/facul-6t/IA 1 - Inteligencia Artificial 1/bimestre2/RedeNeural/RedeNeuralMLP/src/main/resources/arquivos"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {

            TextInputDialog inputDialog = new TextInputDialog();
            inputDialog.setTitle("Input Treinamento/Teste");
            inputDialog.setHeaderText("Porcentagem de Treinamento");
            inputDialog.setContentText("Informe a % do Arquivo para Treino:");
            Optional<String> resultTreino = inputDialog.showAndWait();

            //tratar os valores recebidos
            if (resultTreino.isPresent() && isNumero(resultTreino.get()))
            {
                valorTreinamento = Double.parseDouble(resultTreino.get());
                if(valorTreinamento < 1 || valorTreinamento > 99)
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sistema");
                    alert.setHeaderText("Valores Padrões Foram Aplicados");
                    alert.setContentText("70% para TREINAMENTO e 30% para TESTE");
                    alert.showAndWait();
                    valorTreinamento = 70;
                }
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sistema");
                alert.setHeaderText("Valores Padrões Foram Aplicados");
                alert.setContentText("70% para TREINAMENTO e 30% para TESTE");
                alert.showAndWait();
                valorTreinamento = 70;
            }

            tfCaminhoArquivoTreinoTeste.setText(file.getAbsolutePath());
            serieErro.getData().clear();
            lcGrafico.getData().clear();
            lcGrafico.getData().add(serieErro);
            tvConfusao.getColumns().clear();
            entradaTesteList.clear();
            entradaTreinoList.clear();
            inicializarValores();
            lerArquivoTreinoTeste(file);
            saidas = saidasList.size();
            calcularQtdeNeuroniosOcultos();
            calcularMinMaxTreino();
            normalizarEntradasTreino();
            normalizarEntradasTeste();

            // Exibe apenas TREINO na tabela
            tableView.getItems().addAll(entradaTreinoList);

            //setar as flags
            flagTreinoTesteCarregado = 1;
            flagTreinoTreinado = 0;
            flagTreinoCarregado = 0;
            //limpar o conteúdo que aparece no treino e teste isolados
            tfCaminhoArquivoTeste.setText("");
            tfCaminhoArquivoTreino.setText("");

            tfCamadaEntrada.setText("" + atributos);
            tfCamadaSaida.setText("" + saidas);
            tfCamadaOculta.setText("" + qtdeNeuroniosOcultos);
        }
    }
    private void embaralharEntradas(List<Entrada> entradas)
    {
        int pos;
        Entrada aux;
        Random random = new Random();
        //random.nextInt(entradas.size());
        for(int i=0; i<entradas.size(); i++)
        {
            pos = random.nextInt(entradas.size());

            aux = entradas.get(i);
            entradas.set(i, entradas.get(pos));
            entradas.set(pos, aux);
        }

        for(Entrada e: entradas)
        {
            System.out.println(e.getClasse());
        }
    }
    private void lerArquivoTreinoTeste(File file)
    {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String linha = br.readLine();
            String[] cabecalho = linha.split(",");

            // Limpa tudo
            tableView.getItems().clear();
            tableView.getColumns().clear();
            atributos = 0;
            saidasList.clear();
            entradaTreinoList.clear();
            entradaTesteList.clear();

            // 1) Monta as colunas com o cabecalho
            for (int i = 0; i < cabecalho.length; i++) {

                final int index = i;
                TableColumn<Entrada, String> column = new TableColumn<>(cabecalho[i]);

                if (i == cabecalho.length - 1) // Última coluna do arquivo representa a classe
                {
                    column.setCellValueFactory(new PropertyValueFactory<>("classe"));
                }
                else // O restante das colunas são os atributos e/ou entradas
                {
                    column.setCellValueFactory(param ->
                            new ReadOnlyStringWrapper(String.format("%.4f",
                                    param.getValue().getEntradas().get(index))));
                    atributos++;
                }

                column.prefWidthProperty().bind(tableView.widthProperty().divide(cabecalho.length));
                tableView.getColumns().add(column);
            }

            // 2) Lê todas as linhas do arquivo
            List<Entrada> todasEntradas = new ArrayList<>();

            linha = br.readLine(); //entra lido
            while (linha != null) {
                String[] partes = linha.split(","); //separo todas as partes da minha linha
                List<Double> entradas = new ArrayList<>();

                // atributos numéricos
                for (int i = 0; i < partes.length - 1; i++) //até -1 pois o último é a classe, um atributo numérico
                {
                    entradas.add(Double.parseDouble(partes[i]));
                }

                // classe
                String classe = partes[partes.length - 1]; //último elemento da minha linha

                // armazena temporariamente
                todasEntradas.add(new Entrada(entradas, classe));
                // adiciona classe única
                if (!saidasList.contains(classe))
                    saidasList.add(classe);

                linha = br.readLine(); //sai lido
            }
            br.close();

            //embaralhar as entradas
            embaralharEntradas(todasEntradas);

            // 3) SEPARAÇÃO TREINO / TESTE
            int total = todasEntradas.size();
            int qtdTreino = (int) Math.round((valorTreinamento / 100.0) * total);
            System.out.println(qtdTreino);

            //preencher a lista de treino
            for (int i = 0; i < qtdTreino; i++)
                entradaTreinoList.add(todasEntradas.get(i));

            //preencher a lista de testes
            for (int i = qtdTreino; i < todasEntradas.size(); i++)
                entradaTesteList.add(todasEntradas.get(i));

            //System.out.println(todasEntradas.size());
            //System.out.println(entradaTreinoList.size());
            //System.out.println(entradaTesteList.size());

            // a partir daqui eu já li todo o arquivo e separei entre treino e teste

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erro ao ler arquivo: " + e.getMessage());
            alert.showAndWait();
        }
    }
    private void inicializarValores()
    {
        lbAcuracia.setText("0.0 %");
        lbErroMinimo.setText("0.0");
        lbErroMaximo.setText("0.0");
        lbErroEpoca.setText("0.0");
        lbEpocaAtual.setText("0");
    }

}
