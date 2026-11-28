package ar.edu.utn.frc.dsi.bolsines.entity;

import ar.edu.utn.frc.dsi.bolsines.state.EstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.state.EstadoDocumentacionConverter;
import ar.edu.utn.frc.dsi.bolsines.state.Registrada;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Patrón State (Gamma) - rol <<Context>>.
 *
 * Mantiene una referencia a su estado actual (EstadoDocumentacion) y le delega cada evento de la
 * máquina de estados. No contiene condicionales sobre el estado: es el estado concreto el que
 * sabe si la transición está permitida y el que hace el cambio de estado.
 */
@Entity
public class Documentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Integer numero;
    private String asunto;
    private LocalDate fechaPase;
    @ManyToOne(optional = false)
    private TipoDocumento tipoDocumento;
    @ManyToOne(optional = false)
    private ComisionMedica comisionMedicaOrigen;
    // DEFENSA (4. State - CONTEXT): referencia al estado actual. Es un objeto de una de las 9 clases
    // concretas; el converter lo guarda en la base por su nombre.
    @Convert(converter = EstadoDocumentacionConverter.class)
    private EstadoDocumentacion estadoActual;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "documentacion_id")
    @OrderBy("fechaHoraInicio ASC")
    private List<CambioEstadoDocumentacion> cambiosEstado = new ArrayList<>();

    protected Documentacion() {
    }

    /** CU 7 Registrar documentación: toda documentación nace en estado Registrada. */
    public Documentacion(Integer numero, String asunto, LocalDate fechaPase, TipoDocumento tipoDocumento,
                         ComisionMedica comisionMedicaOrigen, Empleado responsable, LocalDateTime fechaHora) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumento = tipoDocumento;
        this.comisionMedicaOrigen = comisionMedicaOrigen;
        this.estadoActual = new Registrada();
        this.cambiosEstado.add(new CambioEstadoDocumentacion(fechaHora, estadoActual, responsable));
    }

    // ---- Eventos: el contexto delega en su estado actual ----

    public void asignarARemito(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.asignarARemito(this, cambiosEstado, responsable, fechaHora);
    }

    public void cancelarRemito(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.cancelarRemito(this, cambiosEstado, responsable, fechaHora);
    }

    public void agregarABolsin(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.agregarABolsin(this, cambiosEstado, responsable, fechaHora);
    }

    public void quitarDeBolsin(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.quitarDeBolsin(this, cambiosEstado, responsable, fechaHora);
    }

    public void registrarRetiro(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.registrarRetiro(this, cambiosEstado, responsable, fechaHora);
    }

    // DEFENSA (4. State - CONTEXT): una sola línea. No pregunta en qué estado está: le DELEGA el
    // evento a su estado actual y le pasa a sí misma (this) y su lista de cambios de estado, para que el
    // estado concreto busque el cambio actual y le cambie el estado.
    public void aceptar(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.aceptar(this, cambiosEstado, responsable, fechaHora);
    }

    public void noRecibir(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.noRecibir(this, cambiosEstado, responsable, fechaHora);
    }

    public void rechazar(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.rechazar(this, cambiosEstado, responsable, fechaHora);
    }

    public void redirigir(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.redirigir(this, cambiosEstado, responsable, fechaHora);
    }

    public void retomarRegistro(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.retomarRegistro(this, cambiosEstado, responsable, fechaHora);
    }

    public void darDeBaja(Empleado responsable, LocalDateTime fechaHora) {
        estadoActual.darDeBaja(this, cambiosEstado, responsable, fechaHora);
    }

    // ---- Usados por los estados concretos al hacer el cambio de estado ----

    public void agregarCambioEstado(CambioEstadoDocumentacion cambioEstado) {
        cambiosEstado.add(cambioEstado);
    }

    public void setEstado(EstadoDocumentacion estado) {
        this.estadoActual = estado;
    }

    // ---- Datos ----

    public boolean esTuNumero(Integer numero) {
        return this.numero.equals(numero);
    }

    public String getNombreTipoDocumento() {
        return tipoDocumento.getNombre();
    }

    public EstadoDocumentacion getEstadoActual() {
        return estadoActual;
    }

    public String getDescripcionEstadoActual() {
        return estadoActual.getDescripcion();
    }

    public Integer getNumero() {
        return numero;
    }

    public String getAsunto() {
        return asunto;
    }

    public LocalDate getFechaPase() {
        return fechaPase;
    }

    public ComisionMedica getComisionMedicaOrigen() {
        return comisionMedicaOrigen;
    }

    public List<CambioEstadoDocumentacion> getCambiosEstado() {
        return cambiosEstado;
    }
}
