package ar.edu.utn.frc.dsi.bolsines.state;

/** <<ConcreteState>> Estado final: la documentación se dio de baja. No tiene transiciones de salida. */
public class DeBaja extends EstadoDocumentacion {

    public static final String NOMBRE = "DeBaja";

    public DeBaja() {
    }

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String getDescripcion() {
        return "De baja";
    }
}
