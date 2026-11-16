package ar.edu.utn.frc.dsi.bolsines.boundary;

import ar.edu.utn.frc.dsi.bolsines.control.ReglaNegocioException;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.MensajeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las situaciones de negocio a respuestas HTTP con un mensaje para mostrar en pantalla. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<MensajeResponse> manejar(ReglaNegocioException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new MensajeResponse(ex.getMessage()));
    }

    /** Transición no permitida por la máquina de estados de Documentación (patrón State). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MensajeResponse> manejarTransicionInvalida(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MensajeResponse(ex.getMessage()));
    }
}
