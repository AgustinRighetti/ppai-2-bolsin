package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;
import java.util.List;

/** <<ConcreteState>> Documentación incluida en un remito en estado creado. */
public class EnRemito extends EstadoDocumentacion {

    public static final String NOMBRE = "EnRemito";

    public EnRemito() {
    }

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "En remito";
    }

    /** CU 17 Cancelar remito: En remito -> Registrada. */
    @Override
    public void cancelarRemito(Documentacion documentacion, List<CambioEstadoDocumentacion> cambiosEstado,
                               Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = buscarCambioEstadoActual(cambiosEstado);
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = crearEstadoRegistrada();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(nuevoEstado);
    }

    /** CU 19 Generar bolsín: En remito -> En bolsín saliente. */
    @Override
    public void agregarABolsin(Documentacion documentacion, List<CambioEstadoDocumentacion> cambiosEstado,
                               Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = buscarCambioEstadoActual(cambiosEstado);
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = crearEstadoEnBolsinSaliente();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(nuevoEstado);
    }

    private EstadoDocumentacion crearEstadoRegistrada() {
        return new Registrada();
    }

    private EstadoDocumentacion crearEstadoEnBolsinSaliente() {
        return new EnBolsinSaliente();
    }
}
