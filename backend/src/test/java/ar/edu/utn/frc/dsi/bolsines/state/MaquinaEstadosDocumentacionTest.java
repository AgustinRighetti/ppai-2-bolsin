package ar.edu.utn.frc.dsi.bolsines.state;

import ar.edu.utn.frc.dsi.bolsines.entity.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entity.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de la máquina de estados de Documentación implementada con el patrón State. */
class MaquinaEstadosDocumentacionTest {

    private final LocalDateTime inicio = LocalDateTime.of(2026, 11, 10, 9, 0);
    private Empleado responsable;
    private Documentacion documentacion;

    @BeforeEach
    void crearDocumentacionEnBolsinEnviado() {
        ComisionMedica cordoba = new ComisionMedica("CBA", "Comisión Médica Córdoba", "", "", "", false);
        responsable = new Empleado("Laura", "Gómez", "", cordoba);
        documentacion = new Documentacion(1, "Expediente de prueba", LocalDate.of(2026, 11, 10),
                new TipoDocumento("Expediente", ""), cordoba, responsable, inicio);
        documentacion.asignarARemito(responsable, inicio.plusHours(1));
        documentacion.agregarABolsin(responsable, inicio.plusHours(2));
        documentacion.registrarRetiro(responsable, inicio.plusHours(3));
    }

    @Test
    void llegaAEnBolsinEnviadoPorSusTransiciones() {
        assertInstanceOf(EnBolsinEnviado.class, documentacion.getEstadoActual());
        assertEquals(4, documentacion.getCambiosEstado().size());
    }

    @Test
    void aceptarPasaARecibidaYAceptadaYCierraElCambioAnterior() {
        documentacion.aceptar(responsable, inicio.plusHours(4));

        assertInstanceOf(RecibidaYAceptada.class, documentacion.getEstadoActual());
        assertEquals(5, documentacion.getCambiosEstado().size());
        assertEquals(1, documentacion.getCambiosEstado().stream().filter(c -> c.sosActual()).count());
        assertEquals(inicio.plusHours(4), documentacion.getCambiosEstado().get(3).getFechaHoraFin());
        assertNull(documentacion.obtenerCambioEstadoActual().getFechaHoraFin());
        assertEquals(responsable, documentacion.obtenerCambioEstadoActual().getResponsable());
    }

    @Test
    void cadaOpcionDelCU28LlevaASuEstado() {
        documentacion.noRecibir(responsable, inicio.plusHours(4));
        assertTrue(documentacion.esNoRecibida());

        crearDocumentacionEnBolsinEnviado();
        documentacion.rechazar(responsable, inicio.plusHours(4));
        assertTrue(documentacion.esRecibidaYRechazada());

        crearDocumentacionEnBolsinEnviado();
        documentacion.redirigir(responsable, inicio.plusHours(4));
        assertInstanceOf(ParaRedirigir.class, documentacion.getEstadoActual());
    }

    @Test
    void noRecibidaPuedeVolverARegistrada() {
        documentacion.noRecibir(responsable, inicio.plusHours(4));
        documentacion.retomarRegistro(responsable, inicio.plusDays(1));
        assertInstanceOf(Registrada.class, documentacion.getEstadoActual());
    }

    @Test
    void unaTransicionNoPermitidaLanzaExcepcionYNoCambiaElEstado() {
        documentacion.aceptar(responsable, inicio.plusHours(4));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> documentacion.rechazar(responsable, inicio.plusHours(5)));
        assertEquals("No se puede rechazar una documentación en estado Recibida y aceptada.", error.getMessage());
        assertInstanceOf(RecibidaYAceptada.class, documentacion.getEstadoActual());
        assertEquals(5, documentacion.getCambiosEstado().size());
    }

    @Test
    void elEstadoSeReconstruyeDesdeSuNombre() {
        EstadoDocumentacionConverter converter = new EstadoDocumentacionConverter();
        String guardado = converter.convertToDatabaseColumn(new ParaRedirigir());
        assertEquals("ParaRedirigir", guardado);
        assertInstanceOf(ParaRedirigir.class, converter.convertToEntityAttribute(guardado));
    }
}
