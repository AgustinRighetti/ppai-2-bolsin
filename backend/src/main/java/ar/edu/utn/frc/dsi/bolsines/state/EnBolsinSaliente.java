package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

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
    public void quitarDeBolsin(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = documentacion.obtenerCambioEstadoActual();
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = new EnRemito();
        CambioEstadoDocumentacion nuevoCambioEstado = new CambioEstadoDocumentacion(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstadoActual(nuevoEstado);
    }

    /** CU 27 Registrar retiro: En bolsín saliente -> En bolsín enviado. */
    @Override
    public void registrarRetiro(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion cambioEstadoActual = documentacion.obtenerCambioEstadoActual();
        cambioEstadoActual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion nuevoEstado = new EnBolsinEnviado();
        CambioEstadoDocumentacion nuevoCambioEstado = new CambioEstadoDocumentacion(fechaHora, nuevoEstado, responsable);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstadoActual(nuevoEstado);
    }
}
