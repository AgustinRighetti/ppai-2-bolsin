package ar.edu.utn.frc.dsi.bolsines.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Estado de Bolsín y de Remito, identificado por ámbito y nombre (modelo de dominio).
 * La Documentación no usa esta clase: sus estados son la jerarquía del patrón State.
 */
@Entity
public class Estado {

    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String AMBITO_REMITO = "Remito";

    public static final String CREADO = "Creado";
    public static final String CERRADO = "Cerrado";
    public static final String ENVIADO = "Enviado";
    public static final String RECIBIDO_EN_CM_DESTINO = "RecibidoEnCMDestino";
    public static final String EN_BOLSIN_ENVIADO = "EnBolsinEnviado";
    public static final String RECIBIDO_Y_ACEPTADO = "RecibidoYAceptado";
    public static final String RECIBIDO_Y_ACEPTADO_PARCIAL = "RecibidoYAceptadoParcial";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ambito;
    private String nombre;
    private String descripcion;

    protected Estado() {
    }

    public Estado(String ambito, String nombre, String descripcion) {
        this.ambito = ambito;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public boolean esAmbitoBolsin() {
        return AMBITO_BOLSIN.equals(ambito);
    }

    public boolean esAmbitoRemito() {
        return AMBITO_REMITO.equals(ambito);
    }

    public boolean esEnviado() {
        return ENVIADO.equals(nombre);
    }

    public boolean esRecibidoEnCMDestino() {
        return RECIBIDO_EN_CM_DESTINO.equals(nombre);
    }

    public boolean esRecibidoYAceptado() {
        return RECIBIDO_Y_ACEPTADO.equals(nombre);
    }

    public boolean esRecibidoYAceptadoParcial() {
        return RECIBIDO_Y_ACEPTADO_PARCIAL.equals(nombre);
    }

    public String getAmbito() {
        return ambito;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
