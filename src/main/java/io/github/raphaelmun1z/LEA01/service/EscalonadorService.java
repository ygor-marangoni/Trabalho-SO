package io.github.raphaelmun1z.LEA01.service;

import io.github.raphaelmun1z.LEA01.entities.Processo;
import io.github.raphaelmun1z.LEA01.dto.ResultadoSimulacaoResponseDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EscalonadorService {

    public ResultadoSimulacaoResponseDTO simularPrioridade(List<Processo> entradaProcessos) {
        Comparator<Processo> estrategia = Comparator.comparingInt(Processo::getPrioridade)
            .thenComparingInt(Processo::getTempoChegada);

        return executarSimulacao(entradaProcessos, estrategia);
    }

    public ResultadoSimulacaoResponseDTO simularSRTF(List<Processo> entradaProcessos) {
        Comparator<Processo> estrategia = Comparator.comparingInt(Processo::getTempoRestante)
            .thenComparingInt(Processo::getTempoChegada);

        return executarSimulacao(entradaProcessos, estrategia);
    }

    private ResultadoSimulacaoResponseDTO executarSimulacao(List<Processo> entradaProcessos, Comparator<Processo> criterioOrdenacao) {
        // Criei uma nova lista com os processos apenas para facilitar as manipulações
        List<Processo> processos = new ArrayList<>();
        for (Processo p : entradaProcessos) {
            Processo novo = new Processo(p.getNomeProcesso(), p.getTempoChegada(), p.getDuracao(), p.getPrioridade());
            processos.add(novo);
        }

        // Essa lista é para identificar a ordem de execução
        List<String> historicoExecucao = new ArrayList<>();
        int tempoAtual = 0;
        int processosConcluidos = 0;
        int totalProcessos = processos.size();

        // Essa repetição simula o clock do processador
        while (processosConcluidos < totalProcessos) {
            int finalTempoAtual = tempoAtual;

            // Verifica quais processos estão prontos para serem processados
            List<Processo> filaProntos = processos.stream()
                .filter(p -> p.getTempoChegada() <= finalTempoAtual && !p.estaFinalizado())
                .collect(Collectors.toList());

            // Se nenhum estiver pronto nessa rodada, pula uma unidade de tempo
            if (filaProntos.isEmpty()) {
                historicoExecucao.add("Ocioso");
                tempoAtual++;
                continue;
            }

            // Aqui defini a ordem da execução
            filaProntos.sort(criterioOrdenacao);

            // Pega o primeiro processo
            Processo atual = filaProntos.getFirst();

            // Remove uma unidade de tempo do tempo restante do processo
            atual.executar();
            // Adiciona esse processo na lista que mostra a ordem de execução
            historicoExecucao.add(atual.getNomeProcesso());
            tempoAtual++;

            if (atual.estaFinalizado()) {
                processosConcluidos++;
                atual.calcularMetricas(tempoAtual);
            }
        }

        double mediaEspera = processos.stream()
            .mapToDouble(Processo::getTempoEspera)
            .average()
            .orElse(0.0);

        double mediaTurnaround = processos.stream()
            .mapToDouble(Processo::getTempoTurnaround)
            .average()
            .orElse(0.0);

        return new ResultadoSimulacaoResponseDTO(processos, historicoExecucao, mediaEspera, mediaTurnaround);
    }
}