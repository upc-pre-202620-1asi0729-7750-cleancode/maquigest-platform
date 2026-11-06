# ADR — Eventos de dominio e integración internos en MaquiGest (Fase 5)

**Estado:** Adoptado (contratos in-process). **Contextos propietarios:** Profiles, Rentals y Maintenance.

## Decisión

1. Los agregados registran **domain events** en su propio `domain/model/events`. El adaptador JPA realiza `saveAndFlush` y después usa el componente compartido `DomainEventPublisher` para publicar una sola vez y limpiar los eventos registrados. **Profiles** asigna `profile.assignId(stored.getId())` y llama `profile.onCreated()` **antes** de publicar.
2. `CompanyProfileIntegrationEvent`, `RentalIntegrationEvent` y `MaintenanceIntegrationEvent` son contratos internos publicados desde `interfaces/events` en su *bounded context* propietario. Se conservan exactamente los nombres de propiedades, tipos y valores de evento actuales. Los bridges se mantienen bajo `infrastructure/events`.
3. Los bridges escuchan eventos de dominio con `@TransactionalEventListener(phase = AFTER_COMMIT)`. Una transacción revertida no produce notificaciones de integración. `ApplicationEventPublisher` entrega eventos **dentro del proceso** de Spring; no hay garantía de durabilidad frente a caída del proceso ni reintento de entrega.
4. No se cambian endpoints, payloads, permisos, estados, entidades ni esquema MySQL. Eventos de `MaintenanceCreatedEvent` e `IncidentReportedEvent` cuyo aggregateId original no se expone conservan `null` en el contrato, sin inventar IDs.

## Invariantes y pruebas

- `CompanyProfileRepositoryEventPublicationTests` verifica ID asignado antes del evento, publicación única y limpieza tras crear/actualizar.
- `DomainEventPublisherOnceTests` comprueba que despachar dos veces un agregado ya limpio no duplica el evento.
- `PublishedIntegrationEventBoundaryTests` comprueba contratos públicos y fase `AFTER_COMMIT` de todos los bridges.
- `IntegrationEventsAfterCommitMySqlTests` comprueba que el evento de integración no existe antes de commit, se publica después de commit y **no** se publica tras rollback usando el transaction manager real de Spring.
- Se mantiene la regresión de rollback de Fase 3 con la base temporal **`maquigest_ddd_fase3_*`** y las regresiones de Fase 4 (MySQL / concurrencia).

## Límites conocidos

- **No se implementa outbox, cola, notificación por correo ni entrega externa.** Si el proceso se cae después del commit y antes de ejecutar el bridge, un evento in-process podría perderse. Un envío durable requeriría outbox y un dispatcher externo, una decisión distinta.
- **Consumo idempotente no aplica aún:** no hay un consumidor persistente identificado que aplique eventos recibidos a una proyección propia. La deduplicación de consumo exige identificador de mensaje y almacenamiento del identificador procesado; no debe afirmarse sin implementación.
- Se preserva el orden de publicación observado en las transacciones de prueba locales, **sin prometer orden global** ni exactamente-una-vez fuera del proceso.
- Los eventos fuera de una transacción activa no llegan a bridges `AFTER_COMMIT` (`fallbackExecution=false` por defecto). Las escrituras de comandos ocurren en servicios `@Transactional` existentes; no se añade un fallback que pueda emitir eventos de una operación no confirmada.

## Alternativas descartadas

- Publicación directa del evento de integración desde repositorios: pierde la frontera `AFTER_COMMIT`.
- Cualquier cambio de `@TransactionalEventListener` a `@EventListener` en bridges: podría notificar antes de rollback.
- Outbox o infraestructura de mensajería en esta fase: cambia el alcance y requiere infraestructura/migración no acordadas.
