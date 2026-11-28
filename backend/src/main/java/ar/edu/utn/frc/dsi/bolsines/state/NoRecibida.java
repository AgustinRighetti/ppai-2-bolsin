package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;
import java.util.List;

/** <<ConcreteState>> La documentación figuraba en el remito pero no llegó en el bolsín. */
public class NoRecibida extends EstadoDocumentacion {

    public static final String NOMBRE = "NoRecibida";

    public NoRecibida() {
    }

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "No recibida";
    }

    /** CU 29: No recibida -> Registrada (puede vincularse a un nuevo remito). */
    @Override
    public void retomarRegistro(Documentacion documentacion, List<CambioEstadoDocumentacion> cambiosEstado,
                                Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = buscarCambioEstadoActual(cambiosEstado);
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = crearEstadoRegistrada();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(nuevoEstado);
    }

    /** CU 31: No recibida -> De baja. */
    @Override
    public void darDeBaja(Documentacion documentacion, List<CambioEstadoDocumentacion> cambiosEstado,
                          Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = buscarCambioEstadoActual(cambiosEstado);
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = crearEstadoDeBaja();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(nuevoEstado);
    }

    private EstadoDocumentacion crearEstadoRegistrada() {
        return new Registrada();
    }

    private EstadoDocumentacion crearEstadoDeBaja() {
        return new DeBaja();
    }
}
