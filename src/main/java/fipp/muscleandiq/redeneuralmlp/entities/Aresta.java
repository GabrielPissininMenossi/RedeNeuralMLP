package fipp.muscleandiq.redeneuralmlp.entities;

public class Aresta
{
    private Neuronio origem;
    private Neuronio destino;
    private double peso;

    public Aresta(Neuronio origem, Neuronio destino, double peso) {
        this.origem = origem;
        this.destino = destino;
        this.peso = peso;
    }

    public Aresta()
    {
        this(null, null, 0.0);
    }

    public Neuronio getOrigem() {
        return origem;
    }

    public void setOrigem(Neuronio origem) {
        this.origem = origem;
    }

    public Neuronio getDestino() {
        return destino;
    }

    public void setDestino(Neuronio destino) {
        this.destino = destino;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }
}
