import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Simulation, SimulationRequest } from '../models/simulation.model';

@Injectable({ providedIn: 'root' })
export class SimulationService {
  private readonly apiUrl = 'http://localhost:8080/api/simulations';

  constructor(private readonly http: HttpClient) {}

  createSimulation(request: SimulationRequest): Observable<Simulation> {
    return this.http.post<Simulation>(this.apiUrl, request);
  }

  getAllSimulations(): Observable<Simulation[]> {
    return this.http.get<Simulation[]>(this.apiUrl);
  }

  searchByClientName(clientName: string): Observable<Simulation[]> {
    const params = new HttpParams().set('clientName', clientName);
    return this.http.get<Simulation[]>(this.apiUrl, { params });
  }

  searchByDateRange(startDate: string, endDate: string): Observable<Simulation[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<Simulation[]>(this.apiUrl, { params });
  }
}
