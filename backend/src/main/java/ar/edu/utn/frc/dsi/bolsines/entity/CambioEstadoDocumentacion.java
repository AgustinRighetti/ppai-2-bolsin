package ar.edu.utn.frc.dsi.bolsines.entity;

import ar.edu.utn.frc.dsi.bolsines.state.EstadoDocumentacion;
import ar.edu.utn.frc.dsi.bolsines.state.EstadoDocumentacionConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

/** Historial de estados de una documentación: guarda cada estado por el que pasó y quién lo cambió. */
@Entity
public class CambioEstadoDocumentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    @Convert(converter = EstadoDocumentacionConverter.class)
    private EstadoDocumentacion estado;
    @ManyToOne(optional = false)
    private Empleado responsable;

    protected CambioEstadoDocumentacion() {
    }

    public CambioEstadoDocumentacion(LocalDateTime fechaHoraInicio, EstadoDocumentacion estado, Empleado responsable) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
        this.responsable = responsable;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public EstadoDocumentacion getEstado() {
        return estado;
    }

    public Empleado getResponsable() {
        return responsable;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }
}
