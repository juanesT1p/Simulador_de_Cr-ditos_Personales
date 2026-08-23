export interface SimulationRequest {
  clientName: string;
  loanAmount: number;
  interestRate: number;
  termMonths: number;
}

export interface Simulation {
  id: number;
  clientName: string;
  loanAmount: number;
  interestRate: number;
  termMonths: number;
  monthlyPayment: number;
  totalInterest: number;
  totalPayment: number;
  createdAt: string;
}

export interface ValidationErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  validationErrors: Record<string, string> | null;
}
