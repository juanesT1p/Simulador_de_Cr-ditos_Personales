import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';

import { Simulation, SimulationRequest, ValidationErrorResponse } from '../../core/models/simulation.model';
import { SimulationRefreshService } from '../../core/services/simulation-refresh.service';
import { SimulationService } from '../../core/services/simulation.service';

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
    MatSnackBarModule,
    MatTableModule,
    CurrencyPipe,
    DatePipe,
  ],
  templateUrl: './simulation-form.component.html',
  styleUrl: './simulation-form.component.scss',
})
export class SimulationFormComponent {
  private readonly formBuilder = inject(FormBuilder);

  constructor(
    private readonly simulationRefreshService: SimulationRefreshService,
    private readonly simulationService: SimulationService,
    private readonly snackBar: MatSnackBar,
  ) {}

  readonly simulationForm = this.formBuilder.group({
    clientName: ['', [Validators.required, Validators.pattern(/^[a-zA-ZÁÉÍÓÚáéíóúÑñ\s]+$/)]],
    loanAmount: [null, [Validators.required, Validators.min(1)]],
    interestRate: [null, [Validators.required, Validators.min(0.01)]],
    termMonths: [null, [Validators.required, Validators.min(1)]],
  });

  summary: SimulationSummary | null = null;
  amortizationRows: AmortizationRow[] = [];
  savedSimulation: Simulation | null = null;
  isSaving = false;
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
    const monthlyPayment = this.roundCurrency(
      monthlyRate === 0
        ? loanAmount / termMonths
        : loanAmount * ((monthlyRate * rateFactor) / (rateFactor - 1)),
    );

    let remainingBalance = this.roundCurrency(loanAmount);
    let totalInterest = 0;
    this.amortizationRows = [];

    for (let period = 1; period <= termMonths; period++) {
      const interestPayment = this.roundCurrency(remainingBalance * monthlyRate);
      let capitalPayment: number;
      let installment: number;

      if (period === termMonths) {
        capitalPayment = remainingBalance;
        installment = this.roundCurrency(capitalPayment + interestPayment);
        remainingBalance = 0;
      } else {
        installment = monthlyPayment;
        capitalPayment = this.roundCurrency(installment - interestPayment);
        remainingBalance = this.roundCurrency(remainingBalance - capitalPayment);
      }

      totalInterest = this.roundCurrency(totalInterest + interestPayment);

      this.amortizationRows.push({
        period,
        capitalPayment,
        interestPayment,
        installment,
        remainingBalance,
      });
    }

    const totalPayment = this.roundCurrency(loanAmount + totalInterest);
    this.summary = {
      monthlyPayment,
      totalInterest,
      totalPayment,
    };
    this.savedSimulation = null;
  }

  saveSimulation(): void {
    if (this.simulationForm.invalid || !this.summary || this.isSaving) {
      return;
    }

    const { clientName, loanAmount, interestRate, termMonths } = this.simulationForm.getRawValue();
    if (clientName === null || loanAmount === null || interestRate === null || termMonths === null) {
      return;
    }

    const request: SimulationRequest = {
      clientName,
      loanAmount,
      interestRate,
      termMonths,
      monthlyPayment: this.summary.monthlyPayment,
      totalInterest: this.summary.totalInterest,
      totalPayment: this.summary.totalPayment,
    };
    this.isSaving = true;

    this.simulationService.createSimulation(request).subscribe({
      next: (simulation) => {
        this.savedSimulation = simulation;
        this.isSaving = false;
        this.simulationRefreshService.requestRefresh();
        this.snackBar.open('Simulación guardada correctamente.', 'Cerrar', { duration: 4000 });
      },
      error: (error: HttpErrorResponse) => {
        this.isSaving = false;
        this.showSaveError(error);
      },
    });
  }

  private showSaveError(error: HttpErrorResponse): void {
    if (error.status === 400) {
      const validationResponse = error.error as ValidationErrorResponse;
      const validationMessages = validationResponse?.validationErrors
        ? Object.values(validationResponse.validationErrors)
        : [];

      if (validationMessages.length > 0) {
        this.snackBar.open(validationMessages.join(' '), 'Cerrar', { duration: 6000 });
        return;
      }
    }

    this.snackBar.open('Ocurrió un error al guardar la simulación. Intenta nuevamente.', 'Cerrar', { duration: 6000 });
  }

  private roundCurrency(value: number): number {
    return Math.round((value + Number.EPSILON) * 100) / 100;
  }
}
