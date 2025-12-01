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

        return executarSimulacao(entradaProcessos, estrategia, false, 0, 0);
    }

    public ResultadoSimulacaoResponseDTO simularSRTF(List<Processo> entradaProcessos) {
        Comparator<Processo> estrategia = Comparator.comparingInt(Processo::getTempoRestante)
            .thenComparingInt(Processo::getTempoChegada);

        return executarSimulacao(entradaProcessos, estrategia, false, 0, 0);
    }

    public ResultadoSimulacaoResponseDTO simularRoundRobinPrioridadeEnvelhecimento(List<Processo> entradaProcessos) {
        Comparator<Processo> estrategia = Comparator.comparingInt(Processo::getPrioridade)
            .thenComparingInt(Processo::getTempoChegada);

        return executarSimulacao(entradaProcessos, estrategia, true, 2, 1);
    }

    private ResultadoSimulacaoResponseDTO executarSimulacao(
        List<Processo> entradaProcessos,
        Comparator<Processo> criterioOrdenacao,
        boolean usarRoundRobin,
        int quantum,
        int fatorEnvelhecimento
    ) {
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

        // Trecho para auxiliar o algoritmo round robin
        Processo ultimoProcessoExecutado = null;
        int quantumAtual = 0;

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
                ultimoProcessoExecutado = null;
                continue;
            }

            // Aplica envelhecimento
            if (fatorEnvelhecimento > 0 && filaProntos.size() > 1) {
                for (Processo p : filaProntos) {
                    if (p != ultimoProcessoExecutado) {
                        int novaPrioridade = Math.max(0, p.getPrioridade() - fatorEnvelhecimento);
                        p.setPrioridade(novaPrioridade);
                    }
                }
            }

            // Aqui defini a ordem da execução
            filaProntos.sort(criterioOrdenacao);

            // Pega o primeiro processo
            Processo escolhido = filaProntos.get(0);

            if (usarRoundRobin) {
                // Se o último processo ainda está pronto e não estourou o quantum,
                // confirmo se ainda tem a melhor prioridade para continuar.
                boolean deveTrocar = true;

                if (ultimoProcessoExecutado != null && !ultimoProcessoExecutado.estaFinalizado()) {
                    if (quantumAtual < quantum) {
                        if (ultimoProcessoExecutado.getPrioridade() <= escolhido.getPrioridade()) {
                            escolhido = ultimoProcessoExecutado;
                            deveTrocar = false;
                        }
                    }
                }

                if (deveTrocar && escolhido != ultimoProcessoExecutado) {
                    quantumAtual = 0;
                }
            }

            // Remove uma unidade de tempo do tempo restante do processo
            escolhido.executar();
            // Adiciona esse processo na lista que mostra a ordem de execução
            historicoExecucao.add(escolhido.getNomeProcesso());

            // Atualiza lógica do quantum para a próxima iteração
            if (escolhido == ultimoProcessoExecutado) {
                quantumAtual++;
            } else {
                quantumAtual = 1;
                ultimoProcessoExecutado = escolhido;
            }

            tempoAtual++;

            if (escolhido.estaFinalizado()) {
                processosConcluidos++;
                escolhido.calcularMetricas(tempoAtual);
                ultimoProcessoExecutado = null;
                quantumAtual = 0;
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