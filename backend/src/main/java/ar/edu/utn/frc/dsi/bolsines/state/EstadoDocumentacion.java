package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.CambioEstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;

import java.time.LocalDateTime;

/**
 * Patrón State (Gamma) - rol <<State>>.
 *
 * Representa un estado de la máquina de estados de Documentación. Declara un método por cada
 * evento de la máquina; por defecto el evento no está permitido y lanza una excepción. Cada estado
 * concreto redefine solo las transiciones que salen de él, crea el estado siguiente y deja
 * registrado el cambio de estado (con su fecha y el empleado responsable).
 *
 * El contexto (Documentacion) no pregunta en qué estado está: delega el evento en su estado actual.
 */
public abstract class EstadoDocumentacion {

    public abstract String getNombre();

    public abstract String getDescripcion();

    // ---- Eventos de la máquina de estados (transiciones) ----

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

    // ---- Consultas: cada estado responde por sí mismo ----

    public boolean esRegistrada() {
        return false;
    }

    public boolean esEnBolsinEnviado() {
        return false;
    }

    public boolean esRecibidaYAceptada() {
        return false;
    }

    public boolean esNoRecibida() {
        return false;
    }

    public boolean esRecibidaYRechazada() {
        return false;
    }

    public boolean esParaRedirigir() {
        return false;
    }

    /**
     * Pasos comunes a toda transición: cierra el cambio de estado vigente, crea el nuevo cambio de
     * estado con el empleado responsable y deja al estado siguiente como estado actual del contexto.
     */
    protected void cambiarEstado(Documentacion documentacion, EstadoDocumentacion siguiente,
                                 Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion actual = documentacion.obtenerCambioEstadoActual();
        if (actual != null) {
            actual.setFechaHoraFin(fechaHora);
        }
        documentacion.agregarCambioEstado(new CambioEstadoDocumentacion(fechaHora, siguiente, responsable));
        documentacion.setEstadoActual(siguiente);
    }

    private IllegalStateException transicionInvalida(String accion) {
        return new IllegalStateException(
                "No se puede " + accion + " una documentación en estado " + getDescripcion() + ".");
    }

    /** Reconstruye el objeto estado a partir del nombre guardado en la base de datos. */
    public static EstadoDocumentacion desdeNombre(String nombre) {
        return switch (nombre) {
            case Registrada.NOMBRE -> new Registrada();
            case EnRemito.NOMBRE -> new EnRemito();
            case EnBolsinSaliente.NOMBRE -> new EnBolsinSaliente();
            case EnBolsinEnviado.NOMBRE -> new EnBolsinEnviado();
            case RecibidaYAceptada.NOMBRE -> new RecibidaYAceptada();
            case NoRecibida.NOMBRE -> new NoRecibida();
            case RecibidaYRechazada.NOMBRE -> new RecibidaYRechazada();
            case ParaRedirigir.NOMBRE -> new ParaRedirigir();
            case DeBaja.NOMBRE -> new DeBaja();
            default -> throw new IllegalArgumentException("Estado de documentación desconocido: " + nombre);
        };
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
