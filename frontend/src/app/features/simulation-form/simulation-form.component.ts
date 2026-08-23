import { CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';

interface SimulationSummary {
  monthlyPayment: number;
  totalInterest: number;
  totalPayment: number;
}

interface AmortizationRow {
  period: number;
  capitalPayment: number;
  interestPayment: number;
  installment: number;
  remainingBalance: number;
}

@Component({
  selector: 'app-simulation-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    CurrencyPipe,
  ],
  templateUrl: './simulation-form.component.html',
  styleUrl: './simulation-form.component.scss',
})
export class SimulationFormComponent {
  private readonly formBuilder = inject(FormBuilder);

  readonly simulationForm = this.formBuilder.group({
    clientName: ['', [Validators.required, Validators.pattern(/^[a-zA-ZÁÉÍÓÚáéíóúÑñ\s]+$/)]],
    loanAmount: [null, [Validators.required, Validators.min(1)]],
    interestRate: [null, [Validators.required, Validators.min(0.01)]],
    termMonths: [null, [Validators.required, Validators.min(1)]],
  });

  summary: SimulationSummary | null = null;
  amortizationRows: AmortizationRow[] = [];
  readonly displayedColumns = ['period', 'capitalPayment', 'interestPayment', 'installment', 'remainingBalance'];

  calculate(): void {
    if (this.simulationForm.invalid) {
      this.simulationForm.markAllAsTouched();
      return;
    }

    const { loanAmount, interestRate, termMonths } = this.simulationForm.getRawValue();
    if (loanAmount === null || interestRate === null || termMonths === null) {
      return;
    }

    const monthlyRate = Math.pow(1 + interestRate / 100, 1 / 12) - 1;
    const rateFactor = Math.pow(1 + monthlyRate, termMonths);
    const monthlyPayment = loanAmount * ((monthlyRate * rateFactor) / (rateFactor - 1));
    const totalPayment = monthlyPayment * termMonths;

    this.summary = {
      monthlyPayment,
      totalInterest: totalPayment - loanAmount,
      totalPayment,
    };

    let remainingBalance = this.roundCurrency(loanAmount);
    this.amortizationRows = [];

    for (let period = 1; period <= termMonths; period++) {
      const interestPayment = this.roundCurrency(remainingBalance * monthlyRate);
      const capitalPayment = this.roundCurrency(monthlyPayment - interestPayment);
      remainingBalance = this.roundCurrency(remainingBalance - capitalPayment);

      if (Math.abs(remainingBalance) <= 0.01) {
        remainingBalance = 0;
      }

      this.amortizationRows.push({
        period,
        capitalPayment,
        interestPayment,
        installment: this.roundCurrency(monthlyPayment),
        remainingBalance,
      });
    }
  }

  private roundCurrency(value: number): number {
    return Math.round((value + Number.EPSILON) * 100) / 100;
  }
}
