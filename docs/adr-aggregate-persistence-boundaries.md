# ADR: limites de agregados y persistencia de Rentals/Maintenance

- Estado: **Aceptado para Fase 4** (decision conservadora, sin migracion de datos)
- Contextos: `rentals`, `maintenance`
- Base de revision: despues de Fase 3 (`develop`, PR de `feature/ddd-cqrs-result-error-boundary`)

## Contexto

En Rentals, `Rental` es la raiz de agregado y gobierna `CONFIRMED -> ACTIVE -> COMPLETED`.
`Delivery` y `EquipmentReturn` son **entidades hijas del ciclo de vida** de cada `Rental`,
identificadas por `rentalId`. Se mantienen repositorios propios para consultar historicos y
persistir registros sin reconstruir completamente el agregado; esto **no** les da una
transicion de negocio independiente. Las escrituras se originan en
`RentalLifecycleTransactionalExecutor` y comparten transaccion con el root y los cambios
de Inventory. Las restricciones `UNIQUE (rental_id)` de `deliveries` y
`equipment_returns` permiten como maximo una entrega y una devolucion por alquiler.

En Maintenance, `Maintenance` es la raiz que controla el estado y la asignacion;
`MaintenanceActivity` es una **entidad hija de historial** con cardinalidad 1:N para
trazar los cambios. Su repositorio separado permite listar cronologicamente las
actividades y escribirlas en la misma transaccion del comando de Maintenance.

## Decision

1. **Conservar** las raices y los repositorios separados; no convertir entregas,
   devoluciones ni actividades en agregados independientes.
2. Conservar el bloqueo `findByIdForUpdate` del `Rental` y el bloqueo de Inventory
   dentro de la misma transaccion al entregar/devolver. Prohibir segundas escrituras
   inconsistentes y mantener la idempotencia exacta de solicitudes repetidas.
3. Conservar la dependencia de `MaintenanceActivity` respecto de su mantenimiento,
   la atomicidad de `MaintenanceTransactionalExecutor` y el historial append-only en
   operaciones normales (sin API para cambiar/eliminar actividades).
4. **No agregar columnas ni claves foraneas mediante `ddl-auto=update`**. En el esquema
   actual, las relaciones de `deliveries`, `equipment_returns` y `maintenance_activities`
   se identifican por columnas `rental_id`/`maintenance_id`, sin nueva FK en esta fase.
   El control de existencia se realiza en el caso de uso; la integridad referencial
   de la base se revisara solo mediante migracion explicita, si el equipo la aprueba.

## Auditoria de entidades JPA (sin cambios de esquema)

| Entidad persistida | Clasificacion | Decision sobre auditoria |
| --- | --- | --- |
| `Role` | Catalogo de autorizacion IAM | Sin campos de auditoria nuevos |
| `Incident` | Raiz de negocio de incidencia | Sin alteracion de columnas existentes |
| `Maintenance` | Raiz de negocio | Preservar el esquema actual |
| `MaintenanceActivity` | Historial subordinado | `occurred_at` ya representa ocurrencia de actividad |
| `RentalRequest` | Raiz de solicitud | `created_at` existente, sin columna nueva |
| `Rental` | Raiz de alquiler | Preservar estado y relacion con request |
| `Delivery` | Registro hijo de Rental | `delivered_at` existente |
| `EquipmentReturn` | Registro hijo de Rental | `returned_at` existente |
| `RentalRequestSubmissionGuard` | Tabla tecnica de exclusion mutua | No es agregado; no necesita auditoria uniforme |

No se fuerza `AuditableModel` sobre toda entidad JPA: eso alteraria columnas y potencialmente
datos existentes. Una ampliacion futura requeriria comparacion del esquema, una migracion
versionada y `ddl-auto=validate` con datos preexistentes.

## Consecuencias y riesgos residuales

- Ventaja: se conservan APIs, payloads, seguridad, eventos y transacciones ya probadas.
- El modelo protege transiciones, orden temporal e idempotencia exacta por el root y el
  caso de uso. El indice unico protege frente a duplicados aun ante concurrencia.
- Los repositorios de entidades hijas siguen siendo puertos tecnicos; **no deben**
  exponerse como command services independientes para saltarse el ciclo de vida.
- Riesgo documentado: al no existir FK nuevas, un SQL ajeno a los command services
  podria escribir referencias huerfanas. Se mantiene como limitacion conocida para
  evitar cambios peligrosos de esquema durante este cierre.
- El criterio de cierre se verifica con unit tests y pruebas de integridad sobre una
  **base MySQL temporal aislada**, con transacciones revertidas; no se modifica
  `main` ni la base de datos habitual.
