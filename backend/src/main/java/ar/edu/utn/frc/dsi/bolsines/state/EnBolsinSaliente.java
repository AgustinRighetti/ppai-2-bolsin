package ar.edu.utn.frc.dsi.bolsines.state;

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

    /** En bolsín saliente -> En remito. */
    @Override
    public void quitarDeBolsin(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new EnRemito(), responsable, fechaHora);
    }

    /** En bolsín saliente -> En bolsín enviado. */
    @Override
    public void registrarRetiro(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new EnBolsinEnviado(), responsable, fechaHora);
    }
}
