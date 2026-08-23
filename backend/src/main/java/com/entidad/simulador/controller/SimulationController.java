package com.entidad.simulador.controller;

import com.entidad.simulador.dto.SimulationRequestDTO;
import com.entidad.simulador.dto.SimulationResponseDTO;
import com.entidad.simulador.service.SimulationService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping
    public ResponseEntity<SimulationResponseDTO> createSimulation(
            @Valid @RequestBody SimulationRequestDTO request) {
        SimulationResponseDTO simulation = simulationService.createSimulation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(simulation);
    }

    @GetMapping
    public ResponseEntity<List<SimulationResponseDTO>> getSimulations(
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate) {
        List<SimulationResponseDTO> simulations;
        if (clientName != null) {
            simulations = simulationService.searchByClientName(clientName);
        } else if (startDate != null && endDate != null) {
            simulations = simulationService.searchByDateRange(startDate, endDate);
        } else {
            simulations = simulationService.getAllSimulations();
        }
        return ResponseEntity.ok(simulations);
    }
}
