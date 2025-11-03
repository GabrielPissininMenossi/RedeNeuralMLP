package fipp.muscleandiq.redeneuralmlp.entities;

public class Neuronio
{
    private String nome;
    private double i;
    private double erro;
    private double net;

    public Neuronio(String nome, double i, double erro, double net) {
        this.nome = nome;
        this.i = i;
        this.erro = erro;
        this.net = net;
    }

    public Neuronio()
    {
        this("",0.0, 0.0, 0.0);
    }
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getI() {
        return i;
    }

    public void setI(double i) {
        this.i = i;
    }

    public double getErro() {
        return erro;
    }

    public void setErro(double erro) {
        this.erro = erro;
    }

    public double getNet() {
        return net;
    }

    public void setNet(double net) {
        this.net = net;
    }
}
