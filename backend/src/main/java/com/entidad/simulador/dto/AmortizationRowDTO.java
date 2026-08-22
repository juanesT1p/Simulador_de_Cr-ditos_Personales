package com.entidad.simulador.dto;

import java.math.BigDecimal;

/**
 * Representa una cuota dentro de un plan de amortización.
 */
public record AmortizationRowDTO(
        int period,
        BigDecimal capitalPayment,
        BigDecimal interestPayment,
        BigDecimal payment,
        BigDecimal remainingBalance) {
}
