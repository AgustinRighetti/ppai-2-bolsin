package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

/** <<ConcreteState>> Estado final: la documentación llegó a destino y fue aceptada. */
public class RecibidaYAceptada extends EstadoDocumentacion {

    public static final String NOMBRE = "RecibidaYAceptada";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "Recibida y aceptada";
    }

    @Override
    public boolean esRecibidaYAceptada() {
        return true;
    }
}
