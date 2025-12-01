package io.github.raphaelmun1z.LEA01.entities;

public class Processo {
    private String nomeProcesso;
    private int tempoChegada;
    private int duracao;
    private int prioridade;

    private int tempoRestante;
    private int tempoConclusao;
    private int tempoEspera;
    private int tempoTurnaround;

    public Processo() {
    }

    public Processo(String nomeProcesso, int tempoChegada, int duracao, int prioridade) {
        this.nomeProcesso = nomeProcesso;
        this.tempoChegada = tempoChegada;
        this.duracao = duracao;
        this.prioridade = prioridade;
        this.tempoRestante = duracao;
    }

    public void executar() {
        if (tempoRestante > 0) {
            tempoRestante--;
        }
    }

    public boolean estaFinalizado() {
        return tempoRestante == 0;
    }

    public void calcularMetricas(int tempoAtual) {
        this.tempoConclusao = tempoAtual;
        this.tempoTurnaround = this.tempoConclusao - this.tempoChegada;
        this.tempoEspera = this.tempoTurnaround - this.duracao;
    }

    public String getNomeProcesso() {
        return nomeProcesso;
    }

    public void setNomeProcesso(String nomeProcesso) {
        this.nomeProcesso = nomeProcesso;
    }

    public int getTempoChegada() {
        return tempoChegada;
    }

    public void setTempoChegada(int tempoChegada) {
        this.tempoChegada = tempoChegada;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
        this.tempoRestante = duracao;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    public int getTempoRestante() {
        return tempoRestante;
    }

    public int getTempoConclusao() {
        return tempoConclusao;
    }

    public int getTempoEspera() {
        return tempoEspera;
    }

    public int getTempoTurnaround() {
        return tempoTurnaround;
    }
}
