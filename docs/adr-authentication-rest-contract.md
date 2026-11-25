# ADR: contrato REST de autenticacion de MaquiGest

- Estado: Aceptado para Fase 6 (A15), pendiente de validacion de regresion.
- Contexto: IAM.
- Referencia: Learning Center Platform Study Guide, seccion 7.1 (Stateless JWT Authentication).

## Contexto

MaquiGest registra cuentas de empresas arrendadoras y constructoras y autentica
usuarios mediante JWT. La API publicada y la WebApp utilizan
`POST /api/v1/authentication/sign-up` y `POST /api/v1/authentication/sign-in`.
El ejemplo Learning Center Platform utiliza `POST /api/v1/users` y
`POST /api/v1/tokens`. Esta diferencia de rutas no exige duplicar operaciones
ni modificar la logica de autenticacion.

## Decision

- Conservar `AuthenticationController`, ambos POST existentes y sus contratos
  de entrada y salida, assemblers, codigos HTTP y servicios de comandos.
- No incorporar aliases `/api/v1/users` ni `/api/v1/tokens`, ni migrar los
  consumidores de Angular solo por uniformidad con el ejemplo de referencia.
- Conservar el modelo de autenticacion JWT sin estado, la configuracion de
  Spring Security y las rutas existentes de verificacion de correo, recuperacion
  y restablecimiento de contrasena, y consulta del usuario autenticado.
- Mantener las convenciones DDD, CQRS, Result Pattern y Transformer Pattern
  independientemente de la eleccion de las rutas publicadas.

## Alternativas consideradas

1. Conservar el contrato actual: evita redundancia y cambios en consumidores.
2. Incorporar aliases: agrega dos operaciones sin una necesidad funcional.
3. Migrar a `/users` y `/tokens`: rompe el contrato anterior si no se coordina
   con los consumidores; no se justifica para este hallazgo.

## Consecuencias y verificacion

La API conserva sus 73 operaciones HTTP. La diferencia con Learning Center
Platform es intencional y queda documentada, no es una implementacion de sus
rutas de ejemplo. La regresion de Fase 6 debe comprobar los mappings de Spring
MVC, Swagger, seguridad, registro e inicio de sesion mediante las rutas actuales.
No se afirma conformidad con una exigencia explicita de esas rutas si el profesor
la establece posteriormente; en ese caso se evaluara una migracion coordinada.
