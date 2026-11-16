# PPAI 2026 · Bolsines · CU 28 Registrar recepción de bolsín

Segunda entrega (Diseño) del Grupo 14 · 3K2. Rediseño con el patrón **State** (máquina de estados de Documentación) e implementación del caso de uso.

| | |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.3 (API REST) |
| Tecnología | Web: backend Spring Boot + frontend React 18 (Vite) |
| Base de datos | H2 en archivo (`backend/data/bolsines.mv.db`) |
| Persistencia | Spring Data JPA / Hibernate |

## Cómo ejecutar

Requisitos: JDK 21, Maven y Node.js.

```bash
# Terminal 1: backend (http://localhost:8080)
cd backend
mvn spring-boot:run
```

```bash
# Terminal 2: frontend (http://localhost:5173)
cd frontend
npm install
npm run dev
```

Si el backend no arranca porque la base quedó de una versión anterior, borrar `backend/data/` (se vuelve a crear con los datos de prueba).

Tests de la máquina de estados: `cd backend && mvn test`.

Si `java` no se encuentra después de instalarlo con Homebrew:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export PATH=$JAVA_HOME/bin:$PATH
```

## Guion para la defensa

Usuarios de prueba (se cambian desde el selector de arriba a la derecha; simula el inicio de sesión):

| Usuario | Comisión médica | Sirve para mostrar |
|---|---|---|
| `lgomez` | CM Córdoba | Flujo principal y alternativos A1, A2, A3, A4, A6 (bolsines 1, 2 y 3) |
| `mperez` | Comisión Médica Central | A5: redirigir a otra área (bolsines 4 y 5) |
| `rdiaz` | CM Mendoza | Sin bolsines enviados para la CM |

1. **Flujo principal**: Registrar recepción → bolsín 1 → "El contenido del bolsín es igual al registrado" → Confirmar.
2. **A1 / A2**: filtrar por precinto `999999` o por CM de origen `Salta`.
3. **A3**: bolsín 2 → "No se recibe toda la documentación" → marcar una documentación → Confirmar.
4. **A4**: bolsín 3 → "Existe documentación que no corresponde al destino".
5. **A6**: en el diálogo de confirmación elegir "No confirmar".
6. **A5**: cambiar a `mperez` → bolsín 5 → "La documentación se debe redirigir a otra área".
7. **Persistencia**: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/bolsines`, usuario `sa`, sin contraseña).
   Consultas útiles: `SELECT * FROM CAMBIO_ESTADO_BOLSIN`, `SELECT * FROM CAMBIO_ESTADO_DOCUMENTACION`, `SELECT * FROM REMITO`.
8. "Volver a cargar los datos de prueba" (pantalla de inicio) deja la base como al principio.

Los e-mails del CU 29 se simulan: se muestran en la pantalla de resultado y se escriben en el log del backend.

## Dónde está cada cosa

| Elemento del modelo | Código |
|---|---|
| Boundary `PantallaRegistrarRecepcion` | `backend/.../boundary/PantallaRegistrarRecepcion.java` (controller REST) + `frontend/src/App.jsx` |
| Control `GestorRecepcionBolsin` | `backend/.../control/GestorRecepcionBolsin.java` |
| Context del State: `Documentacion` | `backend/.../entity/Documentacion.java` |
| State `EstadoDocumentacion` y los 9 estados concretos | `backend/.../state/` |
| Persistencia del estado (nombre ↔ objeto) | `backend/.../state/EstadoDocumentacionConverter.java` |
| Opciones de recepción del paso 7 | `backend/.../entity/SituacionRecepcion.java` |
| Tests de las transiciones | `backend/src/test/.../state/MaquinaEstadosDocumentacionTest.java` |
| Entidades del dominio | `backend/.../entity/` |
| Persistencia | `backend/.../repository/` |
| Datos de prueba y cambio de usuario (fuera del CU) | `backend/.../soporte/` |

El documento de la entrega está en `docs/PPAI2026_3K2_G14_E2_Diseño.html` y exportado en `docs/PPAI2026_3K2_G14_E2_Diseño.pdf`.
