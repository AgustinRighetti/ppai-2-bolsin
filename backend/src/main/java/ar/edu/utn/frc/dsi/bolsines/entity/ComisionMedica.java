package ar.edu.utn.frc.dsi.bolsines.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ComisionMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String codigo;
    private String nombre;
    private String direccion;
    private String email;
    private String telefono;
    private boolean central;

    protected ComisionMedica() {
    }

    public ComisionMedica(String codigo, String nombre, String direccion, String email, String telefono, boolean central) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
        this.central = central;
    }

    /** Una comisión se reconoce a sí misma por su código. */
    public boolean esTuCodigo(String codigo) {
        return this.codigo.equals(codigo);
    }

    public boolean sosVos(ComisionMedica otra) {
        return otra != null && this.codigo.equals(otra.codigo);
    }

    /** Verdadero si es la Comisión Médica Central (CMC). */
    public boolean esCentral() {
        return central;
    }

    public boolean tuNombreContiene(String texto) {
        return nombre.toLowerCase().contains(texto.toLowerCase());
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }
}
