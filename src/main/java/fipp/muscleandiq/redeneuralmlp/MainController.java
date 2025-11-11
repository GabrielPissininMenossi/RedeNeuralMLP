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
    private int atributos = 0;
    private int saidas = 0;
    private int qtdeNeuronios = 0;

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
                entradaList.add(new Entrada(entradas, classe));
                linha = bufferedReader.readLine();
            }
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
            lerArquivo(file);
            preencherTabela();
        }

    }
}
