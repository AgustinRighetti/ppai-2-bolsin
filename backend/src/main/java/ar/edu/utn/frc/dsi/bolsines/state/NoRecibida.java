package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

/** <<ConcreteState>> La documentación figuraba en el remito pero no llegó en el bolsín. */
public class NoRecibida extends EstadoDocumentacion {

    public static final String NOMBRE = "NoRecibida";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "No recibida";
    }

    @Override
    public boolean esNoRecibida() {
        return true;
    }

    /** No recibida -> Registrada (puede vincularse a un nuevo remito). */
    @Override
    public void retomarRegistro(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new Registrada(), responsable, fechaHora);
    }

    /** No recibida -> De baja (ya no es relevante su envío). */
    @Override
    public void darDeBaja(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new DeBaja(), responsable, fechaHora);
    }
}
