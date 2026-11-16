# Contexto del proyecto (para continuar con otra IA)

Pegá este archivo completo al empezar la conversación.

## Qué es

Trabajo práctico PPAI 2026 de **Diseño de Sistemas de Información** (UTN FRC), caso **Bolsines**.
Grupo 14, curso 3K2, tutora Ing. Valeria Abdala. Es la **Entrega 2 (Diseño)**, se entrega el 17/11/2026 y se defiende en forma oral con el programa funcionando.

- Caso de uso asignado (grupo par): **CU 28 Registrar recepción de bolsín**.
- Patrón de Gamma acordado: **Strategy**.
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
- `control/GestorRecepcionBolsin` — clase de control y **Context** del Strategy. Guarda el estado del caso de uso entre llamadas (CM logueada, bolsín seleccionado, estrategia elegida). El bolsín lo pasa él a "Recibido en CM destino"; remitos y documentación los delega en la estrategia.
- `strategy/EstrategiaRecepcion` (interfaz) y las concretas `RecepcionCompleta`, `RecepcionParcial`, `RecepcionNoCorrespondeDestino`, `RecepcionParaRedirigir` (una por cada opción de recepción del paso 7 del CU).
- `entity/` — `Bolsin`, `Remito`, `DetalleRemito`, `Documentacion`, `CambioEstadoBolsin`, `CambioEstadoDocumentacion`, `Estado`, `ComisionMedica`, `Empleado`, `Usuario`, `Sesion`, `TipoDocumento`. Aplican Experto (por ejemplo `bolsin.sosEnviado()`, `remito.recibir(...)`, `documentacion.actualizarEstado(...)`).
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

- Si `mvn spring-boot:run` falla con "Process terminated with exit code 1", el puerto 8080 está ocupado: `lsof -ti tcp:8080 | xargs kill`.
- Usuarios de demo (selector arriba a la derecha): `lgomez` (CM Córdoba, bolsines 1–3), `mperez` (Central, bolsines 4–5), `rdiaz` (Mendoza, sin bolsines).
- Consola de la base: http://localhost:8080/h2-console, URL `jdbc:h2:file:./data/bolsines`, usuario `sa`, sin contraseña.

## Cómo regenerar el PDF

Editar `docs/PPAI2026_3K2_G14_E2_Diseño.html` y, desde `docs/`:

```bash
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --headless=new --no-pdf-header-footer --virtual-time-budget=20000 --print-to-pdf="$PWD/PPAI2026_3K2_G14_E2_Diseño.pdf" "file://$PWD/PPAI2026_3K2_G14_E2_Dise%C3%B1o.html"
```

Los diagramas son Mermaid (necesita internet) y el script del final del HTML los convierte en imágenes para que cada uno salga en su hoja apaisada.

## Pendientes y cosas a tener en cuenta

- Confirmar con la tutora el supuesto de la opción 3 (estado del remito).
- Los diagramas están en Mermaid, no en la herramienta de modelado de la cátedra; si los piden ahí, hay que redibujarlos a partir del HTML.
- No hay login real (CU 5) y el mail (CU 29) está simulado.
- La carpeta cuelga del repositorio git de la carpeta personal (`~`): no hacer commits ahí sin revisarlo.
- La entrega 1 (análisis) fue mal y no tenemos la devolución escrita de la tutora.
