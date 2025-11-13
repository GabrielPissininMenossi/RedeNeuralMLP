package fipp.muscleandiq.redeneuralmlp;

import fipp.muscleandiq.redeneuralmlp.entities.Entrada;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
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
    private TextField tfCaminhoArquivo;
    @FXML
    public TableView<Entrada> tableView;
    private List<Entrada> entradaList = new ArrayList<>();
    private List<String> saidasList = new ArrayList<>();
    private int atributos = 0; // entradas
    private int saidas = 0;
    private int qtdeNeuroniosOcultos = 0;
    private double[][] mEntradaOculta;
    private double[][] mOcultaSaida;
    private double[] vetNetOculto;
    private double[] vetNetSaida;
    private double[] vetIOculto;
    private double[] vetISaida;
    private double[] vetErroSaida;
    private double[] vetErroOculta;
    @FXML
    public void initialize()
    {
        tfErro.setText("0.00001");
        tfNumIteracao.setText("5000");
        tfN.setText("0.1");
        idLinear.setSelected(true);
    }
    private void gerarMatrizes()
    {
        double peso;
        mEntradaOculta = new double[atributos][qtdeNeuroniosOcultos];
        mOcultaSaida = new double[qtdeNeuroniosOcultos][saidas];
        vetNetOculto = new double[qtdeNeuroniosOcultos];
        vetNetSaida = new double[saidas];
        vetIOculto = new double[qtdeNeuroniosOcultos];
        vetISaida = new double[saidas];
        vetErroSaida = new double[saidas];
        vetErroOculta = new double[qtdeNeuroniosOcultos];
        for (int i = 0; i < atributos; i++)
        {
            for (int j = 0; j < qtdeNeuroniosOcultos; j++)
            {
                peso = Math.random();
                mEntradaOculta[i][j] = peso;
            }
        }
        for (int i = 0; i < qtdeNeuroniosOcultos; i++)
        {
            for (int j = 0; j < saidas; j++)
            {
                peso = Math.random();
                mOcultaSaida[i][j] = peso;
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
    private double fnet(double valor)
    {
        if (idLinear.isSelected())
        {
            valor = valor/10.0;
        }
        else
        if (idLogistica.isSelected())
        {
            valor = 1/(1 + Math.pow(2.71828, (-1 * valor)));
        }
        else
        {
            valor = (1 - Math.pow(2.71828, (-2 * valor)))/ (1 + Math.pow(2.71828, (-2 * valor)));
        }

        return valor;
    }
    private double fnetDerivada(double valor)
    {
        if (idLinear.isSelected())
        {
            valor = 1/10.0;
        }
        else
        if (idLogistica.isSelected())
        {
            valor = fnet(valor) * (1 - fnet(valor));
        }
        else
        {
            valor = 1 - (Math.pow(fnet(valor), 2));
        }

        return valor;
    }
    private void treinarLinha(Entrada entrada)
    {
        double soma;
        List<Double> entradas = entrada.getEntradas();
        // net e i camada oculta
        for (int c = 0; c < qtdeNeuroniosOcultos; c++)
        {
            soma = 0;
            for (int l = 0; l < atributos; l++)
            {
                soma = soma + entradas.get(l) * mEntradaOculta[l][c];

            }
            vetNetOculto[c] = soma;
            vetIOculto[c] = fnet(soma);
        }

        // net e i da camada saida
        for (int c = 0; c < saidas; c++)
        {
            soma = 0;
            for (int l = 0; l < qtdeNeuroniosOcultos; l++)
            {
                soma = soma + vetIOculto[l] * mOcultaSaida[l][c];
            }
            vetNetSaida[c] = soma;
            vetISaida[c] = fnet(soma);

        }
        //erro neurônios camada de saída
        int pos, desejado;
        double erro;
        for(int c = 0; c < saidas; c++)
        {
            pos = buscarIndice(entrada.getClasse());
            if (pos == c)
                desejado = 1;
            else
                desejado = 0;
            erro = (desejado - vetISaida[c]) * fnetDerivada(vetNetSaida[c]);
            vetErroSaida[c] = erro;
        }

        //erro neuronios camada oculta
        for (int i = 0; i < qtdeNeuroniosOcultos; i++)
        {
            erro = 0;
            for (int j = 0; j < saidas; j++)
            {
                // revisar
                erro = erro + (vetErroSaida[j] * mOcultaSaida[i][j]) ;
            }
            vetErroOculta[i] = erro * fnetDerivada(vetNetOculto[i]);
        }

        // atualizar pesos das arestas da camada de oculta para saida
        double novoPeso;
        double n = Double.parseDouble(tfN.getText().toString());
        for (int i = 0; i < qtdeNeuroniosOcultos; i++)
        {
            for (int j = 0; j < saidas; j++)
            {
                novoPeso = mOcultaSaida[i][j] + n * vetErroSaida[j] * vetIOculto[i];
                mOcultaSaida[i][j] = novoPeso;
            }
        }
        // atualizar pesos das arestas da camada de entrada para oculta
        for (int i = 0; i < atributos; i++)
        {
            for(int j = 0; j < qtdeNeuroniosOcultos; j++)
            {
                novoPeso = mEntradaOculta[i][j] + n * vetErroOculta[j] * entradas.get(i);
                mEntradaOculta[i][j] = novoPeso;
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

    private void treinamento()
    {
        gerarMatrizes();
        int i = 0;
        double erroEpoca = 1.0;

        while (i < Integer.parseInt(tfNumIteracao.getText().toString()) && erroEpoca > Double.parseDouble(tfErro.getText().toString()))
        {
            int j = 0;
            double erroTotalEpoca = 0;
            while (j < entradaList.size())
            {
                Entrada entrada = entradaList.get(j);
                treinarLinha(entrada);
                erroTotalEpoca = erroTotalEpoca + calculaErro(entrada);
                //exibirMatrizEntradaOculta();
                //System.out.print("\n");
                //exibirMatrizOcultaSaida();
                //System.out.print("\n");
                j++;
            }
            erroEpoca = erroTotalEpoca / entradaList.size();
            System.out.printf("Epoca: %d Erro: %f\n",i, erroEpoca);
            i++;
        }
        System.out.println(entradaList.size());
        System.out.printf("Epoca: %d Erro: %f\n",i, erroEpoca);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Treinamento Finalizado");
        alert.showAndWait();
    }
    private void calcularNeuroniosOcultos()
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

    private void lerArquivo(File file)
    {
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String linha = bufferedReader.readLine();
            String []cabecalho = linha.split(",");
            List<String> cabecalhoLista = Arrays.asList(cabecalho);
            for (int i=0; i<cabecalhoLista.size(); i++)
            {
                final int index = i;
                TableColumn<Entrada, String> column = new TableColumn<>(cabecalhoLista.get(i));
                if (i == cabecalhoLista.size() - 1)  // classe
                {
                    column.setCellValueFactory(new PropertyValueFactory<>("classe"));
                }
                else
                {
                    column.setCellValueFactory(param ->new ReadOnlyStringWrapper(String.format("%.4f", param.getValue().getEntradas().get(index))));
                    atributos++;
                }
                column.prefWidthProperty().bind(tableView.widthProperty().divide(cabecalhoLista.size()));
                tableView.getColumns().add(column);
            }
            linha = bufferedReader.readLine();

            while (linha != null)
            {
                String[] partes = linha.split(",");
                List<Double> entradas = new ArrayList<>();
                String classe;

                for (int i=0; i<partes.length - 1; i++)
                {
                    entradas.add(Double.parseDouble(partes[i]));
                }
                classe = partes[partes.length - 1];
                if (!saidasList.contains(classe))
                    saidasList.add(classe);
                entradaList.add(new Entrada(entradas, classe));
                linha = bufferedReader.readLine();
            }
            saidas = saidasList.size();
            bufferedReader.close();

        }catch (Exception e){

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erro ao ler arquivo: " + e.getMessage());
            alert.showAndWait();

        }
    }

    public void onAbrir(ActionEvent actionEvent)
    {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("D://"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null)
        {
            tfCaminhoArquivo.setText(file.getAbsolutePath());
            tableView.getItems().clear();
            tableView.getColumns().clear();
            entradaList.clear();
            saidasList.clear();
            atributos = 0;
            saidas = 0;
            lerArquivo(file);
            preencherTabela();
            calcularNeuroniosOcultos();
            tfCamadaEntrada.setText(""+atributos);
            tfCamadaSaida.setText(""+saidas);
            tfCamadaOculta.setText(""+qtdeNeuroniosOcultos);


        }

    }

    public void onAvancar(ActionEvent actionEvent)
    {
        treinamento();
    }
}
