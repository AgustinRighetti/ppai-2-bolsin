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

    /** Verdadero si alguna de las documentaciones indicadas está incluida en este remito. */
    public boolean contieneAlgunaDocumentacion(List<Integer> numerosDocumentacion) {
        return detalleRemito.stream()
                .anyMatch(detalle -> numerosDocumentacion.stream().anyMatch(detalle::esDeDocumentacion));
    }

    public boolean contieneDocumentacion(Integer numeroDocumentacion) {
        return detalleRemito.stream().anyMatch(detalle -> detalle.esDeDocumentacion(numeroDocumentacion));
    }

    /**
     * Registra la recepción del remito: toma el nuevo estado del remito y actualiza cada
     * documentación. Las documentaciones con novedad (no recibidas, rechazadas o a redirigir)
     * pasan a estadoDocConNovedad; el resto pasa a estadoDocAceptada.
     */
    public void recibir(Estado estadoRemito, Estado estadoDocAceptada, List<Integer> numerosDocConNovedad,
                        Estado estadoDocConNovedad, String areaCMCDestino, Empleado responsable,
                        LocalDateTime fechaHora) {
        setEstado(estadoRemito);
        for (DetalleRemito detalle : detalleRemito) {
            boolean conNovedad = numerosDocConNovedad.stream().anyMatch(detalle::esDeDocumentacion);
            if (conNovedad) {
                detalle.actualizarEstadoDocumentacion(estadoDocConNovedad, responsable, fechaHora);
                if (areaCMCDestino != null) {
                    detalle.setAreaCMCDestino(areaCMCDestino);
                }
            } else {
                detalle.actualizarEstadoDocumentacion(estadoDocAceptada, responsable, fechaHora);
            }
        }
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
