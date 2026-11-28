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

/**
 * Clase de control del CU 28 Registrar recepción de bolsín.
 *
 * Coordina el caso de uso y envía los mensajes de recepción a las entidades. No decide a qué
 * estado pasa cada documentación: eso lo resuelve el patrón State dentro de Documentacion.
 */
// DEFENSA (1. Arquitectura): este es el CONTROL. Coordina el caso de uso y recuerda lo que se va
// eligiendo (atributos de abajo). Las entidades (Bolsin, Remito, DetalleRemito, Documentacion)
// responden sobre sus propios datos: patrón Experto.
@Service
public class GestorRecepcionBolsin {

    // Opciones de recepción que el EB elige en el paso 7 del CU 28
    private static final int OPCION_CONTENIDO_IGUAL = 1;
    private static final int OPCION_NO_SE_RECIBE_TODA = 2;
    private static final int OPCION_NO_CORRESPONDE_AL_DESTINO = 3;
    private static final int OPCION_REDIRIGIR_A_OTRA_AREA = 4;

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SesionRepository sesionRepository;
    private final BolsinRepository bolsinRepository;
    private final EstadoRepository estadoRepository;
    private final InterfazEmail interfazEmail;

    // Estado de la ejecución en curso del caso de uso
    private Empleado empleadoLogueado;
    private ComisionMedica cmLogueada;
    private Integer numeroBolsinSeleccionado;
    private Integer opcionSeleccionada;
    private List<Integer> documentacionSeleccionada = new ArrayList<>();
    private String areaDestino;
    private LocalDateTime fechaHoraActual;
    private Estado estadoRecibidoEnCMDestino;
    private Estado estadoRecibidoYAceptado;
    private Estado estadoRecibidoYAceptadoParcial;

    public GestorRecepcionBolsin(SesionRepository sesionRepository, BolsinRepository bolsinRepository,
                                 EstadoRepository estadoRepository, InterfazEmail interfazEmail) {
        this.sesionRepository = sesionRepository;
        this.bolsinRepository = bolsinRepository;
        this.estadoRepository = estadoRepository;
        this.interfazEmail = interfazEmail;
    }

    // ------------------------------------------------------------------ Pasos 1 a 4

    // DEFENSA (2. Recorrido): inicio del CU. Busca la CM del empleado logueado y los bolsines
    // ENVIADOS a esa CM. Al bolsín se le pregunta (esTuCMDestino, sosEnviado): Experto.
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
        this.opcionSeleccionada = null;

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

    /** La opción 4 solo se habilita si la CM destino del bolsín es la Comisión Médica Central. */
    private List<OpcionRecepcionResponse> buscarOpcionesRecepcion(Bolsin bolsin) {
        List<OpcionRecepcionResponse> opciones = new ArrayList<>();
        opciones.add(new OpcionRecepcionResponse(OPCION_CONTENIDO_IGUAL,
                obtenerDescripcionOpcion(OPCION_CONTENIDO_IGUAL), false, false, true));
        opciones.add(new OpcionRecepcionResponse(OPCION_NO_SE_RECIBE_TODA,
                obtenerDescripcionOpcion(OPCION_NO_SE_RECIBE_TODA), true, false, true));
        opciones.add(new OpcionRecepcionResponse(OPCION_NO_CORRESPONDE_AL_DESTINO,
                obtenerDescripcionOpcion(OPCION_NO_CORRESPONDE_AL_DESTINO), true, false, true));
        opciones.add(new OpcionRecepcionResponse(OPCION_REDIRIGIR_A_OTRA_AREA,
                obtenerDescripcionOpcion(OPCION_REDIRIGIR_A_OTRA_AREA), true, true, bolsin.tuDestinoEsCentral()));
        return opciones;
    }

    private String obtenerDescripcionOpcion(int opcion) {
        return switch (opcion) {
            case OPCION_CONTENIDO_IGUAL -> "El contenido del bolsín es igual al registrado";
            case OPCION_NO_SE_RECIBE_TODA ->
                    "No se recibe toda la documentación asociada a los remitos que contiene el bolsín";
            case OPCION_NO_CORRESPONDE_AL_DESTINO -> "Existe documentación que no corresponde al destino";
            case OPCION_REDIRIGIR_A_OTRA_AREA -> "La documentación se debe redirigir a otra área";
            default -> throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "Debe seleccionar una opción de recepción válida.");
        };
    }

    // ------------------------------------------------------------------ Pasos 8 y 9

    @Transactional(readOnly = true)
    public PedidoConfirmacionResponse tomarSeleccionOpcion(Integer opcion, List<Integer> numerosDocumentacion,
                                                           String areaDestino) {
        Bolsin bolsin = obtenerBolsinSeleccionado();
        if (opcion == null) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "Debe seleccionar una opción de recepción válida.");
        }
        String descripcion = obtenerDescripcionOpcion(opcion);
        List<Integer> documentacion = numerosDocumentacion == null ? List.of() : List.copyOf(numerosDocumentacion);

        if (opcion == OPCION_REDIRIGIR_A_OTRA_AREA && !bolsin.tuDestinoEsCentral()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "La opción solo puede usarse cuando la CM destino es la Comisión Médica Central.");
        }
        if (opcion == OPCION_CONTENIDO_IGUAL) {
            documentacion = List.of();
        } else {
            if (documentacion.isEmpty()) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                        "Debe indicar al menos una documentación para la opción seleccionada.");
            }
            if (!bolsin.contieneDocumentaciones(documentacion)) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                        "La documentación indicada no pertenece al bolsín seleccionado.");
            }
        }
        String area = null;
        if (opcion == OPCION_REDIRIGIR_A_OTRA_AREA) {
            if (areaDestino == null || areaDestino.isBlank()) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                        "Debe indicar el área de la sede central a la que se redirige la documentación.");
            }
            area = areaDestino.trim();
        }

        this.opcionSeleccionada = opcion;
        this.documentacionSeleccionada = documentacion;
        this.areaDestino = area;

        return new PedidoConfirmacionResponse(opcion, descripcion, bolsin.getNumeroBolsin(),
                documentacion, area,
                "¿Confirma el registro de la recepción del bolsín N° " + bolsin.getNumeroBolsin() + "?");
    }

    // ------------------------------------------------------------------ Pasos 10 a 12

    // DEFENSA (2. Recorrido): el EB confirma. Recién acá se guarda algo; los pasos anteriores solo
    // consultan y recuerdan. Si no confirma (A6) se descarta la opción. Si confirma, se siguen los
    // mismos pasos que en la realización de análisis: buscarEstado -> actualizarEstadoBolsin ->
    // actualizarEstadoRemito -> actualizarEstadoDocumentacion (acá empieza el State) ->
    // enviarNotificacion -> finCU. La pantalla después informa el registro exitoso.
    @Transactional
    public ResultadoRecepcionResponse tomarConfirmacion(boolean confirma) {
        Bolsin bolsin = obtenerBolsinSeleccionado();
        if (opcionSeleccionada == null) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Primero debe seleccionar una opción de recepción.");
        }
        if (!confirma) {
            // A6: el EB no confirma. Se descarta la opción y vuelve a elegir.
            this.opcionSeleccionada = null;
            this.documentacionSeleccionada = new ArrayList<>();
            this.areaDestino = null;
            return new ResultadoRecepcionResponse(false,
                    "No se registró la recepción. Puede seleccionar nuevamente la opción de recepción.",
                    bolsin.getNumeroBolsin(), bolsin.getNombreEstadoActual(), null, null, List.of(), List.of());
        }

        obtenerFechaYHora();
        buscarEstado();
        actualizarEstadoBolsin(bolsin);
        actualizarEstadoRemito(bolsin);
        actualizarEstadoDocumentacion(bolsin);
        bolsinRepository.save(bolsin);
        List<NotificacionResponse> notificaciones = enviarNotificacion(bolsin);

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

    /**
     * Busca, entre los estados parametrizados, los que se van a asignar: Recibido en CM destino
     * (ámbito Bolsín), Recibido y Aceptado y Recibido y Aceptado Parcial (ámbito Remito).
     */
    private void buscarEstado() {
        this.estadoRecibidoEnCMDestino = null;
        this.estadoRecibidoYAceptado = null;
        this.estadoRecibidoYAceptadoParcial = null;
        for (Estado estado : estadoRepository.findAll()) {
            if (estado.esAmbitoBolsin() && estado.esRecibidoEnCMDestino()) {
                this.estadoRecibidoEnCMDestino = estado;
            }
            if (estado.esAmbitoRemito() && estado.esRecibidoYAceptado()) {
                this.estadoRecibidoYAceptado = estado;
            }
            if (estado.esAmbitoRemito() && estado.esRecibidoYAceptadoParcial()) {
                this.estadoRecibidoYAceptadoParcial = estado;
            }
        }
        if (estadoRecibidoEnCMDestino == null || estadoRecibidoYAceptado == null
                || estadoRecibidoYAceptadoParcial == null) {
            throw new IllegalStateException("No se encuentran parametrizados los estados de recepción.");
        }
    }

    /** El bolsín pasa a Recibido en CM destino (Bolsín no usa el patrón State). */
    private void actualizarEstadoBolsin(Bolsin bolsin) {
        bolsin.actualizarEstado(estadoRecibidoEnCMDestino, empleadoLogueado, fechaHoraActual);
    }

    /**
     * Cada remito pasa a Recibido y Aceptado, salvo que contenga documentación marcada como no
     * recibida (opción 2) o que no corresponde al destino (opción 3): en ese caso queda
     * Recibido y Aceptado Parcial.
     */
    // DEFENSA (2. Recorrido): loop "Actualizar estado de cada remito [mientras haya remitos en el bolsín]".
    private void actualizarEstadoRemito(Bolsin bolsin) {
        for (Remito remito : bolsin.getRemitos()) {
            if (esRecepcionParcial(remito)) {
                remito.actualizarEstado(estadoRecibidoYAceptadoParcial);
            } else {
                remito.actualizarEstado(estadoRecibidoYAceptado);
            }
        }
    }

    private boolean esRecepcionParcial(Remito remito) {
        boolean opcionParcial = opcionSeleccionada == OPCION_NO_SE_RECIBE_TODA
                || opcionSeleccionada == OPCION_NO_CORRESPONDE_AL_DESTINO;
        return opcionParcial && remito.contieneAlgunaDocumentacion(documentacionSeleccionada);
    }

    /**
     * Acá empieza el patrón State: a cada documentación se le envía el evento que corresponde a la
     * opción elegida (aceptar, noRecibir, rechazar o redirigir). El gestor no sabe a qué estado pasa:
     * lo resuelve el estado actual de la documentación.
     */
    // DEFENSA (3. Dónde aparece el patrón): loop "Actualizar estado de cada documentación
    // [mientras haya documentación en el remito]". El gestor manda el EVENTO; el estado concreto
    // (EnBolsinEnviado) es el que crea el estado siguiente y cambia el estado de la documentación.
    private void actualizarEstadoDocumentacion(Bolsin bolsin) {
        for (Remito remito : bolsin.getRemitos()) {
            for (DetalleRemito detalle : remito.getDetalleRemito()) {
                Documentacion documentacion = detalle.getDocumentacion();
                if (!detalle.esDeAlgunaDocumentacion(documentacionSeleccionada)) {
                    documentacion.aceptar(empleadoLogueado, fechaHoraActual);
                } else if (opcionSeleccionada == OPCION_NO_SE_RECIBE_TODA) {
                    documentacion.noRecibir(empleadoLogueado, fechaHoraActual);
                } else if (opcionSeleccionada == OPCION_NO_CORRESPONDE_AL_DESTINO) {
                    documentacion.rechazar(empleadoLogueado, fechaHoraActual);
                } else if (opcionSeleccionada == OPCION_REDIRIGIR_A_OTRA_AREA) {
                    documentacion.redirigir(empleadoLogueado, fechaHoraActual);
                    detalle.setAreaCMCDestino(areaDestino);
                }
            }
        }
    }

    /**
     * Inclusión del CU 29 Notificar recepción de bolsín: un e-mail por cada CM de origen
     * que envió documentación, con la situación de cada documentación recibida.
     */
    private List<NotificacionResponse> enviarNotificacion(Bolsin bolsin) {
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
            String correo = entrada.getKey().getCorreo();
            interfazEmail.enviarCorreo(correo, asunto, cuerpo);
            notificaciones.add(new NotificacionResponse(
                    entrada.getKey().getNombre() + " <" + correo + ">", asunto, cuerpo));
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
        this.opcionSeleccionada = null;
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
