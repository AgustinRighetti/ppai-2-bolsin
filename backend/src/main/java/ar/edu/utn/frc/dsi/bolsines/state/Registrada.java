package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;
import java.util.List;

/** <<ConcreteState>> Documentación registrada, disponible para incluir en un remito. */
public class Registrada extends EstadoDocumentacion {

    public static final String NOMBRE = "Registrada";

    public Registrada() {
    }

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "Registrada";
    }

    /** CU 15 Generar remito: Registrada -> En remito. */
    @Override
    public void asignarARemito(Documentacion documentacion, List<CambioEstadoDocumentacion> cambiosEstado,
                               Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = buscarCambioEstadoActual(cambiosEstado);
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = crearEstadoEnRemito();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(nuevoEstado);
    }

    /** CU 9 Eliminar documentación: Registrada -> De baja. */
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

    private EstadoDocumentacion crearEstadoEnRemito() {
        return new EnRemito();
    }

    private EstadoDocumentacion crearEstadoDeBaja() {
        return new DeBaja();
    }
}
