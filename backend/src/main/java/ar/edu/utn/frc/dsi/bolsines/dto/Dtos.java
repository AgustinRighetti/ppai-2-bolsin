package ar.edu.utn.frc.dsi.bolsines.dto;

import java.util.List;

/**
 * Representaciones en datos primitivos que viajan entre la pantalla y el gestor.
 * Nunca se devuelve una entidad al boundary.
 */
public final class Dtos {

    private Dtos() {
    }

    // ---- Requests ----
    public record FiltrarBolsinesRequest(String numeroPrecinto, String cmOrigen) {
    }

    public record SeleccionarBolsinRequest(Integer numeroBolsin) {
    }

    public record SeleccionarOpcionRequest(Integer opcion, List<Integer> numerosDocumentacion, String areaDestino) {
    }

    public record ConfirmacionRequest(boolean confirma) {
    }

    public record CambiarUsuarioRequest(String usuario) {
    }

    // ---- Responses ----
    public record BolsinResponse(Integer numeroBolsin, String cmOrigen, String numeroPrecinto, String estado) {
    }

    public record InicioResponse(String usuario, String empleado, String comisionMedica, boolean esCentral,
                                 List<BolsinResponse> bolsines) {
    }

    public record DocumentacionResponse(Integer numero, String asunto, String tipoDocumento, String estado,
                                        String areaDestino) {
    }

    public record RemitoResponse(Integer numeroRemito, String estado, List<DocumentacionResponse> documentacion) {
    }

    public record OpcionRecepcionResponse(Integer codigo, String descripcion, boolean requiereSeleccionDocumentacion,
                                          boolean requiereAreaDestino, boolean habilitada) {
    }

    public record DetalleBolsinResponse(Integer numeroBolsin, String cmOrigen, String numeroPrecinto, String estado,
                                        List<RemitoResponse> remitos, List<OpcionRecepcionResponse> opciones) {
    }

    public record PedidoConfirmacionResponse(Integer opcion, String descripcion, Integer numeroBolsin,
                                             List<Integer> numerosDocumentacion, String areaDestino, String mensaje) {
    }

    public record NotificacionResponse(String destinatario, String asunto, String cuerpo) {
    }

    public record ResultadoRecepcionResponse(boolean registrado, String mensaje, Integer numeroBolsin,
                                             String estadoAnteriorBolsin, String estadoBolsin, String responsable, String fechaHora,
                                             List<RemitoResponse> remitos, List<NotificacionResponse> notificaciones) {
    }

    public record UsuarioResponse(String usuario, String empleado, String comisionMedica, boolean activo) {
    }

    public record MensajeResponse(String mensaje) {
    }
}
