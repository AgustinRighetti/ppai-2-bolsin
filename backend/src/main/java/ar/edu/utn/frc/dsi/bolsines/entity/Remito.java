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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Remito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Integer numero;
    private LocalDate fecha;
    @ManyToOne(optional = false)
    private ComisionMedica origen;
    @ManyToOne(optional = false)
    private ComisionMedica destino;
    @ManyToOne(optional = false)
    private Estado estado;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "remito_id")
    private List<DetalleRemito> detalleRemito = new ArrayList<>();

    protected Remito() {
    }

    public Remito(Integer numero, LocalDate fecha, ComisionMedica origen, ComisionMedica destino, Estado estado) {
        this.numero = numero;
        this.fecha = fecha;
        this.origen = origen;
        this.destino = destino;
        this.estado = estado;
    }

    public void agregarDocumentacion(Documentacion documentacion) {
        detalleRemito.add(new DetalleRemito(documentacion));
    }

    public boolean contieneDocumentacion(Integer numeroDocumentacion) {
        return detalleRemito.stream().anyMatch(detalle -> detalle.esDeDocumentacion(numeroDocumentacion));
    }

    // DEFENSA (3. Dónde aparece el patrón): el remito le manda a cada documentación el EVENTO que
    // corresponde (aceptar, noRecibir, rechazar o redirigir). No sabe en qué estado está la
    // documentación ni a cuál va a pasar: eso lo resuelve el estado concreto (patrón State).

    /** Opción 1 (flujo descripto): toda la documentación llegó como estaba registrada. */
    public void registrarRecepcionCompleta(Estado estadoRecibidoYAceptado, Empleado responsable,
                                           LocalDateTime fechaHora) {
        for (DetalleRemito detalle : detalleRemito) {
            detalle.aceptarDocumentacion(responsable, fechaHora);
        }
        setEstado(estadoRecibidoYAceptado);
    }

    /** Opción 2: la documentación marcada no llegó; el resto se acepta. */
    public void registrarDocumentacionNoRecibida(List<Integer> numerosDocumentacion, Estado estadoRecibidoYAceptado,
                                                 Estado estadoRecibidoYAceptadoParcial, Empleado responsable,
                                                 LocalDateTime fechaHora) {
        for (DetalleRemito detalle : detalleRemito) {
            if (detalle.esDeAlgunaDocumentacion(numerosDocumentacion)) {
                detalle.noRecibirDocumentacion(responsable, fechaHora);
            } else {
                detalle.aceptarDocumentacion(responsable, fechaHora);
            }
        }
        actualizarEstado(estadoRecibidoYAceptado, estadoRecibidoYAceptadoParcial);
    }

    /** Opción 3: la documentación marcada no corresponde a la CM destino; el resto se acepta. */
    public void registrarDocumentacionRechazada(List<Integer> numerosDocumentacion, Estado estadoRecibidoYAceptado,
                                                Estado estadoRecibidoYAceptadoParcial, Empleado responsable,
                                                LocalDateTime fechaHora) {
        for (DetalleRemito detalle : detalleRemito) {
            if (detalle.esDeAlgunaDocumentacion(numerosDocumentacion)) {
                detalle.rechazarDocumentacion(responsable, fechaHora);
            } else {
                detalle.aceptarDocumentacion(responsable, fechaHora);
            }
        }
        actualizarEstado(estadoRecibidoYAceptado, estadoRecibidoYAceptadoParcial);
    }

    /** Opción 4 (solo CMC): la documentación marcada se redirige a un área; el resto se acepta. */
    public void registrarDocumentacionARedirigir(List<Integer> numerosDocumentacion, String areaCMCDestino,
                                                 Estado estadoRecibidoYAceptado, Estado estadoRecibidoYAceptadoParcial,
                                                 Empleado responsable, LocalDateTime fechaHora) {
        for (DetalleRemito detalle : detalleRemito) {
            if (detalle.esDeAlgunaDocumentacion(numerosDocumentacion)) {
                detalle.redirigirDocumentacion(areaCMCDestino, responsable, fechaHora);
            } else {
                detalle.aceptarDocumentacion(responsable, fechaHora);
            }
        }
        actualizarEstado(estadoRecibidoYAceptado, estadoRecibidoYAceptadoParcial);
    }

    /** Queda Recibido y Aceptado Parcial si alguna documentación quedó no recibida o rechazada. */
    private void actualizarEstado(Estado estadoRecibidoYAceptado, Estado estadoRecibidoYAceptadoParcial) {
        if (tenesDocumentacionFaltanteORechazada()) {
            setEstado(estadoRecibidoYAceptadoParcial);
        } else {
            setEstado(estadoRecibidoYAceptado);
        }
    }

    private boolean tenesDocumentacionFaltanteORechazada() {
        for (DetalleRemito detalle : detalleRemito) {
            if (detalle.esDocumentacionFaltanteORechazada()) {
                return true;
            }
        }
        return false;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getNombreEstado() {
        return estado.getNombre();
    }

    public Integer getNumero() {
        return numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public ComisionMedica getOrigen() {
        return origen;
    }

    public ComisionMedica getDestino() {
        return destino;
    }

    public List<DetalleRemito> getDetalleRemito() {
        return detalleRemito;
    }
}
