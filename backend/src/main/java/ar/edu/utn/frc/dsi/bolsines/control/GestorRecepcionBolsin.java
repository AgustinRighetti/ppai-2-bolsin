package ar.edu.utn.frc.dsi.bolsines.control;

import ar.edu.utn.frc.dsi.bolsines.boundary.InterfazEmail;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.BolsinResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.DetalleBolsinResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.DocumentacionResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.InicioResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.NotificacionResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.OpcionRecepcionResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.PedidoConfirmacionResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.RemitoResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.ResultadoRecepcionResponse;
import ar.edu.utn.frc.dsi.bolsines.entity.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entity.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.entity.DetalleRemito;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entity.Estado;
import ar.edu.utn.frc.dsi.bolsines.entity.Remito;
import ar.edu.utn.frc.dsi.bolsines.entity.Sesion;
import ar.edu.utn.frc.dsi.bolsines.entity.SituacionRecepcion;
import ar.edu.utn.frc.dsi.bolsines.repository.BolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.EstadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.SesionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Clase de control del CU 28 Registrar recepción de bolsín.
 *
 * Coordina el caso de uso y envía los mensajes de recepción a las entidades. No decide a qué
 * estado pasa cada documentación: eso lo resuelve el patrón State dentro de Documentacion.
 */
@Service
public class GestorRecepcionBolsin {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SesionRepository sesionRepository;
    private final BolsinRepository bolsinRepository;
    private final EstadoRepository estadoRepository;
    private final InterfazEmail interfazEmail;

    // Estado de la ejecución en curso del caso de uso
    private Empleado empleadoLogueado;
    private ComisionMedica cmLogueada;
    private Integer numeroBolsinSeleccionado;
    private SituacionRecepcion situacionSeleccionada;
    private List<Integer> documentacionSeleccionada = new ArrayList<>();
    private String areaDestino;
    private LocalDateTime fechaHoraActual;

    public GestorRecepcionBolsin(SesionRepository sesionRepository, BolsinRepository bolsinRepository,
                                 EstadoRepository estadoRepository, InterfazEmail interfazEmail) {
        this.sesionRepository = sesionRepository;
        this.bolsinRepository = bolsinRepository;
        this.estadoRepository = estadoRepository;
        this.interfazEmail = interfazEmail;
    }

    // ------------------------------------------------------------------ Pasos 1 a 4

    @Transactional(readOnly = true)
    public InicioResponse nuevaRecepcionBolsin() {
        limpiarSeleccion();
        buscarCMLogueado();
        List<Bolsin> enviados = buscarBolsinesEnviados();
        return new InicioResponse(
                obtenerSesionActual().getUsuario().getNombre(),
                empleadoLogueado.getNombreCompleto(),
                cmLogueada.getNombre(),
                cmLogueada.esCentral(),
                enviados.stream().map(this::mostrarDatosBolsin).toList());
    }

    private void buscarCMLogueado() {
        Sesion actual = obtenerSesionActual();
        this.empleadoLogueado = actual.getUsuario().getEmpleado();
        this.cmLogueada = empleadoLogueado.getCM();
    }

    private Sesion obtenerSesionActual() {
        return sesionRepository.findAll().stream()
                .filter(Sesion::esActual)
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "No hay un usuario logueado."));
    }

    /** Bolsines en estado Enviado cuya CM destino es la CM del usuario logueado. */
    private List<Bolsin> buscarBolsinesEnviados() {
        return bolsinRepository.findAll().stream()
                .filter(bolsin -> bolsin.esTuCMDestino(cmLogueada) && bolsin.sosEnviado())
                .toList();
    }

    private BolsinResponse mostrarDatosBolsin(Bolsin bolsin) {
        return new BolsinResponse(bolsin.getNumeroBolsin(), bolsin.getNombreCMOrigen(), bolsin.getNumeroPrecinto());
    }

    /** Filtro del listado por número de precinto y/o CM origen (flujos alternativos A1 y A2). */
    @Transactional(readOnly = true)
    public List<BolsinResponse> tomarFiltroBolsines(String numeroPrecinto, String cmOrigen) {
        validarCUIniciado();
        boolean filtraPrecinto = numeroPrecinto != null && !numeroPrecinto.isBlank();
        boolean filtraOrigen = cmOrigen != null && !cmOrigen.isBlank();

        List<Bolsin> enviados = buscarBolsinesEnviados();
        if (filtraPrecinto) {
            enviados = enviados.stream().filter(b -> b.tenesPrecinto(numeroPrecinto.trim())).toList();
            if (enviados.isEmpty()) {
                throw new ReglaNegocioException(HttpStatus.NOT_FOUND,
                        "No se encuentra bolsín con el número de precinto ingresado.");
            }
        }
        if (filtraOrigen) {
            enviados = enviados.stream().filter(b -> b.tuCMOrigenSeLlama(cmOrigen.trim())).toList();
            if (enviados.isEmpty()) {
                throw new ReglaNegocioException(HttpStatus.NOT_FOUND,
                        "No se encuentra bolsín con la CM de origen ingresada.");
            }
        }
        return enviados.stream().map(this::mostrarDatosBolsin).toList();
    }

    // ------------------------------------------------------------------ Pasos 5 a 7

    @Transactional(readOnly = true)
    public DetalleBolsinResponse tomarSeleccionBolsin(Integer numeroBolsin) {
        validarCUIniciado();
        Bolsin bolsin = buscarBolsinesEnviados().stream()
                .filter(b -> b.esTuNumero(numeroBolsin))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND,
                        "El bolsín seleccionado no está pendiente de recepción en esta comisión médica."));

        this.numeroBolsinSeleccionado = bolsin.getNumeroBolsin();
        this.situacionSeleccionada = null;

        return new DetalleBolsinResponse(
                bolsin.getNumeroBolsin(),
                bolsin.getNombreCMOrigen(),
                bolsin.getNumeroPrecinto(),
                buscarRemitos(bolsin),
                buscarOpcionesRecepcion(bolsin));
    }

    /** Para cada remito del bolsín: su número y, por cada documentación, asunto y tipo. */
    private List<RemitoResponse> buscarRemitos(Bolsin bolsin) {
        List<RemitoResponse> remitos = new ArrayList<>();
        for (Remito remito : bolsin.getRemitos()) {
            List<DocumentacionResponse> documentacion = new ArrayList<>();
            for (DetalleRemito detalle : remito.getDetalleRemito()) {
                documentacion.add(new DocumentacionResponse(
                        detalle.getNumeroDocumentacion(),
                        detalle.getAsuntoDocumentacion(),
                        detalle.getTipoDocumentacion(),
                        detalle.getDocumentacion().getDescripcionEstadoActual(),
                        detalle.getAreaCMCDestino()));
            }
            remitos.add(new RemitoResponse(remito.getNumero(), remito.getNombreEstado(), documentacion));
        }
        return remitos;
    }

    private List<OpcionRecepcionResponse> buscarOpcionesRecepcion(Bolsin bolsin) {
        List<OpcionRecepcionResponse> opciones = new ArrayList<>();
        for (SituacionRecepcion situacion : SituacionRecepcion.values()) {
            opciones.add(new OpcionRecepcionResponse(situacion.getCodigo(), situacion.getDescripcion(),
                    situacion.requiereSeleccionDocumentacion(), situacion.requiereAreaDestino(),
                    situacion.esAplicable(bolsin)));
        }
        return opciones;
    }

    // ------------------------------------------------------------------ Pasos 8 y 9

    @Transactional(readOnly = true)
    public PedidoConfirmacionResponse tomarSeleccionOpcion(Integer opcion, List<Integer> numerosDocumentacion,
                                                           String areaDestino) {
        Bolsin bolsin = obtenerBolsinSeleccionado();
        SituacionRecepcion elegida = SituacionRecepcion.desdeCodigo(opcion)
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                        "Debe seleccionar una opción de recepción válida."));
        List<Integer> documentacion = numerosDocumentacion == null ? List.of() : List.copyOf(numerosDocumentacion);

        if (!elegida.esAplicable(bolsin)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "La opción solo puede usarse cuando la CM destino es la Comisión Médica Central.");
        }
        if (elegida.requiereSeleccionDocumentacion()) {
            if (documentacion.isEmpty()) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                        "Debe indicar al menos una documentación para la opción seleccionada.");
            }
            if (!bolsin.contieneDocumentaciones(documentacion)) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                        "La documentación indicada no pertenece al bolsín seleccionado.");
            }
        } else {
            documentacion = List.of();
        }
        String area = null;
        if (elegida.requiereAreaDestino()) {
            if (areaDestino == null || areaDestino.isBlank()) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                        "Debe indicar el área de la sede central a la que se redirige la documentación.");
            }
            area = areaDestino.trim();
        }

        this.situacionSeleccionada = elegida;
        this.documentacionSeleccionada = documentacion;
        this.areaDestino = area;

        return new PedidoConfirmacionResponse(opcion, elegida.getDescripcion(), bolsin.getNumeroBolsin(),
                documentacion, area,
                "¿Confirma el registro de la recepción del bolsín N° " + bolsin.getNumeroBolsin() + "?");
    }

    // ------------------------------------------------------------------ Pasos 10 a 12

    @Transactional
    public ResultadoRecepcionResponse tomarConfirmacion(boolean confirma) {
        Bolsin bolsin = obtenerBolsinSeleccionado();
        if (situacionSeleccionada == null) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Primero debe seleccionar una opción de recepción.");
        }
        if (!confirma) {
            // A6: el EB no confirma. Se descarta la opción y vuelve a elegir.
            this.situacionSeleccionada = null;
            this.documentacionSeleccionada = new ArrayList<>();
            this.areaDestino = null;
            return new ResultadoRecepcionResponse(false,
                    "No se registró la recepción. Puede seleccionar nuevamente la opción de recepción.",
                    bolsin.getNumeroBolsin(), bolsin.getNombreEstadoActual(), null, null, List.of(), List.of());
        }

        obtenerFechaYHora();
        List<Estado> estados = buscarEstados();
        actualizarEstados(bolsin, estados);
        bolsinRepository.save(bolsin);
        List<NotificacionResponse> notificaciones = notificarRecepcionBolsin(bolsin);

        ResultadoRecepcionResponse resultado = new ResultadoRecepcionResponse(true,
                "La recepción del bolsín N° " + bolsin.getNumeroBolsin() + " se registró con éxito.",
                bolsin.getNumeroBolsin(), bolsin.getNombreEstadoActual(), empleadoLogueado.getNombreCompleto(),
                fechaHoraActual.format(FORMATO_FECHA_HORA), buscarRemitos(bolsin), notificaciones);
        finCU();
        return resultado;
    }

    private void obtenerFechaYHora() {
        this.fechaHoraActual = LocalDateTime.now();
    }

    private List<Estado> buscarEstados() {
        return estadoRepository.findAll();
    }

    /**
     * El bolsín pasa a Recibido en CM destino y cada remito registra la recepción de su
     * documentación. Los cambios de estado de la documentación los resuelve el patrón State.
     */
    private void actualizarEstados(Bolsin bolsin, List<Estado> estados) {
        Estado recibidoEnCMDestino = buscarEstado(estados, e -> e.esAmbitoBolsin() && e.esRecibidoEnCMDestino());
        Estado remitoAceptado = buscarEstado(estados, e -> e.esAmbitoRemito() && e.esRecibidoYAceptado());
        Estado remitoAceptadoParcial = buscarEstado(estados,
                e -> e.esAmbitoRemito() && e.esRecibidoYAceptadoParcial());

        bolsin.recibir(recibidoEnCMDestino, empleadoLogueado, fechaHoraActual);
        for (Remito remito : bolsin.getRemitos()) {
            remito.registrarRecepcion(situacionSeleccionada, documentacionSeleccionada, areaDestino,
                    remitoAceptado, remitoAceptadoParcial, empleadoLogueado, fechaHoraActual);
        }
    }

    /** Cada Estado responde por su ámbito y su nombre. */
    private Estado buscarEstado(List<Estado> estados, Predicate<Estado> criterio) {
        return estados.stream()
                .filter(criterio)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encuentra parametrizado un estado requerido"));
    }

    /**
     * Inclusión del CU 29 Notificar recepción de bolsín: un e-mail por cada CM de origen
     * que envió documentación, con la situación de cada documentación recibida.
     */
    private List<NotificacionResponse> notificarRecepcionBolsin(Bolsin bolsin) {
        Map<ComisionMedica, List<String>> situacionPorCMOrigen = new LinkedHashMap<>();
        for (Remito remito : bolsin.getRemitos()) {
            for (DetalleRemito detalle : remito.getDetalleRemito()) {
                Documentacion documentacion = detalle.getDocumentacion();
                String linea = "- Remito " + remito.getNumero() + " | Doc. N° " + documentacion.getNumero()
                        + " (" + documentacion.getNombreTipoDocumento() + ") \"" + documentacion.getAsunto() + "\": "
                        + documentacion.getDescripcionEstadoActual()
                        + (detalle.getAreaCMCDestino() != null ? " -> " + detalle.getAreaCMCDestino() : "");
                ComisionMedica cmOrigen = buscarClave(situacionPorCMOrigen, documentacion.getComisionMedicaOrigen());
                situacionPorCMOrigen.computeIfAbsent(cmOrigen, cm -> new ArrayList<>()).add(linea);
            }
        }

        List<NotificacionResponse> notificaciones = new ArrayList<>();
        for (Map.Entry<ComisionMedica, List<String>> entrada : situacionPorCMOrigen.entrySet()) {
            String asunto = "Recepción del bolsín N° " + bolsin.getNumeroBolsin() + " en " + cmLogueada.getNombre();
            String cuerpo = "El bolsín N° " + bolsin.getNumeroBolsin() + " (precinto " + bolsin.getNumeroPrecinto()
                    + ") fue recibido el " + fechaHoraActual.format(FORMATO_FECHA_HORA) + " por "
                    + empleadoLogueado.getNombreCompleto() + ".\nSituación de la documentación enviada:\n"
                    + String.join("\n", entrada.getValue());
            interfazEmail.enviarEmail(entrada.getKey().getEmail(), asunto, cuerpo);
            notificaciones.add(new NotificacionResponse(
                    entrada.getKey().getNombre() + " <" + entrada.getKey().getEmail() + ">", asunto, cuerpo));
        }
        return notificaciones;
    }

    private ComisionMedica buscarClave(Map<ComisionMedica, List<String>> mapa, ComisionMedica comisionMedica) {
        return mapa.keySet().stream().filter(cm -> cm.sosVos(comisionMedica)).findFirst().orElse(comisionMedica);
    }

    // ------------------------------------------------------------------ Cancelación y soporte

    /** Observación 1: el EB puede cancelar el caso de uso cuando lo decida. */
    public void cancelarCU() {
        finCU();
    }

    private void finCU() {
        limpiarSeleccion();
        this.empleadoLogueado = null;
        this.cmLogueada = null;
    }

    private void limpiarSeleccion() {
        this.numeroBolsinSeleccionado = null;
        this.situacionSeleccionada = null;
        this.documentacionSeleccionada = new ArrayList<>();
        this.areaDestino = null;
        this.fechaHoraActual = null;
    }

    private void validarCUIniciado() {
        if (cmLogueada == null) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Debe iniciar el registro de recepción de bolsín antes de continuar.");
        }
    }

    private Bolsin obtenerBolsinSeleccionado() {
        validarCUIniciado();
        if (numeroBolsinSeleccionado == null) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Primero debe seleccionar un bolsín.");
        }
        return bolsinRepository.findAll().stream()
                .filter(b -> b.esTuNumero(numeroBolsinSeleccionado))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "No se encuentra el bolsín."));
    }
}
