package ar.edu.utn.frc.dsi.bolsines.soporte;

import ar.edu.utn.frc.dsi.bolsines.entity.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entity.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entity.Estado;
import ar.edu.utn.frc.dsi.bolsines.entity.Remito;
import ar.edu.utn.frc.dsi.bolsines.entity.Sesion;
import ar.edu.utn.frc.dsi.bolsines.entity.TipoDocumento;
import ar.edu.utn.frc.dsi.bolsines.entity.Usuario;
import ar.edu.utn.frc.dsi.bolsines.repository.BolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.ComisionMedicaRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.DocumentacionRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.EstadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.SesionRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.TipoDocumentoRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Datos de prueba para la demostración. Se cargan solo si la base está vacía;
 * no forman parte de la realización del caso de uso.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final EstadoRepository estados;
    private final ComisionMedicaRepository comisiones;
    private final TipoDocumentoRepository tipos;
    private final EmpleadoRepository empleados;
    private final UsuarioRepository usuarios;
    private final SesionRepository sesiones;
    private final DocumentacionRepository documentaciones;
    private final BolsinRepository bolsines;

    private final Map<String, Estado> estadoPorClave = new HashMap<>();
    private int numeroDocumentacion;
    private int numeroRemito;

    public DatosIniciales(EstadoRepository estados, ComisionMedicaRepository comisiones, TipoDocumentoRepository tipos,
                          EmpleadoRepository empleados, UsuarioRepository usuarios, SesionRepository sesiones,
                          DocumentacionRepository documentaciones, BolsinRepository bolsines) {
        this.estados = estados;
        this.comisiones = comisiones;
        this.tipos = tipos;
        this.empleados = empleados;
        this.usuarios = usuarios;
        this.sesiones = sesiones;
        this.documentaciones = documentaciones;
        this.bolsines = bolsines;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (estados.count() == 0) {
            cargar();
        }
    }

    @Transactional
    public void reiniciar() {
        sesiones.deleteAll();
        bolsines.deleteAll();
        documentaciones.deleteAll();
        usuarios.deleteAll();
        empleados.deleteAll();
        tipos.deleteAll();
        comisiones.deleteAll();
        estados.deleteAll();
        estados.flush();
        cargar();
    }

    private void cargar() {
        estadoPorClave.clear();
        numeroDocumentacion = 1000;
        numeroRemito = 500;

        // Estados
        estado(Estado.AMBITO_BOLSIN, Estado.CREADO, "Creado");
        estado(Estado.AMBITO_BOLSIN, Estado.CERRADO, "Cerrado");
        estado(Estado.AMBITO_BOLSIN, Estado.ENVIADO, "Enviado");
        estado(Estado.AMBITO_BOLSIN, Estado.RECIBIDO_EN_CM_DESTINO, "Recibido en CM destino");
        estado(Estado.AMBITO_BOLSIN, "DeBaja", "De baja");
        estado(Estado.AMBITO_REMITO, Estado.CREADO, "Creado");
        estado(Estado.AMBITO_REMITO, "EnBolsinSaliente", "En bolsín saliente");
        estado(Estado.AMBITO_REMITO, Estado.EN_BOLSIN_ENVIADO, "En bolsín enviado");
        estado(Estado.AMBITO_REMITO, Estado.RECIBIDO_Y_ACEPTADO, "Recibido y aceptado");
        estado(Estado.AMBITO_REMITO, Estado.RECIBIDO_Y_ACEPTADO_PARCIAL, "Recibido y aceptado parcial");

        // Comisiones médicas
        ComisionMedica central = comisiones.save(new ComisionMedica("CMC", "Comisión Médica Central",
                "Av. Paseo Colón 1250, C.A.B.A.", "cmc@organismo.gob.ar", "011 4321-0000", true));
        ComisionMedica cordoba = comisiones.save(new ComisionMedica("CBA", "Comisión Médica Córdoba",
                "Av. Colón 345, Córdoba", "cm.cordoba@organismo.gob.ar", "0351 422-1000", false));
        ComisionMedica rosario = comisiones.save(new ComisionMedica("ROS", "Comisión Médica Rosario",
                "San Lorenzo 1055, Rosario", "cm.rosario@organismo.gob.ar", "0341 448-2000", false));
        ComisionMedica mendoza = comisiones.save(new ComisionMedica("MZA", "Comisión Médica Mendoza",
                "San Martín 780, Mendoza", "cm.mendoza@organismo.gob.ar", "0261 429-3000", false));
        ComisionMedica villaMaria = comisiones.save(new ComisionMedica("VMA", "Comisión Médica Villa María",
                "Mendoza 1100, Villa María", "cm.villamaria@organismo.gob.ar", "0353 453-4000", false));

        // Tipos de documento
        TipoDocumento expediente = tipos.save(new TipoDocumento("Expediente", "Expediente administrativo"));
        TipoDocumento dictamen = tipos.save(new TipoDocumento("Dictamen", "Dictamen médico"));
        TipoDocumento estudio = tipos.save(new TipoDocumento("Estudio médico", "Estudio médico complementario"));
        TipoDocumento carta = tipos.save(new TipoDocumento("Carta documento", "Carta documento"));

        // Empleados y usuarios (Encargados de Bolsines)
        Empleado laura = empleados.save(new Empleado("Laura", "Gómez", "lgomez@organismo.gob.ar", cordoba));
        Empleado martin = empleados.save(new Empleado("Martín", "Pérez", "mperez@organismo.gob.ar", central));
        Empleado rocio = empleados.save(new Empleado("Rocío", "Díaz", "rdiaz@organismo.gob.ar", mendoza));
        Empleado diego = empleados.save(new Empleado("Diego", "Sosa", "dsosa@organismo.gob.ar", rosario));
        Usuario uLaura = usuarios.save(new Usuario("lgomez", "1234", laura));
        usuarios.save(new Usuario("mperez", "1234", martin));
        usuarios.save(new Usuario("rdiaz", "1234", rocio));
        sesiones.save(new Sesion(LocalDateTime.now(), uLaura));

        // Bolsines enviados a CM Córdoba
        Bolsin b1 = bolsin(1, 1250, "100234", rosario, cordoba, Estado.ENVIADO, diego);
        Remito r1 = remito(b1, Estado.EN_BOLSIN_ENVIADO);
        doc(r1, "Expediente 4521/26 - Accidente laboral", expediente, diego);
        doc(r1, "Dictamen médico - Paciente Juárez", dictamen, diego);
        Remito r2 = remito(b1, Estado.EN_BOLSIN_ENVIADO);
        doc(r2, "Radiografías de columna - Paciente Juárez", estudio, diego);
        bolsines.save(b1);

        Bolsin b2 = bolsin(2, 980, "100871", mendoza, cordoba, Estado.ENVIADO, rocio);
        Remito r3 = remito(b2, Estado.EN_BOLSIN_ENVIADO);
        doc(r3, "Expediente 3310/26 - Enfermedad profesional", expediente, rocio);
        doc(r3, "Carta documento - Empleador Viñas del Sol", carta, rocio);
        doc(r3, "Resonancia magnética - Paciente Ortiz", estudio, rocio);
        bolsines.save(b2);

        Bolsin b3 = bolsin(3, 640, "200015", central, cordoba, Estado.ENVIADO, martin);
        Remito r4 = remito(b3, Estado.EN_BOLSIN_ENVIADO);
        doc(r4, "Dictamen de apelación - Expediente 2987/25", dictamen, martin);
        doc(r4, "Expediente 2987/25 - Devolución a CMJ", expediente, martin);
        bolsines.save(b3);

        // Bolsines enviados a la Comisión Médica Central
        Bolsin b4 = bolsin(4, 1530, "300412", cordoba, central, Estado.ENVIADO, laura);
        Remito r5 = remito(b4, Estado.EN_BOLSIN_ENVIADO);
        doc(r5, "Recurso de apelación - Expediente 1876/26", expediente, laura);
        doc(r5, "Liquidación de honorarios periciales", carta, laura);
        doc(r5, "Dictamen médico - Paciente Ferreyra", dictamen, laura);
        bolsines.save(b4);

        Bolsin b5 = bolsin(5, 870, "300977", rosario, central, Estado.ENVIADO, diego);
        Remito r6 = remito(b5, Estado.EN_BOLSIN_ENVIADO);
        doc(r6, "Expediente 5102/26 - Elevación a CMC", expediente, diego);
        Remito r7 = remito(b5, Estado.EN_BOLSIN_ENVIADO);
        doc(r7, "Nota para Gerencia de Asuntos Legales", carta, diego);
        doc(r7, "Electromiograma - Paciente Romero", estudio, diego);
        bolsines.save(b5);

        // Bolsín cerrado (todavía no retirado por el correo): no debe aparecer en el listado
        Bolsin b6 = bolsin(6, 410, "400100", villaMaria, cordoba, Estado.CERRADO, diego);
        Remito r8 = remito(b6, "EnBolsinSaliente");
        doc(r8, "Expediente 6001/26 - Accidente in itinere", expediente, diego);
        bolsines.save(b6);
    }

    private void estado(String ambito, String nombre, String descripcion) {
        estadoPorClave.put(ambito + "|" + nombre, estados.save(new Estado(ambito, nombre, descripcion)));
    }

    private Estado estado(String ambito, String nombre) {
        return estadoPorClave.get(ambito + "|" + nombre);
    }

    private Bolsin bolsin(int numero, int peso, String precinto, ComisionMedica origen, ComisionMedica destino,
                          String estadoActual, Empleado responsable) {
        LocalDateTime creado = LocalDateTime.now().minusDays(4).withNano(0);
        Bolsin bolsin = new Bolsin(numero, creado.toLocalDate(), peso, precinto, origen, destino);
        bolsin.crearCambioEstado(estado(Estado.AMBITO_BOLSIN, Estado.CREADO), responsable, creado);
        bolsin.recibir(estado(Estado.AMBITO_BOLSIN, Estado.CERRADO), responsable, creado.plusHours(3));
        if (Estado.ENVIADO.equals(estadoActual)) {
            bolsin.recibir(estado(Estado.AMBITO_BOLSIN, Estado.ENVIADO), responsable, creado.plusDays(1));
        }
        return bolsin;
    }

    private Remito remito(Bolsin bolsin, String estadoRemito) {
        Remito remito = new Remito(++numeroRemito, bolsin.getFecha(), bolsin.getOrigen(), bolsin.getDestino(),
                estado(Estado.AMBITO_REMITO, estadoRemito));
        bolsin.asociarRemito(remito);
        return remito;
    }

    /**
     * Crea la documentación y la lleva hasta el estado del remito usando las transiciones del
     * patrón State: Registrada -> En remito -> En bolsín saliente -> En bolsín enviado.
     */
    private void doc(Remito remito, String asunto, TipoDocumento tipo, Empleado responsable) {
        LocalDateTime registrada = LocalDateTime.now().minusDays(5).withNano(0);
        Documentacion documentacion = new Documentacion(++numeroDocumentacion, asunto, LocalDate.now().minusDays(5),
                tipo, remito.getOrigen(), responsable, registrada);
        documentacion.asignarARemito(responsable, registrada.plusHours(2));
        documentacion.agregarABolsin(responsable, registrada.plusDays(1));
        if (Estado.EN_BOLSIN_ENVIADO.equals(remito.getNombreEstado())) {
            documentacion.registrarRetiro(responsable, registrada.plusDays(2));
        }
        remito.agregarDocumentacion(documentaciones.save(documentacion));
    }
}
