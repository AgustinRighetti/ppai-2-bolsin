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
        for (DetalleRemito detalle : detalleRemito) {
            if (detalle.esDeDocumentacion(numeroDocumentacion)) {
                return true;
            }
        }
        return false;
    }

    /** Verdadero si alguna de las documentaciones indicadas viaja en este remito. */
    public boolean contieneAlgunaDocumentacion(List<Integer> numerosDocumentacion) {
        for (Integer numero : numerosDocumentacion) {
            if (contieneDocumentacion(numero)) {
                return true;
            }
        }
        return false;
    }

    /** El remito pasa al estado recibido (Recibido y Aceptado o Recibido y Aceptado Parcial). */
    public void actualizarEstado(Estado nuevoEstado) {
        setEstado(nuevoEstado);
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
