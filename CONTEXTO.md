# Contexto del proyecto (para continuar con otra IA)

Pegá este archivo completo al empezar la conversación.

## Qué es

Trabajo práctico PPAI 2026 de **Diseño de Sistemas de Información** (UTN FRC), caso **Bolsines**.
Grupo 14, curso 3K2, tutora Ing. Valeria Abdala. Es la **Entrega 2 (Diseño)**, se entrega el 17/11/2026 y se defiende en forma oral con el programa funcionando.

- Caso de uso asignado por la consigna (grupo par): **CU 28 Registrar recepción de bolsín**, con la **máquina de estados de Documentación**.
- Patrón de Gamma: **State**. Primero se había hecho con Strategy, pero los profesores lo rechazaron.
- La consigna pide: identificación y justificación del patrón, diagrama de clases de diseño (con tipos y visibilidad), diagrama de secuencia de la parte rediseñada, implementación funcionando (flujo principal + al menos 2 alternativos) con persistencia en base de datos, y diseño de interfaces con patrones de GUI.
- Los afiches de UX (moodboard, mapa de empatía, user journey) **no** se presentan en esta entrega.

## Estado actual

Todo está hecho y probado. La carpeta es `~/Utn/DSI/PPAI-2-BOLSIN`:

| Carpeta | Contenido |
|---|---|
| `backend/` | Java 21, Spring Boot 3.3.5, Spring Data JPA, H2 en archivo (`backend/data/`) |
| `frontend/` | React 18 + Vite, un solo componente `src/App.jsx`, llama a `/api` (proxy a `:8080`) |
| `docs/` | `PPAI2026_3K2_G14_E2_Diseño.html` (fuente) y `.pdf` (exportado), capturas en `docs/img/` |
| `README.md` | Cómo ejecutar y guion de la defensa |

Paquete base del backend: `ar.edu.utn.frc.dsi.bolsines`

- `boundary/PantallaRegistrarRecepcion` — controller REST en `/api/recepcion-bolsin` (`/iniciar`, `/filtrar`, `/seleccionar-bolsin`, `/seleccionar-opcion`, `/confirmar`, `/cancelar`). También `InterfazEmail` (mail simulado, CU 29) y `ManejadorErrores`.
- `control/GestorRecepcionBolsin` — clase de control. Guarda el estado del caso de uso entre llamadas (CM logueada, bolsín seleccionado, `opcionSeleccionada`). Las 4 opciones del paso 7 son constantes `int` (`OPCION_CONTENIDO_IGUAL`, `OPCION_NO_SE_RECIBE_TODA`, `OPCION_NO_CORRESPONDE_AL_DESTINO`, `OPCION_REDIRIGIR_A_OTRA_AREA`; la 4 solo si la CM destino es la Central). `tomarConfirmacion` sigue los mismos pasos que la secuencia de análisis (Entrega 1): `buscarEstado()` (un loop sobre los `Estado`) → `actualizarEstadoBolsin()` (`bolsin.actualizarEstado(...)`, sin State) → `actualizarEstadoRemito()` (cada remito pasa a Recibido y Aceptado, o Parcial si contiene documentación marcada con las opciones 2 o 3) → `actualizarEstadoDocumentacion()` (acá empieza el State: a cada documentación le manda `aceptar`, `noRecibir`, `rechazar` o `redirigir`) → `enviarNotificacion()` (`interfazEmail.enviarCorreo`) → `finCU()`. La pantalla luego hace `informarRegistroExitoso(resultado)`.
- **Patrón State** (paquete `state/`):
  - **Context**: `entity/Documentacion`, con el atributo `estadoActual: EstadoDocumentacion`. Sus métodos de evento (`asignarARemito`, `agregarABolsin`, `registrarRetiro`, `aceptar`, `noRecibir`, `rechazar`, `redirigir`, `retomarRegistro`, `darDeBaja`, `cancelarRemito`, `quitarDeBolsin`) delegan en el estado: `estadoActual.aceptar(this, cambiosEstado, responsable, fechaHora)`.
  - **State**: clase abstracta `state/EstadoDocumentacion`. Por defecto cada evento lanza `IllegalStateException` (transición inválida → HTTP 409). La clase abstracta NO cambia el estado.
  - **ConcreteStates**: `Registrada`, `EnRemito`, `EnBolsinSaliente`, `EnBolsinEnviado`, `RecibidaYAceptada`, `NoRecibida`, `RecibidaYRechazada`, `ParaRedirigir`, `DeBaja`. Cada uno sobrescribe solo las transiciones válidas y es el que hace el cambio de estado: recibe la documentación y su lista `cambiosEstado`, llama a `buscarCambioEstadoActual(cambiosEstado)` (método protegido de la abstracta, recorre con `sosActual()`) y le asigna fecha/hora fin, crea el estado siguiente con su método privado `crearEstadoX()` (`new RecibidaYAceptada()`), crea el nuevo cambio con `crearCambioEstado(...)` (protegido de la abstracta) y llama a `documentacion.agregarCambioEstado(...)` y `documentacion.setEstado(...)`.
  - Persistencia: `state/EstadoDocumentacionConverter` (JPA `AttributeConverter`) guarda el nombre del estado y al leer crea el estado concreto con un `switch` sobre el nombre. Bolsín y Remito siguen usando la entidad `Estado` (ámbito + nombre).
  - Tests: `backend/src/test/.../state/MaquinaEstadosDocumentacionTest` (`mvn test`, 6 tests).
- `entity/` — `Bolsin`, `Remito`, `DetalleRemito`, `Documentacion`, `CambioEstadoBolsin`, `CambioEstadoDocumentacion`, `Estado`, `ComisionMedica`, `Empleado`, `Usuario`, `Sesion`, `TipoDocumento`. Aplican Experto (por ejemplo `bolsin.sosEnviado()`, `remito.contieneAlgunaDocumentacion(...)`, `detalleRemito.esDeAlgunaDocumentacion(...)`).
- `repository/` — interfaces `JpaRepository`.
- `dto/Dtos` — records de request/response; nunca se devuelve una entidad a la pantalla.
- `soporte/` — datos de prueba (`DatosIniciales`) y endpoints de demo en `/api/demo` (cambiar usuario, reiniciar datos). No son parte del caso de uso.

Se siguió la guía de la cátedra "Administración de Campos": controller = boundary, service = gestor, los mensajes gestor→pantalla viajan como retorno de los métodos.

## Reglas de negocio implementadas (por opción)

| Opción | Remitos | Documentación |
|---|---|---|
| 1. Contenido igual al registrado | Recibido y Aceptado | Recibida y Aceptada |
| 2. No se recibe toda | Parcial si tiene faltantes | Faltante: No Recibida; resto aceptada |
| 3. No corresponde al destino | Parcial si tiene rechazos (**supuesto nuestro**, la ERS no lo dice) | Indicada: Recibida y Rechazada; resto aceptada |
| 4. Redirigir a otra área (solo si la CM destino es la Central) | Recibido y Aceptado | Indicada: Para Redirigir + área; resto aceptada |

Flujos alternativos cubiertos: A1 y A2 (filtro sin resultados), A3, A4, A5, A6 (no confirma) y cancelar.

## Cómo ejecutar

```bash
cd backend && mvn spring-boot:run     # http://localhost:8080
cd frontend && npm install && npm run dev   # http://localhost:5173
```

- Si la base quedó con el esquema viejo (versión Strategy), borrar `backend/data/`; se recrea sola.
- Si `mvn spring-boot:run` falla con "Process terminated with exit code 1", el puerto 8080 está ocupado: `lsof -ti tcp:8080 | xargs kill`.
- Usuarios de demo (selector "Usuario logueado", abajo en el menú lateral): `lgomez` (CM Córdoba, bolsines 1, 2, 3, 7 y 8), `mperez` (Central, bolsines 4, 5, 9 y 10), `rdiaz` (Mendoza, bolsines 11 y 12), `dsosa` (Rosario, sin bolsines).
- Consola de la base: http://localhost:8080/h2-console, URL `jdbc:h2:file:./data/bolsines`, usuario `sa`, sin contraseña.

## Cómo regenerar el PDF

Editar `docs/PPAI2026_3K2_G14_E2_Diseño.html` y, desde `docs/`:

```bash
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --headless=new --no-pdf-header-footer --virtual-time-budget=20000 --print-to-pdf="$PWD/PPAI2026_3K2_G14_E2_Diseño.pdf" "file://$PWD/PPAI2026_3K2_G14_E2_Dise%C3%B1o.html"
```

Los diagramas son Mermaid (necesita internet): máquina de estados (Fig. 1), clases del State (Fig. 2a), clases boundary/control/entidades (Fig. 2b) y secuencia (Fig. 3). El script del final del HTML los convierte en imágenes para que cada uno salga en su hoja apaisada.

## Pendientes y cosas a tener en cuenta

- Confirmar con la tutora el supuesto de la opción 3 (estado del remito).
- Los diagramas están en Mermaid, no en la herramienta de modelado de la cátedra; si los piden ahí, hay que redibujarlos a partir del HTML.
- No hay login real (CU 5) y el mail (CU 29) está simulado.
- La carpeta tiene su propio repositorio git (repo privado en el GitHub de AgustinRighetti); no commitear en el repo del home (`~`).
- La entrega 1 (análisis) fue mal y no tenemos la devolución escrita de la tutora.
