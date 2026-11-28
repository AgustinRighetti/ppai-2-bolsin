package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;
import java.util.List;

/** <<ConcreteState>> El remito que la contiene está en un bolsín que todavía no retiró el correo. */
public class EnBolsinSaliente extends EstadoDocumentacion {

    public static final String NOMBRE = "EnBolsinSaliente";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "En bolsín saliente";
    }

    /** CU 20 Modificar / CU 21 Eliminar bolsín: En bolsín saliente -> En remito. */
    @Override
    public void quitarDeBolsin(Documentacion documentacion, List<CambioEstadoDocumentacion> cambiosEstado,
                               Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = buscarCambioEstadoActual(cambiosEstado);
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = crearEstadoEnRemito();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(nuevoEstado);
    }

    /** CU 27 Registrar retiro: En bolsín saliente -> En bolsín enviado. */
    @Override
    public void registrarRetiro(Documentacion documentacion, List<CambioEstadoDocumentacion> cambiosEstado,
                                Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = buscarCambioEstadoActual(cambiosEstado);
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = crearEstadoEnBolsinEnviado();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(nuevoEstado);
    }

    private EstadoDocumentacion crearEstadoEnRemito() {
        return new EnRemito();
    }

    private EstadoDocumentacion crearEstadoEnBolsinEnviado() {
        return new EnBolsinEnviado();
    }
}
