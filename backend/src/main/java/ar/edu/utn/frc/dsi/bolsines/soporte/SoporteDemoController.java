package ar.edu.utn.frc.dsi.bolsines.soporte;

import ar.edu.utn.frc.dsi.bolsines.control.GestorRecepcionBolsin;
import ar.edu.utn.frc.dsi.bolsines.control.ReglaNegocioException;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.CambiarUsuarioRequest;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.MensajeResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.Dtos.UsuarioResponse;
import ar.edu.utn.frc.dsi.bolsines.entity.Sesion;
import ar.edu.utn.frc.dsi.bolsines.entity.Usuario;
import ar.edu.utn.frc.dsi.bolsines.repository.SesionRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Soporte para la demostración (fuera del CU 28): simula el inicio de sesión de otro
 * Encargado de Bolsines y permite volver a cargar los datos de prueba.
 */
@RestController
@RequestMapping("/api/demo")
public class SoporteDemoController {

    private final UsuarioRepository usuarios;
    private final SesionRepository sesiones;
    private final DatosIniciales datosIniciales;
    private final GestorRecepcionBolsin gestor;

    public SoporteDemoController(UsuarioRepository usuarios, SesionRepository sesiones,
                                 DatosIniciales datosIniciales, GestorRecepcionBolsin gestor) {
        this.usuarios = usuarios;
        this.sesiones = sesiones;
        this.datosIniciales = datosIniciales;
        this.gestor = gestor;
    }

    @GetMapping("/usuarios")
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        String activo = sesiones.findAll().stream().filter(Sesion::esActual)
                .map(s -> s.getUsuario().getNombre()).findFirst().orElse("");
        return usuarios.findAll().stream()
                .map(u -> new UsuarioResponse(u.getNombre(), u.getEmpleado().getNombreCompleto(),
                        u.getEmpleado().getCM().getNombre(), u.esTuUsuario(activo)))
                .toList();
    }

    @PostMapping("/cambiar-usuario")
    @Transactional
    public MensajeResponse cambiarUsuario(@RequestBody CambiarUsuarioRequest request) {
        Usuario usuario = usuarios.findAll().stream()
                .filter(u -> u.esTuUsuario(request.usuario()))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "No existe el usuario indicado."));
        LocalDateTime ahora = LocalDateTime.now();
        sesiones.findAll().stream().filter(Sesion::esActual).forEach(s -> s.cerrar(ahora));
        sesiones.save(new Sesion(ahora, usuario));
        gestor.cancelarCU();
        return new MensajeResponse("Sesión iniciada como " + usuario.getNombre());
    }

    @PostMapping("/reiniciar-datos")
    public MensajeResponse reiniciarDatos() {
        gestor.cancelarCU();
        datosIniciales.reiniciar();
        return new MensajeResponse("Se volvieron a cargar los datos de prueba.");
    }
}
