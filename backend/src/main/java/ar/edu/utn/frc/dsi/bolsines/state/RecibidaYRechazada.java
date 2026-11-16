package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

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

    /** Recibida y rechazada -> Registrada (se reenvía a la CM correcta). */
    @Override
    public void retomarRegistro(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new Registrada(), responsable, fechaHora);
    }
}
