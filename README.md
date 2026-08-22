# Simulador de Créditos Personales

## Estado del desarrollo

### Fase 2: entidad de simulación

Se creó la entidad JPA `Simulation`, mapeada a la tabla `simulations` en PostgreSQL.

La entidad contiene los siguientes campos:

| Campo | Tipo de dato | Descripción |
| --- | --- | --- |
| `id` | `Long` | Identificador de la simulación. |
| `clientName` | `String` | Nombre del cliente. |
| `loanAmount` | `BigDecimal` | Monto del crédito. |
| `interestRate` | `BigDecimal` | Tasa de interés. |
| `termMonths` | `Integer` | Plazo del crédito en meses. |
| `monthlyPayment` | `BigDecimal` | Valor de la cuota mensual. |
| `totalInterest` | `BigDecimal` | Total de intereses generados. |
| `totalPayment` | `BigDecimal` | Valor total a pagar. |
| `createdAt` | `LocalDateTime` | Fecha y hora de creación, administrada con `@CreationTimestamp`. |

Los valores monetarios y las tasas de interés se representan con `BigDecimal` para mantener precisión decimal.

La tabla `simulations` se genera y actualiza automáticamente mediante Hibernate con la propiedad `spring.jpa.hibernate.ddl-auto: update`. Por el momento no se requiere un script SQL manual.

### Fase 3: DTOs de simulación

Se agregaron los DTOs `SimulationRequestDTO` y `SimulationResponseDTO` para separar los datos de entrada y salida de las simulaciones.

`SimulationRequestDTO` recibe los datos proporcionados por el cliente. Aplica las siguientes validaciones: `clientName` es obligatorio y no puede estar vacío mediante `@NotBlank`; `loanAmount` e `interestRate` son obligatorios y deben ser positivos mediante `@NotNull` y `@Positive`; y `termMonths` es obligatorio y debe ser como mínimo uno mediante `@NotNull` y `@Min(1)`.

`SimulationResponseDTO` representa la información de una simulación guardada para su retorno al frontend, incluyendo los valores calculados y la fecha de creación. Los valores monetarios y las tasas de interés se representan con `BigDecimal`.

La persistencia real y la exposición de operaciones mediante controller y service se implementarán en fases posteriores.

### Fase 4: motor de amortización francesa

Se creó `AmortizationService` para calcular planes de pago de créditos personales mediante el sistema de amortización francesa. El servicio calcula la cuota mensual fija, el total de intereses, el total a pagar y la tabla completa de amortización con el abono a capital, el interés, el valor de la cuota y el saldo pendiente de cada período.

La tasa efectiva anual recibida como porcentaje se convierte a la tasa mensual periódica equivalente con la fórmula `i_mensual = (1 + EA / 100)^(1 / 12) - 1`. Esta conversión conserva el interés compuesto y no divide la tasa anual de forma simple entre doce.

Las pruebas unitarias validan la cuota calculada para un caso conocido y la consistencia matemática del plan de pagos: los abonos a capital suman el monto solicitado, el saldo final es cero y el total de intereses coincide con la suma de intereses de las cuotas.

### Fase 5: persistencia y servicio de simulaciones

Se creó `SimulationRepository`, una interfaz de Spring Data JPA encargada del acceso a las simulaciones almacenadas. Además de las operaciones estándar de `JpaRepository`, soporta filtros opcionales de histórico por coincidencia parcial del nombre del cliente, sin distinguir mayúsculas y minúsculas, y por rango de fechas de creación.

Se creó `SimulationService` como capa de negocio para orquestar la creación de simulaciones. El servicio usa `AmortizationService` para obtener los valores financieros, los asigna a la entidad `Simulation`, los guarda mediante el repositorio y devuelve los datos con `SimulationResponseDTO`. También obtiene y mapea el histórico completo, ordenado por fecha de creación descendente, y los resultados de los filtros disponibles.
