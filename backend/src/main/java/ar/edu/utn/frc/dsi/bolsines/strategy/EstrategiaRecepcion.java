package ar.edu.utn.frc.dsi.bolsines.strategy;

import ar.edu.utn.frc.dsi.bolsines.entity.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entity.Estado;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

/**
 * Patrón Strategy (Gamma) - rol <<Strategy>>.
 *
 * Declara la interfaz común a todas las formas de registrar la recepción de un bolsín.
 * El GestorRecepcionBolsin (Context) trabaja contra esta interfaz y no conoce cómo queda
 * cada remito y cada documentación según la situación detectada por el Encargado de Bolsines.
 */
public interface EstrategiaRecepcion {

    /** Texto de la opción que se le muestra al Encargado de Bolsines. */
    String getDescripcion();

    /** Indica si el EB debe marcar qué documentación presenta la novedad. */
    boolean requiereSeleccionDocumentacion();

    /** Indica si el EB debe informar el área de la sede central a la que se redirige. */
    boolean requiereAreaDestino();

    /** Indica si la opción puede aplicarse al bolsín (por ejemplo, redirigir solo aplica a la CMC). */
    boolean esAplicable(Bolsin bolsin);

    /**
     * Algoritmo variable: actualiza el estado de los remitos del bolsín y de la documentación
     * asociada según la situación de recepción que representa la estrategia.
     */
    void registrarRecepcion(Bolsin bolsin, List<Integer> numerosDocumentacion, String areaDestino,
                            List<Estado> estados, Empleado responsable, LocalDateTime fechaHora);

    /** Busca entre los estados el que cumple el criterio (cada Estado responde por su ámbito y nombre). */
    static Estado buscarEstado(List<Estado> estados, Predicate<Estado> criterio) {
        return estados.stream()
                .filter(criterio)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encuentra parametrizado un estado requerido"));
    }
}
