package ar.edu.utn.frc.dsi.bolsines.state;

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

    /** En remito -> Registrada. */
    @Override
    public void cancelarRemito(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new Registrada(), responsable, fechaHora);
    }

    /** En remito -> En bolsín saliente. */
    @Override
    public void agregarABolsin(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new EnBolsinSaliente(), responsable, fechaHora);
    }
}
