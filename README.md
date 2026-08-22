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
