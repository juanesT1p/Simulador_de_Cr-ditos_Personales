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

### Fase 6: controller REST de simulaciones

Se creó `SimulationController` para exponer las operaciones del simulador mediante la ruta base `/api/simulations`. El endpoint `POST /api/simulations` recibe una solicitud de simulación validada, delega su creación al servicio y devuelve `201 Created` con la simulación registrada.

El endpoint `GET /api/simulations` devuelve el histórico de simulaciones con `200 OK`. Acepta opcionalmente `clientName` para buscar coincidencias parciales de nombre sin distinguir mayúsculas y minúsculas, o `startDate` y `endDate` para filtrar por rango de fechas. Cuando se envían ambos tipos de filtro, se prioriza `clientName`.

### Problemas resueltos durante el desarrollo

Durante las Fases 5 y 6 se presentó el error de compilación `package com.entidad.simulador.entity does not exist`. La causa fue que el archivo `entity/Simulation.java`, creado en la Fase 2, no se había agregado al repositorio, aunque los DTOs y los servicios que lo referencian tenían las importaciones correctas. Se creó manualmente el archivo faltante en `backend/src/main/java/com/entidad/simulador/entity/Simulation.java` y la compilación se ejecutó nuevamente con éxito.

Al iniciar la aplicación con `mvn spring-boot:run` se presentó el error `FATAL: no existe la base de datos "simulador_creditos"`. La causa fue que la base de datos indicada en `spring.datasource.url` de `application.yml` no había sido creada en la instancia local de PostgreSQL. Se creó manualmente mediante el comando `sudo -u postgres psql -c "CREATE DATABASE simulador_creditos;"`. Después de crearla, Hibernate generó automáticamente la tabla `simulations` al iniciar la aplicación, conforme a la configuración `ddl-auto: update`.

Tras resolver ambas incidencias, se verificó manualmente el funcionamiento de los endpoints REST de la Fase 6. `POST /api/simulations` creó simulaciones y devolvió `201 Created` con los valores calculados `monthlyPayment`, `totalInterest` y `totalPayment`. `GET /api/simulations` devolvió `200 OK` con las simulaciones guardadas y `GET /api/simulations?clientName=...` filtró correctamente por nombre de cliente. La persistencia se confirmó consultando directamente la tabla `simulations` en PostgreSQL con `psql`.

Como nota pendiente, las solicitudes con datos inválidos, como un `loanAmount` negativo o un `clientName` vacío, actualmente devuelven un error `500` sin formato estructurado. Esta respuesta se resolverá en la Fase 7 mediante el manejo centralizado de excepciones con `@ControllerAdvice`.

### Fase 7: manejo de errores y CORS

Se creó `GlobalExceptionHandler` como manejo centralizado de excepciones mediante `@RestControllerAdvice`. Los errores de validación de solicitudes devuelven `400 Bad Request`, mientras que los errores no controlados devuelven `500 Internal Server Error` sin exponer detalles internos al cliente. También queda preparada la excepción `SimulationNotFoundException` para respuestas futuras de `404 Not Found`.

Las respuestas de error utilizan un cuerpo JSON estructurado con los campos `timestamp`, `status`, `error` y `message`. En el caso de errores de validación, se incluye adicionalmente un mapa de errores que relaciona cada campo con su mensaje de validación.

Se configuró CORS de forma centralizada para las rutas `/api/**`, permitiendo solicitudes desde `http://localhost:4200`. La configuración admite los métodos `GET`, `POST`, `PUT`, `DELETE` y `OPTIONS`, así como todos los encabezados requeridos por el frontend.

### Fase 8: prueba manual de endpoints REST

Se verificó manualmente el backend con el servidor local iniciado mediante `mvn spring-boot:run` y PostgreSQL activo. Una solicitud `POST /api/simulations` con `clientName` igual a `Juan Pérez`, `loanAmount` de `20000000`, `interestRate` de `18.0` y `termMonths` de `36` devolvió `201 Created`. La respuesta incluyó `monthlyPayment` con valor `709735.61`, `totalInterest` con valor `5550482.06`, `totalPayment` con valor `25550482.06`, además de los valores `id` y `createdAt` generados automáticamente.

También se validaron los escenarios de error. Un `POST /api/simulations` con `clientName` vacío y `loanAmount` negativo devolvió `400 Bad Request` con un cuerpo estructurado que incluye `validationErrors`, detallando el campo y el mensaje de cada violación. Una solicitud con cuerpo no JSON o malformado devolvió `400 Bad Request` con un mensaje genérico de formato inválido, sin exponer el stacktrace interno.

La consulta `GET /api/simulations` devolvió `200 OK` con la lista de simulaciones persistidas. La consulta `GET /api/simulations?clientName=...` devolvió `200 OK` con los resultados filtrados correctamente mediante coincidencia parcial e ignorando mayúsculas y minúsculas.

Se comprobó el preflight CORS mediante una solicitud `OPTIONS` desde el origen `http://localhost:4200`. La respuesta devolvió `200 OK` e incluyó los encabezados `Access-Control-Allow-Origin` y `Access-Control-Allow-Methods`, confirmando que el frontend Angular puede consumir la API sin bloqueos de CORS.

Como verificación adicional, los datos persistidos se confirmaron directamente en PostgreSQL mediante una consulta SQL a la tabla `simulations`. Todos los endpoints del backend quedaron validados manualmente y el backend está listo para ser consumido por el frontend Angular en las fases siguientes.

### Fase 10: modelos y servicio HTTP del frontend

Se crearon las interfaces TypeScript `SimulationRequest`, `Simulation` y `ValidationErrorResponse` dentro de `frontend/src/app/core/models`. Estas interfaces tipan los datos enviados y recibidos por la API, incluidos los valores calculados de una simulación y el cuerpo estructurado de los errores de validación.

Se creó `SimulationService` en `frontend/src/app/core/services` para centralizar el consumo HTTP de simulaciones. El servicio expone `createSimulation`, que corresponde a `POST /api/simulations`; `getAllSimulations`, que corresponde a `GET /api/simulations`; `searchByClientName`, que envía el parámetro `clientName`; y `searchByDateRange`, que envía los parámetros `startDate` y `endDate` al endpoint de consulta.

La configuración standalone de Angular registra `provideHttpClient()` en `app.config.ts` para habilitar `HttpClient` en los servicios de la aplicación. La URL base de la API se mantiene en el servicio y queda preparada para trasladarse a una configuración de entornos cuando el proyecto la incorpore.
