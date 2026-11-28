package ar.edu.utn.frc.dsi.bolsines.state;

/** <<ConcreteState>> Estado final: la documentación llegó a destino y fue aceptada. No tiene transiciones de salida. */
public class RecibidaYAceptada extends EstadoDocumentacion {

    public static final String NOMBRE = "RecibidaYAceptada";

    public RecibidaYAceptada() {
    }

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "Recibida y aceptada";
    }
}
