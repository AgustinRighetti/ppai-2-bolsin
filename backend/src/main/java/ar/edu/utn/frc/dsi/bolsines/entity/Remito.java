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

    /**
     * Registra la recepción del remito: a cada documentación le envía el evento que corresponde
     * según la situación informada por el EB. Qué estado resulta lo resuelve el patrón State.
     * Luego el remito queda Recibido y Aceptado o, si alguna documentación quedó no recibida o
     * rechazada, Recibido y Aceptado Parcial.
     */
    // DEFENSA (3. Dónde aparece el patrón): el remito le manda a cada documentación un EVENTO según
    // la opción: aceptar, noRecibir, rechazar o redirigir. No sabe en qué estado está la documentación
    // ni a cuál va a pasar: eso lo resuelve el State. (El switch es sobre la OPCIÓN elegida, no sobre
    // el estado.)
    public void registrarRecepcion(SituacionRecepcion situacion, List<Integer> numerosDocConNovedad,
                                   String areaCMCDestino, Estado estadoAceptado, Estado estadoAceptadoParcial,
                                   Empleado responsable, LocalDateTime fechaHora) {
        for (DetalleRemito detalle : detalleRemito) {
            boolean conNovedad = numerosDocConNovedad.stream().anyMatch(detalle::esDeDocumentacion);
            if (!conNovedad) {
                detalle.aceptarDocumentacion(responsable, fechaHora);
                continue;
            }
            switch (situacion) {
                case NO_SE_RECIBE_TODA -> detalle.noRecibirDocumentacion(responsable, fechaHora);
                case NO_CORRESPONDE_AL_DESTINO -> detalle.rechazarDocumentacion(responsable, fechaHora);
                case REDIRIGIR_A_OTRA_AREA -> detalle.redirigirDocumentacion(areaCMCDestino, responsable, fechaHora);
                default -> detalle.aceptarDocumentacion(responsable, fechaHora);
            }
        }
        setEstado(tenesDocumentacionFaltanteORechazada() ? estadoAceptadoParcial : estadoAceptado);
    }

    private boolean tenesDocumentacionFaltanteORechazada() {
        return detalleRemito.stream().anyMatch(DetalleRemito::esDocumentacionFaltanteORechazada);
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
