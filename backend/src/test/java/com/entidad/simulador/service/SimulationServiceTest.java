package com.entidad.simulador.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.entidad.simulador.dto.SimulationRequestDTO;
import com.entidad.simulador.dto.SimulationResponseDTO;
import com.entidad.simulador.entity.Simulation;
import com.entidad.simulador.repository.SimulationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

    @Mock
    private SimulationRepository simulationRepository;

    @Mock
    private AmortizationService amortizationService;

    @InjectMocks
    private SimulationService simulationService;

    @Captor
    private ArgumentCaptor<Simulation> simulationCaptor;

    @Test
    void createSimulationSavesCalculatedValuesAndReturnsSavedDto() {
        SimulationRequestDTO request = new SimulationRequestDTO(
                "Ana Pérez", new BigDecimal("1500000.00"), new BigDecimal("12.00"), 12);
        AmortizationService.AmortizationPlan plan = new AmortizationService.AmortizationPlan(
                new BigDecimal("132843.10"),
                new BigDecimal("94117.20"),
                new BigDecimal("1594117.20"),
                List.of());
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 10, 30);
        Simulation savedSimulation = simulation(25L, "Ana Pérez", createdAt);
        savedSimulation.setMonthlyPayment(plan.monthlyPayment());
        savedSimulation.setTotalInterest(plan.totalInterest());
        savedSimulation.setTotalPayment(plan.totalPayment());

        when(amortizationService.calculateAmortization(
                request.getLoanAmount(), request.getInterestRate(), request.getTermMonths())).thenReturn(plan);
        when(simulationRepository.save(any(Simulation.class))).thenReturn(savedSimulation);

        SimulationResponseDTO response = simulationService.createSimulation(request);

        verify(simulationRepository).save(simulationCaptor.capture());
        Simulation simulationToSave = simulationCaptor.getValue();
        assertEquals(request.getClientName(), simulationToSave.getClientName());
        assertEquals(request.getLoanAmount(), simulationToSave.getLoanAmount());
        assertEquals(request.getInterestRate(), simulationToSave.getInterestRate());
        assertEquals(request.getTermMonths(), simulationToSave.getTermMonths());
        assertEquals(plan.monthlyPayment(), simulationToSave.getMonthlyPayment());
        assertEquals(plan.totalInterest(), simulationToSave.getTotalInterest());
        assertEquals(plan.totalPayment(), simulationToSave.getTotalPayment());
        assertEquals(25L, response.getId());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(plan.totalPayment(), response.getTotalPayment());
    }

    @Test
    void getAllSimulationsMapsResultsOrderedByMostRecentCreationDate() {
        Simulation older = simulation(1L, "Carlos Gómez", LocalDateTime.of(2026, 8, 20, 9, 0));
        Simulation newer = simulation(2L, "Laura Ruiz", LocalDateTime.of(2026, 8, 21, 9, 0));
        when(simulationRepository.findAll()).thenReturn(List.of(older, newer));

        List<SimulationResponseDTO> response = simulationService.getAllSimulations();

        assertEquals(2, response.size());
        assertEquals(2L, response.get(0).getId());
        assertEquals("Laura Ruiz", response.get(0).getClientName());
        assertEquals(1L, response.get(1).getId());
    }

    @Test
    void searchByClientNameDelegatesFilterAndMapsResults() {
        Simulation simulation = simulation(8L, "María López", LocalDateTime.of(2026, 8, 22, 8, 0));
        when(simulationRepository.findByClientNameContainingIgnoreCase("maría"))
                .thenReturn(List.of(simulation));

        List<SimulationResponseDTO> response = simulationService.searchByClientName("maría");

        verify(simulationRepository).findByClientNameContainingIgnoreCase("maría");
        assertEquals(1, response.size());
        assertEquals("María López", response.get(0).getClientName());
    }

    private Simulation simulation(Long id, String clientName, LocalDateTime createdAt) {
        Simulation simulation = new Simulation();
        simulation.setId(id);
        simulation.setClientName(clientName);
        simulation.setLoanAmount(new BigDecimal("1000000.00"));
        simulation.setInterestRate(new BigDecimal("10.00"));
        simulation.setTermMonths(12);
        simulation.setMonthlyPayment(new BigDecimal("87000.00"));
        simulation.setTotalInterest(new BigDecimal("44000.00"));
        simulation.setTotalPayment(new BigDecimal("1044000.00"));
        simulation.setCreatedAt(createdAt);
        return simulation;
    }
}
