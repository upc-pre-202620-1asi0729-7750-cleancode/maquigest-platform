# MaquiGest — Platform

**CleanCode · Aplicaciones Open Source · UPC · NRC 7750**

## Descripción

MaquiGest es una plataforma SaaS para la gestión y el alquiler de maquinaria de construcción, dirigida a empresas arrendadoras y constructoras. Este repositorio implementa la API REST, las reglas de negocio, la seguridad y la persistencia de [MaquiGest WebApp](https://github.com/upc-pre-202620-1asi0729-7750-cleancode/maquigest-webapp).

## Tecnologías

Java 27, Spring Boot 4.1.2, Maven Wrapper 3.9.11, MySQL 8, Spring Data JPA, Spring Security, JWT, Jakarta Validation y Swagger/OpenAPI.

## Arquitectura y Bounded Contexts

La plataforma es un monolito modular basado en Domain-Driven Design (DDD), arquitectura hexagonal y CQRS. El paquete raíz es `com.maquigest.platform`.

| Bounded Context | Responsabilidad |
|---|---|
| IAM | Registro, autenticación, verificación de correo y recuperación de contraseña. |
| Profiles | Gestión de perfiles empresariales. |
| Inventory | Gestión de maquinaria, categorías, estado y disponibilidad. |
| Rentals | Solicitudes, reprogramación, aprobación, entrega y devolución de maquinaria. |
| Maintenance | Mantenimientos, incidencias e historial de equipos. |
| Subscriptions | Planes, suscripciones y checkout de prueba. |

El módulo `shared` contiene abstracciones transversales e infraestructura común; no constituye un Bounded Context de negocio adicional.

| Capa | Responsabilidad |
|---|---|
| `domain` | Aggregates, Entities, Value Objects, Commands, Queries y contratos de repositorio. |
| `application` | Servicios CQRS, coordinación de casos de uso y manejo de eventos. |
| `infrastructure` | Adaptadores, entidades JPA, repositorios de persistencia y servicios externos. |
| `interfaces` | Controllers REST, Resources, Assemblers y contratos publicados. |

### Patrones arquitectónicos

- **CQRS:** separación de Commands y Queries mediante contratos de servicio y métodos `handle`.
- **Result Pattern:** representación de resultados y errores esperados con `Result<T, ApplicationError>` en los servicios de comandos.
- **Transformer Pattern:** Assemblers para traducir entre Resources, Commands, modelos de dominio y persistencia, según corresponda.
- **OHS y ACL:** fachadas públicas y adaptadores consumidores que protegen los límites de los contextos.
- **Published Language:** contratos públicos de integración mediante fachadas, eventos y recursos REST.
- **Domain Events e Integration Events:** coordinación de hechos de negocio mediante handlers y contratos de integración internos.

### Context Mapping

| Consumidor | Proveedor | Contrato público |
|---|---|---|
| Profiles y Rentals | IAM | `IamCompanyDirectoryFacade` |
| Subscriptions | IAM | `IamSubscriberFacade` |
| Inventory, Profiles, Rentals, Maintenance y Subscriptions | IAM | `IamActorFacade` |
| Rentals | Inventory | `InventoryRentalFacade` |
| Rentals y Maintenance | Subscriptions | `SubscriptionsContextFacade` |
| Maintenance | Inventory | `InventoryMaintenanceFacade` |
| Maintenance | Rentals | `RentalsMaintenanceFacade` |

## Decisiones arquitectónicas (ADR)

### ADR 01 — Propiedad de las notificaciones

**Estado:** Aceptado e implementado.

#### Contexto

MaquiGest necesita comunicar hechos de negocio a las empresas arrendadoras y constructoras. El alcance del proyecto define seis Bounded Contexts, cada uno con responsabilidades y lenguaje propios. Las notificaciones acompañan sus procesos de negocio; no se definió la gestión de comunicaciones como un subdominio independiente.

#### Decisión y justificación

Se implementa un aggregate root `Notification` dentro de cada Bounded Context. Cada agregado tiene identidad, destinatario, referencia al hecho de negocio, tipo y estado de lectura propios, junto con sus repositorios, Commands, Queries y servicios.

Esta decisión preserva el **Ubiquitous Language**, porque cada contexto determina el significado de sus avisos y sus reglas de generación y destinatarios. Favorece la cohesión y la encapsulación: los hechos de negocio se comunican desde el modelo que los conoce, sin depender de los modelos internos de otros contextos.

La frontera de un agregado delimita sus invariantes y ciclo de vida, mientras que la de un Bounded Context delimita un modelo de dominio. Por ello, una notificación puede tener su propio agregado sin requerir un contexto adicional. Marcar un aviso como leído no obliga a modificar el agregado que originó el hecho.

Un Bounded Context independiente de notificaciones sería una alternativa válida si la gestión de comunicaciones tuviera reglas y un modelo propios. En el alcance actual, mantener agregados locales evita introducir esa frontera y contratos adicionales sin una necesidad de negocio específica. Tampoco se utiliza un agregado global en `shared`: allí se comparten únicamente abstracciones técnicas, como el puerto `EmailGateway`.

#### Implementación

| Contexto | Notificaciones | Recurso REST bajo `/api/v1` |
|---|---|---|
| IAM | Registro de cuenta. | `/iam-notifications` |
| Profiles | Creación y actualización del perfil. | `/profile-notifications` |
| Inventory | Registro y cambio de estado de maquinaria. | `/inventory-notifications` |
| Rentals | Solicitud creada, aprobada, rechazada, entrega y devolución. | `/rental-notifications` |
| Maintenance | Mantenimiento programado y completado; incidencia reportada y resuelta. | `/maintenance-notifications` |
| Subscriptions | Activación y cancelación de suscripción. | `/subscriptions-notifications` |

Cada contexto posee su propio `NotificationType`, repositorio, tabla de persistencia y recursos REST. Los handlers locales crean notificaciones mediante `@TransactionalEventListener(BEFORE_COMMIT)`; el envío de correo de alquileres se procesa mediante un handler `AFTER_COMMIT`. La consulta y el marcado de lectura están restringidos al destinatario autenticado.

**Ejemplo:** cuando una arrendadora aprueba una solicitud, Rentals genera `RENTAL_REQUEST_APPROVED`. La constructora puede consultar el aviso y marcarlo como leído sin alterar el estado del alquiler. La responsabilidad sobre la notificación permanece en Rentals.

## Seguridad e internacionalización

La API utiliza Spring Security y JWT Bearer para las operaciones protegidas. El registro y el inicio de sesión se realizan mediante `POST /api/v1/authentication/sign-up` y `POST /api/v1/authentication/sign-in`. Los mensajes localizados admiten inglés y español; se puede solicitar español con `Accept-Language: es-PE`.

## Ejecución local (Windows PowerShell)

Requisitos: JDK 27 configurado en `JAVA_HOME`, Git y MySQL 8 en ejecución.

```powershell
git clone https://github.com/upc-pre-202620-1asi0729-7750-cleancode/maquigest-platform.git
cd maquigest-platform
git switch develop
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DATABASE_URL = 'localhost'
$env:DATABASE_PORT = '3306'
$env:DATABASE_NAME = 'maquigest-platform-dev'
$env:DATABASE_USER = 'root'
$credential = Get-Credential -UserName $env:DATABASE_USER -Message 'Credenciales MySQL'
$env:DATABASE_PASSWORD = $credential.GetNetworkCredential().Password
$bytes = New-Object byte[] 64
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
.\mvnw.cmd spring-boot:run
```

Utilizar una base exclusiva de desarrollo: el perfil `dev` permite actualizar el esquema. No compartir ni versionar credenciales. Para conservar sesiones entre reinicios, gestionar un secreto JWT estable de forma segura.

| Recurso | URL local |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI | http://localhost:8080/v3/api-docs |
| WebApp | http://localhost:4200 |

## Integraciones externas

Las integraciones opcionales se habilitan mediante variables de entorno y requieren credenciales del servidor.

| Servicio | Funcionalidad | Variables principales |
|---|---|---|
| SendGrid | Correo de alquileres, verificación y recuperación. | `MAQUIGEST_EMAIL_ENABLED`, `SENDGRID_API_KEY`, `SENDGRID_FROM_EMAIL` |
| Stripe | Checkout de suscripciones en sandbox. | `MAQUIGEST_STRIPE_ENABLED`, `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET` |
| Google Maps | Geocodificación de direcciones de planificación. | `MAQUIGEST_MAPS_ENABLED`, `GOOGLE_MAPS_API_KEY` |

## Pruebas

Ejecutar sobre un esquema desechable, comprobando primero la configuración efectiva de conexión y sin utilizar datos ni credenciales de producción.

```powershell
$env:DATABASE_NAME = 'maquigest_documented_test_local'
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:MAQUIGEST_EMAIL_ENABLED = 'false'
$env:MAQUIGEST_IAM_EMAIL_VERIFICATION_REQUIRED = 'false'
$env:MAQUIGEST_STRIPE_ENABLED = 'false'
$env:MAQUIGEST_MAPS_ENABLED = 'false'
.\mvnw.cmd clean verify
```

El proyecto utiliza GitFlow y Semantic Versioning.
