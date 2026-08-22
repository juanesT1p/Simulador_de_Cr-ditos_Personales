package com.entidad.simulador.service;

import com.entidad.simulador.dto.SimulationRequestDTO;
import com.entidad.simulador.dto.SimulationResponseDTO;
import com.entidad.simulador.entity.Simulation;
import com.entidad.simulador.repository.SimulationRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final AmortizationService amortizationService;

    public SimulationService(
            SimulationRepository simulationRepository, AmortizationService amortizationService) {
        this.simulationRepository = simulationRepository;
        this.amortizationService = amortizationService;
    }

    public SimulationResponseDTO createSimulation(SimulationRequestDTO request) {
        AmortizationService.AmortizationPlan amortizationPlan = amortizationService.calculateAmortization(
                request.getLoanAmount(), request.getInterestRate(), request.getTermMonths());

        Simulation simulation = new Simulation();
        simulation.setClientName(request.getClientName());
        simulation.setLoanAmount(request.getLoanAmount());
        simulation.setInterestRate(request.getInterestRate());
        simulation.setTermMonths(request.getTermMonths());
        simulation.setMonthlyPayment(amortizationPlan.monthlyPayment());
        simulation.setTotalInterest(amortizationPlan.totalInterest());
        simulation.setTotalPayment(amortizationPlan.totalPayment());

        return toResponseDTO(simulationRepository.save(simulation));
    }

    public List<SimulationResponseDTO> getAllSimulations() {
        return simulationRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        Simulation::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponseDTO)
                .toList();
    }

    public List<SimulationResponseDTO> searchByClientName(String clientName) {
        return simulationRepository.findByClientNameContainingIgnoreCase(clientName).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<SimulationResponseDTO> searchByDateRange(LocalDateTime start, LocalDateTime end) {
        return simulationRepository.findByCreatedAtBetween(start, end).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private SimulationResponseDTO toResponseDTO(Simulation simulation) {
        return new SimulationResponseDTO(
                simulation.getId(),
                simulation.getClientName(),
                simulation.getLoanAmount(),
                simulation.getInterestRate(),
                simulation.getTermMonths(),
                simulation.getMonthlyPayment(),
                simulation.getTotalInterest(),
                simulation.getTotalPayment(),
                simulation.getCreatedAt());
    }
}
