package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

/** <<ConcreteState>> Viaja en un bolsín retirado por el correo. Es el estado desde el que parten las transiciones del CU 28. */
// DEFENSA (4. State - CONCRETE STATE): hay 9 estados concretos, uno por estado de la máquina. En el
// CU 28 la documentación está en este. Redefine solo sus 4 transiciones y en cada una CREA el estado
// siguiente: aceptar -> RecibidaYAceptada, noRecibir -> NoRecibida, rechazar -> RecibidaYRechazada,
// redirigir -> ParaRedirigir.
public class EnBolsinEnviado extends EstadoDocumentacion {

    public static final String NOMBRE = "EnBolsinEnviado";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "En bolsín enviado";
    }

    @Override
    public boolean esEnBolsinEnviado() {
        return true;
    }

    /** CU 28: el contenido llegó como estaba registrado. */
    @Override
    public void aceptar(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new RecibidaYAceptada(), responsable, fechaHora);
    }

    /** CU 28: la documentación no vino en el bolsín. */
    @Override
    public void noRecibir(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new NoRecibida(), responsable, fechaHora);
    }

    /** CU 28: la documentación no corresponde a la CM destino. */
    @Override
    public void rechazar(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new RecibidaYRechazada(), responsable, fechaHora);
    }

    /** CU 28: la CMC debe enviarla a un área interna. */
    @Override
    public void redirigir(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        cambiarEstado(documentacion, new ParaRedirigir(), responsable, fechaHora);
    }
}
