import { CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

interface SimulationSummary {
  monthlyPayment: number;
  totalInterest: number;
  totalPayment: number;
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
    CurrencyPipe,
  ],
  templateUrl: './simulation-form.component.html',
  styleUrl: './simulation-form.component.scss',
})
export class SimulationFormComponent {
  private readonly formBuilder = inject(FormBuilder);

  readonly simulationForm = this.formBuilder.group({
    clientName: ['', Validators.required],
    loanAmount: [null, [Validators.required, Validators.min(1)]],
    interestRate: [null, [Validators.required, Validators.min(0.01)]],
    termMonths: [null, [Validators.required, Validators.min(1)]],
  });

  summary: SimulationSummary | null = null;
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
  }
}
