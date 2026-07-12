package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.repository.EstadoCultivoRepository;
import AgroLink.AgroLink.domain.repository.EstadoPedidoRepository;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import AgroLink.AgroLink.persistance.entity.Estado_Cultivo;
import AgroLink.AgroLink.persistance.entity.Estado_Pedido;
import AgroLink.AgroLink.persistance.entity.Rol;
import AgroLink.AgroLink.persistance.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    /** Email del usuario técnico usado como autor de acciones automáticas (ej. cancelaciones por falta de stock). */
    public static final String EMAIL_USUARIO_SISTEMA = "sistema@agrolink.app";

    private final EstadoCultivoRepository estadoCultivoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        sembrarEstadosCultivo();
        sembrarEstadosPedido();
        sembrarUsuarioSistema();
    }

    private void sembrarEstadosCultivo() {
        List<String> estadosRequeridos = List.of(
                "Recién cultivado",
                "En crecimiento",
                "Listo para cosechar"
        );

        for (String desc : estadosRequeridos) {
            if (estadoCultivoRepository.findByDescripcionEstadoCultivo(desc).isEmpty()) {
                Estado_Cultivo nuevoEstado = new Estado_Cultivo();
                nuevoEstado.setDescripcionEstadoCultivo(desc);
                estadoCultivoRepository.save(nuevoEstado);
                System.out.println("[DatabaseSeeder] Estado de cultivo '" + desc + "' creado.");
            }
        }
    }

    private void sembrarEstadosPedido() {
        List<String> estadosRequeridos = List.of(
                "Pendiente",
                "En preparación",
                "Enviado",
                "Entregado",
                "Cancelado"
        );

        for (String desc : estadosRequeridos) {
            if (estadoPedidoRepository.findByDescripcionEstadoPedidoIgnoreCase(desc).isEmpty()) {
                Estado_Pedido nuevoEstado = new Estado_Pedido();
                nuevoEstado.setDescripcionEstadoPedido(desc);
                estadoPedidoRepository.save(nuevoEstado);
                System.out.println("[DatabaseSeeder] Estado de pedido '" + desc + "' creado.");
            }
        }
    }

    private void sembrarUsuarioSistema() {
        if (usuarioRepository.findByEmail(EMAIL_USUARIO_SISTEMA).isPresent()) {
            return;
        }

        Usuario sistema = new Usuario();
        sistema.setNombres("Sistema");
        sistema.setApellidoPaterno("AgroLink");
        sistema.setEmail(EMAIL_USUARIO_SISTEMA);
        sistema.setRol(Rol.SISTEMA);
        sistema.setVerificado(true);
        usuarioRepository.save(sistema);
        System.out.println("[DatabaseSeeder] Usuario sistema creado.");
    }
}
