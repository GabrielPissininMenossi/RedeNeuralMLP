package fipp.muscleandiq.redeneuralmlp.entities;

import java.util.List;

public class Entrada
{
    private List<Double> entradas;
    private String classe;

    public Entrada(List<Double> entradas, String classe) {
        this.entradas = entradas;
        this.classe = classe;
    }

    public List<Double> getEntradas() {
        return entradas;
    }

    public void setEntradas(List<Double> entradas) {
        this.entradas = entradas;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }
}
