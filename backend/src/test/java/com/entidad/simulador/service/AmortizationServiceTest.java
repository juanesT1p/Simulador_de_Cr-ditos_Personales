package com.entidad.simulador.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.entidad.simulador.dto.AmortizationRowDTO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class AmortizationServiceTest {

    private static final BigDecimal LOAN_AMOUNT = new BigDecimal("1000000.00");
    private static final BigDecimal EFFECTIVE_ANNUAL_RATE = new BigDecimal("12.00");
    private static final int TERM_MONTHS = 12;

    private final AmortizationService amortizationService = new AmortizationService();

    @Test
    void calculatesFixedMonthlyPaymentUsingEffectiveAnnualRate() {
        BigDecimal payment = amortizationService.calculateMonthlyPayment(
                LOAN_AMOUNT, EFFECTIVE_ANNUAL_RATE, TERM_MONTHS);

        // i = (1 + 12 / 100)^(1 / 12) - 1; cuota = 1,000,000 * i * (1 + i)^12 / ((1 + i)^12 - 1)
        assertEquals(new BigDecimal("88562.07"), payment);
    }

    @Test
    void amortizationCapitalPaymentsAddUpToOriginalLoanAmount() {
        AmortizationService.AmortizationPlan plan = calculatePlan();

        assertEquals(0, sumCapitalPayments(plan.rows()).compareTo(LOAN_AMOUNT));
    }

    @Test
    void amortizationEndsWithZeroRemainingBalance() {
        AmortizationService.AmortizationPlan plan = calculatePlan();
        List<AmortizationRowDTO> rows = plan.rows();

        assertEquals(0, rows.get(rows.size() - 1).remainingBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void totalInterestMatchesInterestSumFromAmortizationRows() {
        AmortizationService.AmortizationPlan plan = calculatePlan();

        BigDecimal interestFromRows = plan.rows().stream()
                .map(AmortizationRowDTO::interestPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, interestFromRows.compareTo(plan.totalInterest()));
    }

    private AmortizationService.AmortizationPlan calculatePlan() {
        return amortizationService.calculateAmortization(LOAN_AMOUNT, EFFECTIVE_ANNUAL_RATE, TERM_MONTHS);
    }

    private BigDecimal sumCapitalPayments(List<AmortizationRowDTO> rows) {
        return rows.stream()
                .map(AmortizationRowDTO::capitalPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
