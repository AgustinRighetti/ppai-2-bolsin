package ar.edu.utn.frc.dsi.bolsines.entity;

import java.util.Arrays;
import java.util.Optional;

/** Opciones de recepción que el Encargado de Bolsines elige en el paso 7 del CU 28. */
public enum SituacionRecepcion {

    CONTENIDO_IGUAL(1, "El contenido del bolsín es igual al registrado", false, false),
    NO_SE_RECIBE_TODA(2, "No se recibe toda la documentación asociada a los remitos que contiene el bolsín", true, false),
    NO_CORRESPONDE_AL_DESTINO(3, "Existe documentación que no corresponde al destino", true, false),
    REDIRIGIR_A_OTRA_AREA(4, "La documentación se debe redirigir a otra área", true, true);

    private final int codigo;
    private final String descripcion;
    private final boolean requiereSeleccionDocumentacion;
    private final boolean requiereAreaDestino;

    SituacionRecepcion(int codigo, String descripcion, boolean requiereSeleccionDocumentacion,
                       boolean requiereAreaDestino) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.requiereSeleccionDocumentacion = requiereSeleccionDocumentacion;
        this.requiereAreaDestino = requiereAreaDestino;
    }

    public static Optional<SituacionRecepcion> desdeCodigo(Integer codigo) {
        return Arrays.stream(values()).filter(s -> codigo != null && s.codigo == codigo).findFirst();
    }

    /** Redirigir a otra área solo aplica cuando la CM destino es la Comisión Médica Central. */
    public boolean esAplicable(Bolsin bolsin) {
        return this != REDIRIGIR_A_OTRA_AREA || bolsin.tuDestinoEsCentral();
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean requiereSeleccionDocumentacion() {
        return requiereSeleccionDocumentacion;
    }

    public boolean requiereAreaDestino() {
        return requiereAreaDestino;
    }
}
