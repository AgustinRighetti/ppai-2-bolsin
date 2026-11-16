package ar.edu.utn.frc.dsi.bolsines.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "documentacion_id")
    @OrderBy("fechaHoraInicio ASC")
    private List<CambioEstadoDocumentacion> cambiosEstado = new ArrayList<>();

    protected Documentacion() {
    }

    public Documentacion(Integer numero, String asunto, LocalDate fechaPase, TipoDocumento tipoDocumento,
                         ComisionMedica comisionMedicaOrigen) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumento = tipoDocumento;
        this.comisionMedicaOrigen = comisionMedicaOrigen;
    }

    public boolean esTuNumero(Integer numero) {
        return this.numero.equals(numero);
    }

    public String getNombreTipoDocumento() {
        return tipoDocumento.getNombre();
    }

    /**
     * Cierra el cambio de estado vigente y crea uno nuevo con el estado indicado,
     * dejando registrado el empleado responsable (trazabilidad).
     */
    public void actualizarEstado(Estado nuevoEstado, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoDocumentacion actual = obtenerCambioEstadoActual();
        if (actual != null) {
            actual.setFechaHoraFin(fechaHora);
        }
        crearCambioEstado(nuevoEstado, responsable, fechaHora);
    }

    private void crearCambioEstado(Estado estado, Empleado responsable, LocalDateTime fechaHora) {
        cambiosEstado.add(new CambioEstadoDocumentacion(fechaHora, estado, responsable));
    }

    private CambioEstadoDocumentacion obtenerCambioEstadoActual() {
        return cambiosEstado.stream().filter(CambioEstadoDocumentacion::sosActual).findFirst().orElse(null);
    }

    public String getNombreEstadoActual() {
        CambioEstadoDocumentacion actual = obtenerCambioEstadoActual();
        return actual == null ? null : actual.getNombreEstado();
    }

    public String getDescripcionEstadoActual() {
        CambioEstadoDocumentacion actual = obtenerCambioEstadoActual();
        return actual == null ? null : actual.getDescripcionEstado();
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
