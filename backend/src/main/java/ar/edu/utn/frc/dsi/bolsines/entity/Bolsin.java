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
public class Bolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Integer numeroBolsin;
    private LocalDate fecha;
    private Integer peso;
    private String numeroPrecinto;
    @ManyToOne(optional = false)
    private ComisionMedica origen;
    @ManyToOne(optional = false)
    private ComisionMedica destino;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bolsin_id")
    @OrderBy("numero ASC")
    private List<Remito> remitos = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bolsin_id")
    @OrderBy("fechaHoraInicio ASC")
    private List<CambioEstadoBolsin> cambiosEstado = new ArrayList<>();

    protected Bolsin() {
    }

    public Bolsin(Integer numeroBolsin, LocalDate fecha, Integer peso, String numeroPrecinto,
                  ComisionMedica origen, ComisionMedica destino) {
        this.numeroBolsin = numeroBolsin;
        this.fecha = fecha;
        this.peso = peso;
        this.numeroPrecinto = numeroPrecinto;
        this.origen = origen;
        this.destino = destino;
    }

    public void asociarRemito(Remito remito) {
        remitos.add(remito);
    }

    public boolean esTuNumero(Integer numeroBolsin) {
        return this.numeroBolsin.equals(numeroBolsin);
    }

    public boolean esTuCMDestino(ComisionMedica comisionMedica) {
        return destino.sosVos(comisionMedica);
    }

    /** El bolsín está enviado si su cambio de estado actual corresponde al estado Enviado. */
    public boolean sosEnviado() {
        CambioEstadoBolsin actual = obtenerCambioEstadoActual();
        return actual != null && actual.sosEnviado();
    }

    public boolean tenesPrecinto(String numeroPrecinto) {
        return this.numeroPrecinto != null && this.numeroPrecinto.contains(numeroPrecinto);
    }

    public boolean tuCMOrigenSeLlama(String nombre) {
        return origen.tuNombreContiene(nombre);
    }

    public boolean tuDestinoEsCentral() {
        return destino.esCentral();
    }

    /** Verdadero si todas las documentaciones indicadas viajan en algún remito de este bolsín. */
    public boolean contieneDocumentaciones(List<Integer> numerosDocumentacion) {
        return numerosDocumentacion.stream()
                .allMatch(numero -> remitos.stream().anyMatch(remito -> remito.contieneDocumentacion(numero)));
    }

    /**
     * Registra la recepción del bolsín en la CM destino: cierra el cambio de estado
     * vigente y crea el nuevo con el empleado responsable.
     */
    public void recibir(Estado estadoRecibido, Empleado responsable, LocalDateTime fechaHora) {
        CambioEstadoBolsin actual = obtenerCambioEstadoActual();
        if (actual != null) {
            actual.setFechaHoraFin(fechaHora);
        }
        crearCambioEstado(estadoRecibido, responsable, fechaHora);
    }

    public void crearCambioEstado(Estado estado, Empleado responsable, LocalDateTime fechaHora) {
        cambiosEstado.add(new CambioEstadoBolsin(fechaHora, estado, responsable));
    }

    private CambioEstadoBolsin obtenerCambioEstadoActual() {
        return cambiosEstado.stream().filter(CambioEstadoBolsin::sosActual).findFirst().orElse(null);
    }

    public String getNombreEstadoActual() {
        CambioEstadoBolsin actual = obtenerCambioEstadoActual();
        return actual == null ? null : actual.getNombreEstado();
    }

    public String getNombreCMOrigen() {
        return origen.getNombre();
    }

    public Integer getNumeroBolsin() {
        return numeroBolsin;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Integer getPeso() {
        return peso;
    }

    public String getNumeroPrecinto() {
        return numeroPrecinto;
    }

    public ComisionMedica getOrigen() {
        return origen;
    }

    public ComisionMedica getDestino() {
        return destino;
    }

    public List<Remito> getRemitos() {
        return remitos;
    }

    public List<CambioEstadoBolsin> getCambiosEstado() {
        return cambiosEstado;
    }
}
