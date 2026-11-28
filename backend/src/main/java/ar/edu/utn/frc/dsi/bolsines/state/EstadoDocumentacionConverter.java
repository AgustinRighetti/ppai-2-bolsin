package ar.edu.utn.frc.dsi.bolsines.state;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persistencia del patrón State: en la base se guarda el nombre del estado y, al leer,
 * se vuelve a crear el objeto del estado concreto correspondiente.
 */
// DEFENSA (5. Cierre): guarda el NOMBRE del estado en la base ("RecibidaYAceptada") y al leer vuelve
// a crear el objeto del estado concreto. Bolsín y Remito NO usan State: la profe pidió State solo
// para Documentación.
@Converter
public class EstadoDocumentacionConverter implements AttributeConverter<EstadoDocumentacion, String> {

    @Override
    public String convertToDatabaseColumn(EstadoDocumentacion estado) {
        return estado == null ? null : estado.getNombre();
    }

    @Override
    public EstadoDocumentacion convertToEntityAttribute(String nombre) {
        if (nombre == null) {
            return null;
        }
        return switch (nombre) {
            case Registrada.NOMBRE -> new Registrada();
            case EnRemito.NOMBRE -> new EnRemito();
            case EnBolsinSaliente.NOMBRE -> new EnBolsinSaliente();
            case EnBolsinEnviado.NOMBRE -> new EnBolsinEnviado();
            case RecibidaYAceptada.NOMBRE -> new RecibidaYAceptada();
            case NoRecibida.NOMBRE -> new NoRecibida();
            case RecibidaYRechazada.NOMBRE -> new RecibidaYRechazada();
            case ParaRedirigir.NOMBRE -> new ParaRedirigir();
            case DeBaja.NOMBRE -> new DeBaja();
            default -> throw new IllegalArgumentException("Estado de documentación desconocido: " + nombre);
        };
    }
}
