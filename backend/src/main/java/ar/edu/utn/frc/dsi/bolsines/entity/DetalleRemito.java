package ar.edu.utn.frc.dsi.bolsines.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class DetalleRemito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Área de la sede central a la que debe redirigirse la documentación (solo CMC). */
    private String areaCMCDestino;
    @ManyToOne(optional = false)
    private Documentacion documentacion;

    protected DetalleRemito() {
    }

    public DetalleRemito(Documentacion documentacion) {
        this.documentacion = documentacion;
    }

    public boolean esDeDocumentacion(Integer numeroDocumentacion) {
        return documentacion.esTuNumero(numeroDocumentacion);
    }

    public void aceptarDocumentacion(Empleado responsable, LocalDateTime fechaHora) {
        documentacion.aceptar(responsable, fechaHora);
    }

    public void noRecibirDocumentacion(Empleado responsable, LocalDateTime fechaHora) {
        documentacion.noRecibir(responsable, fechaHora);
    }

    public void rechazarDocumentacion(Empleado responsable, LocalDateTime fechaHora) {
        documentacion.rechazar(responsable, fechaHora);
    }

    public void redirigirDocumentacion(String areaCMCDestino, Empleado responsable, LocalDateTime fechaHora) {
        documentacion.redirigir(responsable, fechaHora);
        this.areaCMCDestino = areaCMCDestino;
    }

    /** Verdadero si la documentación quedó No recibida o Recibida y rechazada. */
    public boolean esDocumentacionFaltanteORechazada() {
        return documentacion.esNoRecibida() || documentacion.esRecibidaYRechazada();
    }

    public String getAreaCMCDestino() {
        return areaCMCDestino;
    }

    public Integer getNumeroDocumentacion() {
        return documentacion.getNumero();
    }

    public String getAsuntoDocumentacion() {
        return documentacion.getAsunto();
    }

    public String getTipoDocumentacion() {
        return documentacion.getNombreTipoDocumento();
    }

    public Documentacion getDocumentacion() {
        return documentacion;
    }
}
