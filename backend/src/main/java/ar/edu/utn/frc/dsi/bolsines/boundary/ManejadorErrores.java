package ar.edu.utn.frc.dsi.bolsines.boundary;

import ar.edu.utn.frc.dsi.bolsines.control.ReglaNegocioException;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.MensajeResponse;
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
}
