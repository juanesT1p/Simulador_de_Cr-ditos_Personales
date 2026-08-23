import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatButtonModule, MatToolbarModule, RouterLink, RouterLinkActive],
  template: `
    <mat-toolbar>
      <a mat-button routerLink="/simulacion" routerLinkActive="active-link">Simular Crédito</a>
      <a mat-button routerLink="/historico" routerLinkActive="active-link">Histórico</a>
    </mat-toolbar>
  `,
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {}
