package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

/** <<ConcreteState>> Documentación incluida en un remito en estado creado. */
public class EnRemito extends EstadoDocumentacion {

    public static final String NOMBRE = "EnRemito";

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
    public void cancelarRemito(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = documentacion.obtenerCambioEstadoActual();
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = new Registrada();
        CambioEstadoDocumentacion nuevoCambioEstado = new CambioEstadoDocumentacion(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstadoActual(nuevoEstado);
    }

    /** CU 19 Generar bolsín: En remito -> En bolsín saliente. */
    @Override
    public void agregarABolsin(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = documentacion.obtenerCambioEstadoActual();
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = new EnBolsinSaliente();
        CambioEstadoDocumentacion nuevoCambioEstado = new CambioEstadoDocumentacion(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstadoActual(nuevoEstado);
    }
}
