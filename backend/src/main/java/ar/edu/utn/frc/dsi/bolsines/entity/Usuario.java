package ar.edu.utn.frc.dsi.bolsines.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String nombre;
    private String password;
    @ManyToOne(optional = false)
    private Empleado empleado;

    protected Usuario() {
    }

    public Usuario(String nombre, String password, Empleado empleado) {
        this.nombre = nombre;
        this.password = password;
        this.empleado = empleado;
    }

    public boolean esTuUsuario(String nombre) {
        return this.nombre.equals(nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public Empleado getEmpleado() {
        return empleado;
    }
}
