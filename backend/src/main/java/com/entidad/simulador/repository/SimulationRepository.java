package com.entidad.simulador.repository;

import com.entidad.simulador.entity.Simulation;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationRepository extends JpaRepository<Simulation, Long> {

    List<Simulation> findByClientNameContainingIgnoreCase(String clientName);

    List<Simulation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
