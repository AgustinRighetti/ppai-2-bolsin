package ar.edu.utn.frc.dsi.bolsines.boundary;

import ar.edu.utn.frc.dsi.bolsines.control.GestorRecepcionBolsin;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.BolsinResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.ConfirmacionRequest;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.DetalleBolsinResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.FiltrarBolsinesRequest;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.InicioResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.MensajeResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.PedidoConfirmacionResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.ResultadoRecepcionResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.SeleccionarBolsinRequest;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.SeleccionarOpcionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Boundary del CU 28: punto de entrada de la API que consume la interfaz React.
 * Los mensajes Gestor -> Pantalla de la realización (mostrarDatosBolsin, pedirSeleccionOpcion,
 * pedirConfirmacion, informarEjecucion) viajan como retorno de cada método, por ser una API REST.
 */
@RestController
@RequestMapping("/api/recepcion-bolsin")
public class PantallaRegistrarRecepcion {

    private final GestorRecepcionBolsin gestor;

    public PantallaRegistrarRecepcion(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<InicioResponse> opcionRegistrarRecepcionBolsin() {
        return ResponseEntity.ok(habilitarPantalla());
    }

    private InicioResponse habilitarPantalla() {
        return gestor.nuevaRecepcionBolsin();
    }

    @PostMapping("/filtrar")
    public ResponseEntity<List<BolsinResponse>> tomarFiltroBolsines(@RequestBody FiltrarBolsinesRequest request) {
        return ResponseEntity.ok(gestor.tomarFiltroBolsines(request.numeroPrecinto(), request.cmOrigen()));
    }

    @PostMapping("/seleccionar-bolsin")
    public ResponseEntity<DetalleBolsinResponse> tomarSeleccionBolsin(@RequestBody SeleccionarBolsinRequest request) {
        return ResponseEntity.ok(gestor.tomarSeleccionBolsin(request.numeroBolsin()));
    }

    @PostMapping("/seleccionar-opcion")
    public ResponseEntity<PedidoConfirmacionResponse> tomarSeleccionOpcion(@RequestBody SeleccionarOpcionRequest request) {
        return ResponseEntity.ok(gestor.tomarSeleccionOpcion(request.opcion(), request.numerosDocumentacion(),
                request.areaDestino()));
    }

    @PostMapping("/confirmar")
    public ResponseEntity<ResultadoRecepcionResponse> tomarConfirmacion(@RequestBody ConfirmacionRequest request) {
        return ResponseEntity.ok(gestor.tomarConfirmacion(request.confirma()));
    }

    @PostMapping("/cancelar")
    public ResponseEntity<MensajeResponse> tomarCancelacion() {
        gestor.cancelarCU();
        return ResponseEntity.ok(new MensajeResponse("Se canceló el registro de recepción de bolsín."));
    }
}
