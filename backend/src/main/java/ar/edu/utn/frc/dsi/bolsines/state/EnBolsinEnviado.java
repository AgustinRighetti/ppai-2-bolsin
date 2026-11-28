package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

/** <<ConcreteState>> Viaja en un bolsín retirado por el correo. Es el estado desde el que parten las transiciones del CU 28. */
// DEFENSA (4. State - CONCRETE STATE): en el CU 28 la documentación está en este estado. Redefine
// sus 4 transiciones y en cada una ES ÉL quien cambia el estado: cierra el cambio de estado actual,
// crea el estado siguiente y el nuevo cambio de estado, y se los asigna a la documentación.
public class EnBolsinEnviado extends EstadoDocumentacion {

    public static final String NOMBRE = "EnBolsinEnviado";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "En bolsín enviado";
    }

    /** CU 28 [contenido igual]: En bolsín enviado -> Recibida y aceptada. */
    @Override
    public void aceptar(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = documentacion.obtenerCambioEstadoActual();
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = new RecibidaYAceptada();
        CambioEstadoDocumentacion nuevoCambioEstado = new CambioEstadoDocumentacion(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstadoActual(nuevoEstado);
    }

    /** CU 28 [no se recibe]: En bolsín enviado -> No recibida. */
    @Override
    public void noRecibir(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = documentacion.obtenerCambioEstadoActual();
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = new NoRecibida();
        CambioEstadoDocumentacion nuevoCambioEstado = new CambioEstadoDocumentacion(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstadoActual(nuevoEstado);
    }

    /** CU 28 [no corresponde al destino]: En bolsín enviado -> Recibida y rechazada. */
    @Override
    public void rechazar(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = documentacion.obtenerCambioEstadoActual();
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = new RecibidaYRechazada();
        CambioEstadoDocumentacion nuevoCambioEstado = new CambioEstadoDocumentacion(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstadoActual(nuevoEstado);
    }

    /** CU 28 [redirigir a otra área]: En bolsín enviado -> Para redirigir. */
    @Override
    public void redirigir(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = documentacion.obtenerCambioEstadoActual();
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = new ParaRedirigir();
        CambioEstadoDocumentacion nuevoCambioEstado = new CambioEstadoDocumentacion(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstadoActual(nuevoEstado);
    }
}
