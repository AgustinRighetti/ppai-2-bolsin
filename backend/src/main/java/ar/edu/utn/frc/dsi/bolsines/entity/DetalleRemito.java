package ar.edu.utn.frc.dsi.bolsines.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.List;

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

    /** Verdadero si la documentación del detalle es una de las marcadas por el EB. */
    public boolean esDeAlgunaDocumentacion(List<Integer> numerosDocumentacion) {
        for (Integer numero : numerosDocumentacion) {
            if (documentacion.esTuNumero(numero)) {
                return true;
            }
        }
        return false;
    }

    public void setAreaCMCDestino(String areaCMCDestino) {
        this.areaCMCDestino = areaCMCDestino;
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
