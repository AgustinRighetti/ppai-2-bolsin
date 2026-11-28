package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;
import java.util.List;

/** <<ConcreteState>> Recibida en la CMC, pendiente de entrega interna a otra área. */
public class ParaRedirigir extends EstadoDocumentacion {

    public static final String NOMBRE = "ParaRedirigir";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "Para redirigir";
    }

    /** CU 31 [es correcta]: Para redirigir -> Recibida y aceptada. */
    @Override
    public void aceptar(Documentacion documentacion, List<CambioEstadoDocumentacion> cambiosEstado,
                        Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = buscarCambioEstadoActual(cambiosEstado);
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = crearEstadoRecibidaYAceptada();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(nuevoEstado);
    }

    /** CU 31 [no es correcta]: Para redirigir -> Recibida y rechazada. */
    @Override
    public void rechazar(Documentacion documentacion, List<CambioEstadoDocumentacion> cambiosEstado,
                         Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = buscarCambioEstadoActual(cambiosEstado);
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = crearEstadoRecibidaYRechazada();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(nuevoEstado);
    }

    private EstadoDocumentacion crearEstadoRecibidaYAceptada() {
        return new RecibidaYAceptada();
    }

    private EstadoDocumentacion crearEstadoRecibidaYRechazada() {
        return new RecibidaYRechazada();
    }
}
