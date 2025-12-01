package io.github.raphaelmun1z.LEA01.dto;

import io.github.raphaelmun1z.LEA01.entities.Processo;

import java.util.List;

public class ResultadoSimulacaoResponseDTO {
    private List<Processo> processosProcessados;
    private List<String> historicoExecucao;
    private double tempoMedioEspera;
    private double tempoMedioTurnaround;

    public ResultadoSimulacaoResponseDTO(List<Processo> processosProcessados, List<String> historicoExecucao, double tempoMedioEspera, double tempoMedioTurnaround) {
        this.processosProcessados = processosProcessados;
        this.historicoExecucao = historicoExecucao;
        this.tempoMedioEspera = tempoMedioEspera;
        this.tempoMedioTurnaround = tempoMedioTurnaround;
    }

    public List<Processo> getProcessosProcessados() {
        return processosProcessados;
    }

    public List<String> getHistoricoExecucao() {
        return historicoExecucao;
    }

    public double getTempoMedioEspera() {
        return tempoMedioEspera;
    }

    public double getTempoMedioTurnaround() {
        return tempoMedioTurnaround;
    }
}
