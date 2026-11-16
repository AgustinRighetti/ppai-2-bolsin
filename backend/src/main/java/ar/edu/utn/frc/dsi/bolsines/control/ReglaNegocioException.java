package ar.edu.utn.frc.dsi.bolsines.control;

import org.springframework.http.HttpStatus;

/** Situación de negocio que impide continuar el paso actual del caso de uso (flujos alternativos). */
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus status;

    public ReglaNegocioException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
