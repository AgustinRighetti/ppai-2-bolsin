package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;
import java.util.List;

/** <<ConcreteState>> La documentación llegó pero no corresponde a la CM destino; vuelve al origen. */
public class RecibidaYRechazada extends EstadoDocumentacion {

    public static final String NOMBRE = "RecibidaYRechazada";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "Recibida y rechazada";
    }

    @Override
    public boolean esRecibidaYRechazada() {
        return true;
    }

    /** CU 31: Recibida y rechazada -> Registrada (se reenvía a la CM correcta). */
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

    private EstadoDocumentacion crearEstadoRegistrada() {
        return new Registrada();
    }
}
