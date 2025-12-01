package io.github.raphaelmun1z.LEA01.controller;

import io.github.raphaelmun1z.LEA01.entities.Processo;
import io.github.raphaelmun1z.LEA01.dto.ResultadoSimulacaoResponseDTO;
import io.github.raphaelmun1z.LEA01.service.EscalonadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escalonamento")
public class EscalonadorController {
    private final EscalonadorService escalonadorService;

    public EscalonadorController(EscalonadorService escalonadorService) {
        this.escalonadorService = escalonadorService;
    }

    @PostMapping("/prioridade-com-preempcao")
    public ResponseEntity<ResultadoSimulacaoResponseDTO> simularEscalonamentoPrioComPreempcao(@RequestBody List<Processo> processos) {
        ResultadoSimulacaoResponseDTO resultado = escalonadorService.simularPrioridade(processos);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/srtf")
    public ResponseEntity<ResultadoSimulacaoResponseDTO> simularEscalonamentoSRTF(@RequestBody List<Processo> processos) {
        ResultadoSimulacaoResponseDTO resultado = escalonadorService.simularSRTF(processos);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/rr-prioridade-envelhecimento")
    public ResponseEntity<ResultadoSimulacaoResponseDTO> simularEscalonamentoRRComPrioridade(@RequestBody List<Processo> processos) {
        ResultadoSimulacaoResponseDTO resultado = escalonadorService.simularRoundRobinPrioridadeEnvelhecimento(processos);
        return ResponseEntity.ok(resultado);
    }
}
