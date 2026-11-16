package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

/** <<ConcreteState>> Recibida en la CMC, pendiente de entrega interna a otra área. */
public class ParaRedirigir extends EstadoDocumentacion {

    public static final String NOMBRE = "ParaRedirigir";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "Para redirigir";
    }

    @Override
    public boolean esParaRedirigir() {
        return true;
    }

    /** CU 31: el área la recibe y es correcta. */
    @Override
    public void aceptar(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new RecibidaYAceptada(), responsable, fechaHora);
    }

    /** CU 31: el área la recibe y no le corresponde. */
    @Override
    public void rechazar(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new RecibidaYRechazada(), responsable, fechaHora);
    }
}
