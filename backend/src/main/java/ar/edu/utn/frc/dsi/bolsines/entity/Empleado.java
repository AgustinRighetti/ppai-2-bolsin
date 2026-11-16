package ar.edu.utn.frc.dsi.bolsines.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    @ManyToOne(optional = false)
    private ComisionMedica comisionMedica;

    protected Empleado() {
    }

    public Empleado(String nombre, String apellido, String email, ComisionMedica comisionMedica) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.comisionMedica = comisionMedica;
    }

    /** CM a la que está asignado el empleado. */
    public ComisionMedica getCM() {
        return comisionMedica;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }
}
