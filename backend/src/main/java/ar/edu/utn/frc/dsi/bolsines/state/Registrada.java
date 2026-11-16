package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

/** <<ConcreteState>> Documentación registrada, disponible para incluir en un remito. */
public class Registrada extends EstadoDocumentacion {

    public static final String NOMBRE = "Registrada";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "Registrada";
    }

    @Override
    public boolean esRegistrada() {
        return true;
    }

    /** Registrada -> En remito. */
    @Override
    public void asignarARemito(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new EnRemito(), responsable, fechaHora);
    }

    /** Registrada -> De baja. */
    @Override
    public void darDeBaja(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new DeBaja(), responsable, fechaHora);
    }
}
