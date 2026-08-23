import { Component } from '@angular/core';
import { SimulationFormComponent } from './features/simulation-form/simulation-form.component';
import { SimulationHistoryComponent } from './features/simulation-history/simulation-history.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [SimulationFormComponent, SimulationHistoryComponent],
  template: `
    <app-simulation-form />
    <app-simulation-history />
  `,
  styles: [],
})
export class AppComponent {
}
