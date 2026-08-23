import { CurrencyPipe, DatePipe, DecimalPipe, formatDate } from '@angular/common';
import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, Observable } from 'rxjs';

import { Simulation } from '../../core/models/simulation.model';
import { SimulationRefreshService } from '../../core/services/simulation-refresh.service';
import { SimulationService } from '../../core/services/simulation.service';

@Component({
  selector: 'app-simulation-history',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
  ],
  templateUrl: './simulation-history.component.html',
  styleUrl: './simulation-history.component.scss',
})
export class SimulationHistoryComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  readonly clientNameControl = new FormControl('', { nonNullable: true });
  readonly startDateControl = new FormControl<Date | null>(null);
  readonly endDateControl = new FormControl<Date | null>(null);
  readonly displayedColumns = ['createdAt', 'clientName', 'loanAmount', 'termMonths', 'interestRate', 'monthlyPayment'];

  simulations: Simulation[] = [];
  isLoading = false;

  constructor(
    private readonly simulationRefreshService: SimulationRefreshService,
    private readonly simulationService: SimulationService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.refreshHistory();

    this.simulationRefreshService.refreshRequested$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.refreshHistory());

    this.clientNameControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.searchByClientName());
  }

  refreshHistory(): void {
    this.loadSimulations(this.simulationService.getAllSimulations());
  }

  searchByClientName(): void {
    const clientName = this.clientNameControl.value.trim();

    if (!clientName) {
      this.loadSimulations(this.simulationService.getAllSimulations());
      return;
    }

    this.loadSimulations(this.simulationService.searchByClientName(clientName));
  }

  searchByDateRange(): void {
    const startDate = this.startDateControl.value;
    const endDate = this.endDateControl.value;

    if (!startDate || !endDate) {
      return;
    }

    this.loadSimulations(
      this.simulationService.searchByDateRange(
        this.toIsoDateTime(startDate, false),
        this.toIsoDateTime(endDate, true),
      ),
    );
  }

  clearFilters(): void {
    this.clientNameControl.setValue('', { emitEvent: false });
    this.startDateControl.reset();
    this.endDateControl.reset();
    this.loadSimulations(this.simulationService.getAllSimulations());
  }

  private loadSimulations(request: Observable<Simulation[]>): void {
    this.isLoading = true;

    request.subscribe({
      next: (simulations) => {
        this.simulations = simulations;
        this.isLoading = false;
      },
      error: () => {
        this.simulations = [];
        this.isLoading = false;
        this.snackBar.open('No fue posible cargar las simulaciones. Intenta nuevamente.', 'Cerrar', { duration: 6000 });
      },
    });
  }

  private toIsoDateTime(date: Date, isEndDate: boolean): string {
    const hours = isEndDate ? 23 : 0;
    const minutes = isEndDate ? 59 : 0;
    const seconds = isEndDate ? 59 : 0;
    const localDateTime = new Date(date.getFullYear(), date.getMonth(), date.getDate(), hours, minutes, seconds);

    return formatDate(localDateTime, "yyyy-MM-dd'T'HH:mm:ss", 'en-US');
  }
}
