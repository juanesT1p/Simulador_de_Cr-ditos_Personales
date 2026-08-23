import { Component } from '@angular/core';
import { SimulationFormComponent } from './features/simulation-form/simulation-form.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [SimulationFormComponent],
  template: `
    <app-simulation-form />
  `,
  styles: [],
})
export class AppComponent {
}
