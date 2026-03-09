# HALLAZGOS DE AUDITORÍA DE CÓDIGO (CODE REVIEW FINDINGS)

Este documento detalla los resultados del análisis profundo (Deep Analysis) ejecutado sobre el repositorio del sistema eCommerce (Frontend y Backend). Se han identificado hallazgos relacionados con la consistencia entre la API y las pruebas en Postman, riesgos de seguridad, potenciales bugs y deuda técnica.

# TAREA 9 — VALIDACIÓN DE CONSISTENCIA API vs POSTMAN

Tras contrastar los endpoints existentes en la aplicación Spring Boot (*Controllers*) con la colección `ECommerce_Collection.json`, se han detectado las siguientes observaciones:

## 1. Endpoints Faltantes en Postman
* **Direcciones**: No existen pruebas creadas para el CRUD completo de rutas como `GET /api/direcciones`, `POST /api/direcciones`, ni `GET /api/direcciones/mis-direcciones`.
* **Cancelación de Órdenes**: El endpoint `POST /api/ordenes/{id}/cancelar` está expuesto en el `OrdenController`, pero la colección actual de pruebas no lo invoca.
* **Listado de Órdenes de Usuario Específico**: El controlador permite a un ADMIN ver las órdenes de cualquier usuario (`GET /api/ordenes/usuario/{usuarioId}`), sin embargo, no está documentado ni automatizado en el archivo JSON.

## 2. Parámetros Inconsistentes
* **Productos**: El seeder inicial modela `ProductoDigital` y `ProductoFisico`. La prueba asume siempre un `type: "Fisico"` al mandar el payload JSON, pero no se prueba la variante abstracta `ProductoDigital` para validar que el polimorfismo inserte el objeto correcto.
* **Errores Negativos**: La colección carece de aserciones para "Unhappy Paths", tales como:
  * Evidenciar que un stock negativo regresa el estado HTTP `400` y transiciona la orden a `OUT_OF_STOCK`.
  * Intentar autenticarse con contraseñas incorrectas (`HTTP 401 Unauthorized`).

## 3. Rutas Desactualizadas
* Las dependencias DTO recién introducidas han cambiado algunas estructuras de respuesta anidando sub-objetos como `paymentTransactionDTO`, pero los tests de Postman actuales se limitan a checar un `HTTP 200` y extraer sólo el `.id` de la respuesta, omitiendo la validación de un "JSON Schema" riguroso.

---

# TAREA 15 — AUDITORÍA DE CÓDIGO

## Riesgos de Seguridad 🛡️
1. **Secretos Hardcodeados**: Es probable que la clave secreta para la firma del JWT (`jwt.secret`), así como las credenciales de la base de datos MySQL (usuario `root`, por ejemplo), residan en plano dentro de `application.properties`. Se sugiere migrar al uso de variables de entorno (`${DB_PASSWORD}`) o Spring Cloud Config / HashiCorp Vault.
2. **Exposición de Trazas de Excepción**: En ocasiones, el `InvalidDefinitionException` o la ausencia de controladores de exepción `@ControllerAdvice` globales podría filtrar *Stacktraces* de la lógica interna de Java o consultas Hibernate al cliente (frontend).
3. **CORS y Orígenes Confiables**: La política CORS, si está configurada como `*` (permitir todos), debe ser restringida a los dominios autorizados de la interfaz frontend por motivos de protección contra CSRF o *Cross-Site Scripting*.

## Posibles Bugs (Bugs Potenciales) 🐛
1. **Lógica de Concurrencia de Inventario**: Al usar `sumar/restar` stock interactuando bajo un volumen enorme y concurrente, el valor escalar de la base de datos en `ProductoFisico` podría sufrir de la condición de carrera (*Race Condition*), a menos que se asegure el uso estricto de bloqueos optimistas JPA (`@Version`) o bloqueos pesimistas (`@Lock(LockModeType.PESSIMISTIC_WRITE)`).
2. **Borrado de Elementos Relacionados**: Las relaciones `@ManyToMany` u `@OneToMany` sin el atributo `orphanRemoval` apropiado pueden derivar en retención de basura (filas viudas en SQL) al eliminar perfiles o carritos.

## Mejoras de Rendimiento ⚡
1. **Overhead de Serialización Hibernate**: La resolución principal usando DTOs corrigió un bucle de consultas (El problema N+1 y serialización ByBuddy proxy). Se recomienda continuar usando inyección de dependencias como *MapStruct* para transcodificar DTO-a-Entity automáticamente en tiempo de compilación.
2. **Caché para Catálogos Estáticos**: Las consultas a `GET /api/productos` acceden a la base de datos MySQL de forma reiterativa. Para un modelo de alta demanda, se sugiere agregar la caché de segundo nivel de Hibernate y memoria distribuida mediante integración con `@EnableCaching` (Redis/Hazelcast).

## Mejoras de Arquitectura y Deuda Técnica 📐
1. **Arquitectura Hexagonal (Puertos y Adaptadores)**: Si las reglas de negocio, los DTOs y el uso estricto de la máquina de estados continúan creciendo, migrar de un diseño clásico "Controller->Service->Repository" hacia un *Domain-Driven Design (DDD)* completo para desacoplar y prevenir fugas de la dependencia `@Entity` hacia APIs externas.
2. **Carga en Memoria vs Paginación**: En los endpoints de listado (`listarUsuarios`, `listarTodasDTO`), el arreglo puede crecer enormemente, comprometiendo la RAM de la JVM. Como deuda técnica crítica, todos los controladores GET deben admitir `Pageable` para transitar los datos por lotes (Chunks) al cliente web.

_Estas recomendaciones se limitan estrictamente al ámbito de análisis, de acuerdo al mandato de no alterar la funcionalidad ni operación actual del sistema._
