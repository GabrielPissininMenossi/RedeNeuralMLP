package fipp.muscleandiq.redeneuralmlp;

import fipp.muscleandiq.redeneuralmlp.entities.Entrada;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
    private int atributos = 0; // entradas
    private int saidas = 0;
    private int qtdeNeuroniosOcultos = 0;
    private double[][] mEntradaOculta;
    private double[][] mOcultaSaida;
    private double[] vetNetOculto;
    private double[] vetNetSaida;
    private double[] vetIOculto;
    private double[] vetISaida;
    private void gerarMatrizes()
    {
        double peso;
        mEntradaOculta = new double[atributos][qtdeNeuroniosOcultos];
        mOcultaSaida = new double[qtdeNeuroniosOcultos][saidas];
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
    private double calculaErro()
    {
        return 0; //teste
    }
    private int buscarIndice(String classe)
    {
//        int i = 0;
//        while(i < sai)
//        return i + 1;
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
                soma = soma + entradas.get(l) * mEntradaOculta[c][l];

            }
            vetNetOculto[c] = soma;
            vetIOculto[c] = soma/2;
        }

        // net e i da camada saida
        for (int c = 0; c < saidas; c++)
        {
            soma = 0;
            for (int l = 0; l < qtdeNeuroniosOcultos; l++)
            {
                soma = soma + vetIOculto[l] * mOcultaSaida[c][l];
            }
            vetNetSaida[c] = soma;
            vetISaida[c] = soma/2;
        }

        //erro neurônios camada de saída
        //for(int )
    }
    private void treinamento()
    {
        gerarMatrizes();
        int i = 0;
        tfErro.setText("0.00001");
        double erro = 1;
        while (i < Integer.parseInt(tfNumIteracao.getText().toString()) && erro > Double.parseDouble(tfErro.getText().toString()))
        {
            int j = 0;
            while (j < entradaList.size())
            {
                Entrada entrada = entradaList.get(j);
                treinarLinha(entrada);
                j++;
            }
            erro = calculaErro();
            i++;
        }
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
            List<String> saidasList = new ArrayList<>();
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
            atributos = 0;
            saidas = 0;
            lerArquivo(file);
            preencherTabela();
            calcularNeuroniosOcultos();
            System.out.println(qtdeNeuroniosOcultos);
        }

    }
}
