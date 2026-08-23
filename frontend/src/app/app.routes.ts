import { Routes } from '@angular/router';
import { SimulationFormComponent } from './features/simulation-form/simulation-form.component';
import { SimulationHistoryComponent } from './features/simulation-history/simulation-history.component';

export const routes: Routes = [
  { path: 'simulacion', component: SimulationFormComponent },
  { path: 'historico', component: SimulationHistoryComponent },
  { path: '', pathMatch: 'full', redirectTo: 'simulacion' },
  { path: '**', redirectTo: 'simulacion' },
];
