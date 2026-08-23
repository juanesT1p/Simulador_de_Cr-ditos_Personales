# Simulador de Créditos Personales

Aplicación web para simular créditos personales mediante el sistema de amortización francesa. Permite calcular la cuota mensual, los intereses, el total a pagar y el plan de amortización, además de guardar y consultar simulaciones en PostgreSQL.

El proyecto integra un frontend Angular con un backend Spring Boot expuesto como API REST.

Repositorio: `simulador-creditos`

## Tecnologías

| Categoría | Tecnología |
| --- | --- |
| Frontend | Angular `18.2.14`, TypeScript `5.5.4`, Angular Material `18.2.14`, RxJS `7.8.2` |
| Backend | Java `17`, Spring Boot `3.3.13`, Spring Web, Spring Data JPA, Jakarta Validation, Lombok |
| Base de datos | PostgreSQL |
| Construcción | Maven, Angular CLI `18.2.21`, npm |
| Arquitectura | Arquitectura por capas, DTO, Repository, API REST |

Las versiones frontend corresponden a las instaladas en `frontend/package-lock.json`. En `frontend/package.json` se declaran los rangos `^18.2.0` para los paquetes principales de Angular, `^18.2.14` para Angular Material, `~5.5.2` para TypeScript y `~7.8.0` para RxJS. El driver de PostgreSQL, Spring Data JPA, Jakarta Validation y Lombok no tienen una versión independiente en `backend/pom.xml`; son administrados por Spring Boot `3.3.13`.

## Descripción

El simulador está dirigido a personas que necesitan estimar las condiciones de un crédito antes de solicitarlo. El usuario ingresa el nombre del cliente, el monto, la tasa efectiva anual y el plazo en meses. La aplicación calcula los valores financieros, muestra los resultados en pesos colombianos y permite guardar la simulación para consultarla posteriormente.

El frontend consume la API REST, aplica validaciones, muestra una tabla local de amortización y ofrece un histórico con filtros por nombre del cliente y rango de fechas.

## Características

- Cálculo de créditos con amortización francesa.
- Cálculo de cuota mensual, intereses totales y valor total a pagar.
- Visualización local del plan de amortización.
- Validaciones en frontend y backend.
- Persistencia de simulaciones en PostgreSQL.
- Histórico ordenado por fecha de creación.
- Filtros por nombre del cliente y rango de fechas.
- Actualización automática del histórico después de guardar una simulación.
- Navegación entre las vistas `/simulacion` y `/historico`.
- Interfaz responsive basada en Angular Material.
- Manejo centralizado de errores y configuración CORS.
- Pruebas unitarias para el motor de amortización y el servicio de simulaciones.

## Arquitectura

```text
Usuario
   │
   ▼
Frontend Angular
   │
   ▼
API REST Spring Boot
   │
   ▼
Controller
   │
   ▼
Service
   ├── AmortizationService
   └── SimulationService
   │
   ▼
Repository
   │
   ▼
PostgreSQL
```

El frontend envía solicitudes HTTP al backend. El `controller` recibe y valida las solicitudes mediante DTOs. La capa `service` contiene las reglas de negocio, calcula la amortización y coordina la persistencia. El `repository` accede a PostgreSQL mediante Spring Data JPA.

### Capas y responsabilidades

| Capa | Responsabilidad |
| --- | --- |
| `controller` | Expone los endpoints REST y construye las respuestas HTTP. |
| `service` | Implementa las reglas de negocio, los cálculos financieros y la consulta del histórico. |
| `repository` | Encapsula el acceso a datos mediante Spring Data JPA. |
| `dto` | Define los objetos de entrada y salida de la API. |
| `entity` | Representa las entidades JPA y el modelo persistido. |
| `exception` | Centraliza el manejo de errores y respuestas de validación. |
| `config` | Contiene la configuración CORS. |

### Decisiones de arquitectura

| Decisión | Descripción |
| --- | --- |
| Arquitectura por capas | Separa la entrada HTTP, la lógica de negocio y el acceso a datos. |
| DTO | Evita exponer directamente la entidad JPA en el contrato de la API. |
| Repository | Aísla las consultas y la persistencia de la lógica de negocio. |
| API REST | Permite que el frontend Angular consuma las operaciones del backend mediante HTTP. |
| Manejo centralizado de errores | Entrega respuestas consistentes para validaciones y errores internos. |

## Estructura del proyecto

```text
simulador-creditos/
├── backend/
│   ├── src/main/java/com/entidad/simulador/
│   │   ├── config/          Configuración CORS.
│   │   ├── controller/      Endpoints REST.
│   │   ├── service/         Reglas de negocio y amortización.
│   │   ├── repository/      Acceso a datos con Spring Data JPA.
│   │   ├── dto/             Objetos de transferencia y respuestas.
│   │   ├── entity/          Entidades JPA.
│   │   └── exception/       Manejo centralizado de excepciones.
│   ├── src/main/resources/
│   │   └── application.yml  Configuración de Spring Boot y PostgreSQL.
│   └── src/test/             Pruebas unitarias.
├── frontend/
│   └── src/app/
│       ├── core/
│       │   ├── models/       Interfaces y modelos TypeScript.
│       │   └── services/     Servicios HTTP y comunicación entre vistas.
│       ├── features/
│       │   ├── simulation-form/    Formulario y cálculo de simulaciones.
│       │   └── simulation-history/ Histórico y filtros.
│       └── shared/
│           └── navbar/       Barra de navegación reutilizable.
└── README.md
```

`core` contiene modelos y servicios transversales. `features` agrupa las funcionalidades principales de simulación e histórico. `shared` contiene componentes reutilizables, como la barra de navegación.

## Instalación

### Requisitos

- Node.js y npm compatibles con Angular 18.
- Java SDK 17 o superior.
- PostgreSQL en ejecución.
- Maven.

Verificar las instalaciones:

```bash
node --version
npm --version
java --version
mvn --version
psql --version
```

### Base de datos

Crear la base de datos antes de iniciar el backend:

```bash
sudo -u postgres psql -c "CREATE DATABASE simulador_creditos;"
```

Si la base de datos ya existe, no es necesario ejecutar nuevamente el comando. El usuario de PostgreSQL debe tener permisos para conectarse y crear o actualizar tablas.

La configuración actual se encuentra en `backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/simulador_creditos
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

El usuario y la contraseña pueden reemplazarse mediante `DB_USER` y `DB_PASSWORD`. Hibernate utiliza `ddl-auto: update` para crear o actualizar la tabla `simulations` automáticamente durante el desarrollo.

El archivo `backend/src/main/resources/schema.sql` existe como script SQL documental de referencia para la tabla `simulations`; no se ejecuta automáticamente, ya que `ddl-auto: update` continúa gestionando el esquema.

Como alternativa, la misma configuración puede expresarse en `application.properties`:

```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/simulador_creditos
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
```

Debe utilizarse uno de los dos formatos de configuración, no ambos simultáneamente con valores contradictorios.

### Backend

Desde la raíz del proyecto:

```bash
cd backend
mvn spring-boot:run
```

Backend:

`http://localhost:8080`

Pruebas y construcción:

```bash
mvn test
mvn clean package
```

### Frontend

En otra terminal:

```bash
cd frontend
npm install
npm start
```

Frontend:

`http://localhost:4200`

La URL de la API está configurada en `SimulationService` como `http://localhost:8080/api/simulations`.

Rutas disponibles:

| Ruta | Descripción |
| --- | --- |
| `/simulacion` | Formulario, cálculo y guardado de créditos. |
| `/historico` | Histórico y filtros de simulaciones. |

## Endpoints

Ruta base: `http://localhost:8080/api/simulations`

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `POST` | `/api/simulations` | Crea y guarda una simulación. |
| `GET` | `/api/simulations` | Lista el histórico completo. |
| `GET` | `/api/simulations?clientName=Juan` | Filtra por coincidencia parcial del nombre. |
| `GET` | `/api/simulations?startDate=2026-08-01T00:00:00&endDate=2026-08-22T23:59:59` | Filtra por rango de fechas ISO 8601. |

El histórico se ordena por fecha de creación descendente. Si se envía `clientName` junto con un rango de fechas, el backend prioriza el filtro por nombre. Si no se envían filtros, devuelve el histórico completo.

### Crear una simulación

Request:

```http
POST /api/simulations
Content-Type: application/json
```

```json
{
  "clientName": "Juan Pérez",
  "loanAmount": 20000000,
  "interestRate": 18.0,
  "termMonths": 36,
  "monthlyPayment": 709735.61,
  "totalInterest": 5550482.06,
  "totalPayment": 25550482.06
}
```

`monthlyPayment`, `totalInterest` y `totalPayment` forman parte del contrato de entrada, pero el backend los recalcula con `AmortizationService` antes de persistir.
La aplicación no confía en los valores recibidos desde el cliente para guardar la simulación.

Response `201 Created`:

```json
{
  "id": 1,
  "clientName": "Juan Pérez",
  "loanAmount": 20000000,
  "interestRate": 18.0,
  "termMonths": 36,
  "monthlyPayment": 709735.61,
  "totalInterest": 5550482.06,
  "totalPayment": 25550482.06,
  "createdAt": "2026-08-22T10:30:00"
}
```

Los valores calculados dependen del monto, la tasa efectiva anual y el plazo enviados.

### Consultar el histórico

Request:

```http
GET /api/simulations
```

Response `200 OK`:

```json
[
  {
    "id": 1,
    "clientName": "Juan Pérez",
    "loanAmount": 20000000,
    "interestRate": 18.0,
    "termMonths": 36,
    "monthlyPayment": 709735.61,
    "totalInterest": 5550482.06,
    "totalPayment": 25550482.06,
    "createdAt": "2026-08-22T10:30:00"
  }
]
```

Las consultas por nombre o por rango de fechas devuelven una lista con el mismo formato.

### Errores de validación

Los campos `clientName`, `loanAmount`, `interestRate` y `termMonths` son obligatorios. `clientName` usa `@NotBlank`; `loanAmount` e `interestRate` usan `@NotNull` y `@Positive`; y `termMonths` usa `@NotNull` y `@Min(1)`. En el frontend, el monto y el plazo deben ser mayores o iguales a uno y la tasa efectiva anual debe ser como mínimo `0,01`.

Una solicitud inválida devuelve `400 Bad Request`:

```json
{
  "timestamp": "2026-08-22T10:31:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La solicitud contiene datos inválidos.",
  "validationErrors": {
    "clientName": "no debe estar vacío",
    "loanAmount": "debe ser mayor que 0"
  }
}
```

Los errores internos devuelven `500 Internal Server Error` sin exponer detalles técnicos. El backend permite solicitudes CORS desde `http://localhost:4200` para `/api/**`, con los métodos `GET`, `POST`, `PUT`, `DELETE` y `OPTIONS`.

## Proceso de desarrollo

El proyecto se construyó por etapas, comenzando por el backend, continuando con el frontend y finalizando con la integración, el enrutamiento, el pulido visual y la revisión general.

### Backend

Se definió el alcance y se preparó la estructura inicial. Después se creó la entidad JPA `Simulation`, mapeada a la tabla `simulations` en PostgreSQL:

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

Los valores monetarios y las tasas se representan con `BigDecimal` para mantener precisión decimal. La tabla se genera y actualiza mediante Hibernate.

Se agregaron `SimulationRequestDTO` y `SimulationResponseDTO` para separar los datos de entrada y salida. Luego se creó `AmortizationService`, responsable de calcular la cuota mensual fija, los intereses, el total a pagar y la tabla completa de amortización. La tasa efectiva anual se convierte a tasa mensual periódica equivalente con `i_mensual = (1 + EA / 100)^(1 / 12) - 1`, conservando el interés compuesto.

Las pruebas unitarias validan la cuota de un caso conocido y la consistencia matemática del plan: los abonos a capital suman el monto solicitado, el saldo final es cero y el total de intereses coincide con la suma de intereses de las cuotas.

Posteriormente se creó `SimulationRepository`, con operaciones de `JpaRepository` y filtros por coincidencia parcial del nombre, sin distinguir mayúsculas y minúsculas, y por rango de fechas. `SimulationService` orquesta la creación, usa `AmortizationService`, persiste la entidad y devuelve `SimulationResponseDTO`. También obtiene y mapea el histórico ordenado por fecha descendente.

Finalmente se creó `SimulationController` con la ruta base `/api/simulations`. El endpoint `POST` recibe una solicitud validada y devuelve `201 Created`; el endpoint `GET` devuelve el histórico con `200 OK` y admite los filtros disponibles.

Durante esta etapa se resolvió el error `package com.entidad.simulador.entity does not exist`, causado porque `backend/src/main/java/com/entidad/simulador/entity/Simulation.java` aún no se había agregado al repositorio. También se resolvió el error `FATAL: no existe la base de datos "simulador_creditos"` creando la base de datos antes de iniciar la aplicación. Hibernate generó la tabla automáticamente y la persistencia se confirmó mediante `psql`.

### Errores y CORS

Se creó `GlobalExceptionHandler` mediante `@RestControllerAdvice`. Las validaciones devuelven `400 Bad Request` con `timestamp`, `status`, `error`, `message` y `validationErrors`; los errores no controlados devuelven `500 Internal Server Error` sin exponer detalles internos. También quedó preparada `SimulationNotFoundException` para respuestas futuras de `404 Not Found`.

La configuración CORS se centralizó para `/api/**`, permitiendo solicitudes desde `http://localhost:4200` y los métodos `GET`, `POST`, `PUT`, `DELETE` y `OPTIONS`. El preflight `OPTIONS` se verificó con respuesta `200 OK` y los encabezados correspondientes.

### Frontend e integración

Se crearon las interfaces `SimulationRequest`, `Simulation` y `ValidationErrorResponse` en `frontend/src/app/core/models`. `SimulationService`, ubicado en `frontend/src/app/core/services`, centraliza `createSimulation`, `getAllSimulations`, `searchByClientName` y `searchByDateRange`. La configuración standalone registra `provideHttpClient()` en `app.config.ts`.

Después se construyó `SimulationFormComponent` con Reactive Forms y Angular Material. El formulario captura el nombre, el monto, la tasa efectiva anual y el plazo. Al calcular una simulación válida, muestra la cuota fija, los intereses y el total a pagar con formato de pesos colombianos y configuración regional `es-CO`. El cálculo local entrega retroalimentación inmediata, mientras que el cálculo autoritativo y persistido permanece en `AmortizationService` del backend.

La tabla de amortización local presenta una fila por período con cuota, abono a capital, interés del mes, valor de la cuota y saldo pendiente. Los valores se redondean a dos decimales para reducir el arrastre de precisión de los números de punto flotante en JavaScript.

Luego se integró el guardado mediante `POST /api/simulations`. El formulario conserva el identificador y la fecha de creación, deshabilita el botón para evitar envíos duplicados y comunica los resultados mediante `MatSnackBar`. Los errores `400` muestran los mensajes de validación del backend y los demás errores muestran un mensaje genérico.

Por último se desarrolló la vista de histórico con Angular Material. La vista consulta `GET /api/simulations`, muestra fechas y valores monetarios formateados, permite buscar por nombre con una espera breve al escribir, filtrar por rango ISO 8601 y limpiar los filtros. También informa el estado de carga, la ausencia de registros y los errores de consulta.

### Fases completadas

| Fase | Resultado | Estado |
| --- | --- | --- |
| 0 | Definición del proyecto y alcance del simulador. | Completa |
| 1 | Configuración inicial de la estructura backend y frontend. | Completa |
| 2 | Entidad JPA `Simulation` y mapeo de la tabla `simulations`. | Completa |
| 3 | DTOs de solicitud y respuesta con validaciones. | Completa |
| 4 | Motor de amortización francesa y pruebas unitarias. | Completa |
| 5 | Repositorio y servicio de negocio para persistencia y consultas. | Completa |
| 6 | Controller REST para creación y consulta de simulaciones. | Completa |
| 7 | Manejo centralizado de errores y configuración CORS. | Completa |
| 8 | Verificación manual de endpoints REST y persistencia. | Completa |
| 9 | Preparación de la integración entre la aplicación Angular y la API. | Completa |
| 10 | Modelos TypeScript y servicio HTTP del frontend. | Completa |
| 11 | Formulario de simulación y cálculo local de resultados. | Completa |
| 12 | Tabla de amortización local. | Completa |
| 13 | Guardado de simulaciones desde el formulario. | Completa |
| 14 | Histórico de simulaciones y filtros. | Completa |
| 15 | Enrutamiento, navegación y navbar reutilizable. | Completa |
| 16 | Pulido visual de la interfaz con Angular Material. | Completa |
| 17 | Consolidación del README como documentación final. | Completa |
| 18 | Revisión final contra la rúbrica y corrección de hallazgos (redondeo del último periodo, contrato de solicitud completo, script SQL documental). | Completa |

## Objetivos

- Implementar un simulador de créditos personales con amortización francesa.
- Separar frontend, lógica de negocio, persistencia y contrato de la API.
- Proporcionar una consulta histórica con filtros y resultados formateados.
- Aplicar validaciones y manejo centralizado de errores.

## Estado actual

Las Fases 0 a 18 están completas. El proyecto está funcionalmente completo: el backend calcula y persiste simulaciones en PostgreSQL, el frontend consume la API y presenta el formulario, el plan de amortización y el histórico, y la interfaz cuenta con navegación, filtros y diseño responsive basado en Angular Material.
