package ar.edu.utn.frc.dsi.bolsines.state;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persistencia del patrón State: en la base se guarda el nombre del estado y, al leer,
 * se vuelve a crear el objeto del estado concreto correspondiente.
 */
// DEFENSA (5. Cierre): guarda el NOMBRE del estado en la base ("RecibidaYAceptada") y al leer vuelve
// a crear el objeto del estado concreto (desdeNombre). Bolsín y Remito NO usan State: la profe pidió
// State solo para Documentación.
@Converter
public class EstadoDocumentacionConverter implements AttributeConverter<EstadoDocumentacion, String> {

    @Override
    public String convertToDatabaseColumn(EstadoDocumentacion estado) {
        return estado == null ? null : estado.getNombre();
    }

    @Override
    public EstadoDocumentacion convertToEntityAttribute(String nombre) {
        return nombre == null ? null : EstadoDocumentacion.desdeNombre(nombre);
    }
}
