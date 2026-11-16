package ar.edu.utn.frc.dsi.bolsines.boundary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Boundary hacia el actor secundario Servidor de Correo (CU 29 Notificar recepción de bolsín).
 * En esta entrega el envío se simula escribiendo el e-mail en el log de la aplicación.
 */
@Component
public class InterfazEmail {

    private static final Logger log = LoggerFactory.getLogger(InterfazEmail.class);

    public void enviarEmail(String destinatario, String asunto, String cuerpo) {
        log.info("\n--- E-MAIL ENVIADO ---\nPara: {}\nAsunto: {}\n{}\n----------------------", destinatario, asunto, cuerpo);
    }
}
