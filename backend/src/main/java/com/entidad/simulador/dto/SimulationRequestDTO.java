package com.entidad.simulador.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequestDTO {

    @NotBlank
    private String clientName;

    @NotNull
    @Positive
    private BigDecimal loanAmount;

    @NotNull
    @Positive
    private BigDecimal interestRate;

    @NotNull
    @Min(1)
    private Integer termMonths;

    // Se reciben por compatibilidad del contrato; el backend recalcula estos valores como fuente de verdad.
    private BigDecimal monthlyPayment;

    private BigDecimal totalInterest;

    private BigDecimal totalPayment;

    public SimulationRequestDTO(
            String clientName, BigDecimal loanAmount, BigDecimal interestRate, Integer termMonths) {
        this.clientName = clientName;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
    }
}
