package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

/**
 * Patrón State (Gamma) - rol <<State>>.
 *
 * Declara la interfaz común de los estados de Documentación: un método por cada evento de la máquina
 * de estados. Por defecto el evento no está permitido en el estado y se rechaza. Cada estado concreto
 * redefine solo las transiciones que salen de él y es él quien hace el cambio de estado.
 */
// DEFENSA (4. State - STATE): clase abstracta. Declara un método por cada evento de la máquina; por
// defecto lanza excepción porque esa transición no está permitida. NO cambia el estado: eso lo hacen
// los estados concretos, cada uno con su propio comportamiento.
public abstract class EstadoDocumentacion {

    public abstract String getNombre();

    public abstract String getDescripcion();

    // ---- Eventos de la máquina de estados (por defecto, transición no permitida) ----

    /** CU 15 Generar remito. */
    public void asignarARemito(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("incluir en un remito");
    }

    /** CU 17 Cancelar remito. */
    public void cancelarRemito(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("quitar de un remito cancelado");
    }

    /** CU 19 Generar bolsín. */
    public void agregarABolsin(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("agregar a un bolsín");
    }

    /** CU 20 Modificar bolsín / CU 21 Eliminar bolsín. */
    public void quitarDeBolsin(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("quitar de un bolsín");
    }

    /** CU 27 Registrar el retiro de bolsines. */
    public void registrarRetiro(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("registrar como retirada por el correo");
    }

    /** CU 28 Registrar recepción de bolsín (contenido igual) / CU 31 (área acepta). */
    public void aceptar(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("aceptar");
    }

    /** CU 28 Registrar recepción de bolsín (no se recibe toda la documentación). */
    public void noRecibir(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("registrar como no recibida");
    }

    /** CU 28 Registrar recepción de bolsín (no corresponde al destino) / CU 31 (área rechaza). */
    public void rechazar(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("rechazar");
    }

    /** CU 28 Registrar recepción de bolsín (redirigir a otra área de la CMC). */
    public void redirigir(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("redirigir");
    }

    /** CU 29 / CU 31: la documentación vuelve a quedar registrada para un nuevo remito. */
    public void retomarRegistro(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("volver a registrar");
    }

    /** CU 9 Eliminar documentación / CU 31 Registrar revisión de documentación. */
    public void darDeBaja(Documentacion documentacion, Empleado responsable, LocalDateTime fechaHora) {
        throw transicionInvalida("dar de baja");
    }

    // ---- Consultas: el estado concreto que corresponde responde verdadero ----

    public boolean esNoRecibida() {
        return false;
    }

    public boolean esRecibidaYRechazada() {
        return false;
    }

    private IllegalStateException transicionInvalida(String accion) {
        return new IllegalStateException(
                "No se puede " + accion + " una documentación en estado " + getDescripcion() + ".");
    }
}
