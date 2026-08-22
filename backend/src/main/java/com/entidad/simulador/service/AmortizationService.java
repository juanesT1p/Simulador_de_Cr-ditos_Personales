package com.entidad.simulador.service;

import com.entidad.simulador.dto.AmortizationRowDTO;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Calcula planes de crédito mediante el sistema de amortización francés.
 */
@Service
public class AmortizationService {

    private static final MathContext MATH_CONTEXT = new MathContext(34, RoundingMode.HALF_UP);
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;
    private static final int MONEY_SCALE = 2;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal ROOT_TOLERANCE = BigDecimal.ONE.scaleByPowerOfTen(-30);
    private static final int MAX_ROOT_ITERATIONS = 100;

    /**
     * Convierte una tasa efectiva anual expresada como porcentaje a su tasa
     * periódica mensual equivalente: (1 + EA / 100)^(1 / 12) - 1.
     */
    public BigDecimal calculateMonthlyPeriodicRate(BigDecimal effectiveAnnualRate) {
        requireNonNegative(effectiveAnnualRate, "La tasa efectiva anual no puede ser negativa.");

        BigDecimal annualRateDecimal = effectiveAnnualRate.divide(ONE_HUNDRED, MATH_CONTEXT);
        BigDecimal annualFactor = BigDecimal.ONE.add(annualRateDecimal, MATH_CONTEXT);
        return twelfthRoot(annualFactor).subtract(BigDecimal.ONE, MATH_CONTEXT);
    }

    /**
     * Calcula la cuota mensual fija de un préstamo con amortización francesa.
     */
    public BigDecimal calculateMonthlyPayment(
            BigDecimal loanAmount, BigDecimal effectiveAnnualRate, int termMonths) {
        validateInputs(loanAmount, effectiveAnnualRate, termMonths);

        BigDecimal monthlyRate = calculateMonthlyPeriodicRate(effectiveAnnualRate);
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return loanAmount.divide(BigDecimal.valueOf(termMonths), MONEY_SCALE, MONEY_ROUNDING);
        }

        BigDecimal periodicFactor = BigDecimal.ONE.add(monthlyRate, MATH_CONTEXT)
                .pow(termMonths, MATH_CONTEXT);
        BigDecimal numerator = loanAmount.multiply(monthlyRate, MATH_CONTEXT)
                .multiply(periodicFactor, MATH_CONTEXT);
        BigDecimal denominator = periodicFactor.subtract(BigDecimal.ONE, MATH_CONTEXT);

        return numerator.divide(denominator, MATH_CONTEXT).setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    /**
     * Genera el plan completo y sus totales. La última cuota ajusta únicamente
     * diferencias de centavos para que el saldo pendiente final sea cero.
     */
    public AmortizationPlan calculateAmortization(
            BigDecimal loanAmount, BigDecimal effectiveAnnualRate, int termMonths) {
        validateInputs(loanAmount, effectiveAnnualRate, termMonths);

        BigDecimal monthlyPayment = calculateMonthlyPayment(loanAmount, effectiveAnnualRate, termMonths);
        BigDecimal monthlyRate = calculateMonthlyPeriodicRate(effectiveAnnualRate);
        BigDecimal balance = loanAmount.setScale(MONEY_SCALE, MONEY_ROUNDING);
        BigDecimal totalInterest = BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING);
        List<AmortizationRowDTO> rows = new ArrayList<>(termMonths);

        for (int period = 1; period <= termMonths; period++) {
            BigDecimal interest = balance.multiply(monthlyRate, MATH_CONTEXT)
                    .setScale(MONEY_SCALE, MONEY_ROUNDING);
            BigDecimal capitalPayment;
            BigDecimal payment;

            if (period == termMonths) {
                capitalPayment = balance;
                payment = capitalPayment.add(interest, MATH_CONTEXT).setScale(MONEY_SCALE, MONEY_ROUNDING);
            } else {
                payment = monthlyPayment;
                capitalPayment = payment.subtract(interest, MATH_CONTEXT).setScale(MONEY_SCALE, MONEY_ROUNDING);
                if (capitalPayment.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("La cuota no cubre los intereses del período.");
                }
            }

            balance = balance.subtract(capitalPayment, MATH_CONTEXT).setScale(MONEY_SCALE, MONEY_ROUNDING);
            if (period == termMonths) {
                balance = BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING);
            }
            totalInterest = totalInterest.add(interest, MATH_CONTEXT).setScale(MONEY_SCALE, MONEY_ROUNDING);
            rows.add(new AmortizationRowDTO(period, capitalPayment, interest, payment, balance));
        }

        BigDecimal totalPayment = loanAmount.setScale(MONEY_SCALE, MONEY_ROUNDING)
                .add(totalInterest, MATH_CONTEXT)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
        return new AmortizationPlan(monthlyPayment, totalInterest, totalPayment, List.copyOf(rows));
    }

    private BigDecimal twelfthRoot(BigDecimal value) {
        BigDecimal estimate = value.compareTo(BigDecimal.ONE) >= 0 ? value : BigDecimal.ONE;
        for (int iteration = 0; iteration < MAX_ROOT_ITERATIONS; iteration++) {
            BigDecimal divisor = estimate.pow(11, MATH_CONTEXT);
            BigDecimal nextEstimate = estimate.multiply(BigDecimal.valueOf(11), MATH_CONTEXT)
                    .add(value.divide(divisor, MATH_CONTEXT), MATH_CONTEXT)
                    .divide(TWELVE, MATH_CONTEXT);
            if (nextEstimate.subtract(estimate, MATH_CONTEXT).abs().compareTo(ROOT_TOLERANCE) <= 0) {
                return nextEstimate;
            }
            estimate = nextEstimate;
        }
        return estimate;
    }

    private void validateInputs(BigDecimal loanAmount, BigDecimal effectiveAnnualRate, int termMonths) {
        requirePositive(loanAmount, "El monto solicitado debe ser mayor que cero.");
        requireNonNegative(effectiveAnnualRate, "La tasa efectiva anual no puede ser negativa.");
        if (termMonths <= 0) {
            throw new IllegalArgumentException("El plazo debe ser mayor que cero.");
        }
    }

    private void requirePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireNonNegative(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public record AmortizationPlan(
            BigDecimal monthlyPayment,
            BigDecimal totalInterest,
            BigDecimal totalPayment,
            List<AmortizationRowDTO> rows) {
    }
}
