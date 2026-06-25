package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.CambiarPasswordRequest;
import AgroLink.AgroLink.domain.dto.DatosPersonalesRequest;
import AgroLink.AgroLink.domain.dto.PerfilAgricolaRequest;
import AgroLink.AgroLink.domain.dto.PerfilAgricultorResponse;
import AgroLink.AgroLink.domain.dto.VentaAgricultorDTO;
import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Usuario;
import AgroLink.AgroLink.domain.repository.AgricultorRepository;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import AgroLink.AgroLink.domain.repository.PedidoRepository;
import AgroLink.AgroLink.persistance.entity.Pedido;
import AgroLink.AgroLink.persistance.entity.DetallePedido;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgricultorService {

    private final UsuarioRepository usuarioRepository;
    private final AgricultorRepository agricultorRepository;
    private final PasswordEncoder passwordEncoder;
    private final PedidoRepository pedidoRepository;

    // Sección 1 — Datos Personales
    public void actualizarDatosPersonales(String email, DatosPersonalesRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombres(request.getNombres());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setEdad(request.getEdad());
        usuario.setFotoPerfil(request.getFotoPerfil());

        usuarioRepository.save(usuario);
    }

    // Sección 2 — Cambiar Password
    public void cambiarPassword(String email, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Verificar que nueva password y confirmar coincidan
        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }

    // Sección 3 — Perfil Agrícola
    public void actualizarPerfilAgricola(String email, PerfilAgricolaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Buscar si ya tiene perfil agrícola o crear uno nuevo
        Agricultor agricultor = agricultorRepository.findByUsuario(usuario)
                .orElse(new Agricultor());

        agricultor.setDescripcion(request.getDescripcion());
        agricultor.setDniRuc(request.getDniRuc());
        agricultor.setUbicacion(request.getUbicacion());
        agricultor.setHectareasTotales(request.getHectareasTotales());
        agricultor.setAnosExperiencia(request.getAnosExperiencia());
        agricultor.setCertificaciones(request.getCertificaciones());
        agricultor.setUsuario(usuario);

        agricultorRepository.save(agricultor);
    }

    public PerfilAgricultorResponse obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Agricultor agricultor = agricultorRepository.findByUsuario(usuario)
                .orElse(new Agricultor());

        return new PerfilAgricultorResponse(
                usuario.getNombres(),
                usuario.getApellidoPaterno(),
                usuario.getApellidoMaterno(),
                usuario.getEdad(),
                usuario.getEmail(),
                usuario.getFotoPerfil(),
                agricultor.getDescripcion(),
                agricultor.getDniRuc(),
                agricultor.getUbicacion(),
                agricultor.getHectareasTotales() != null ?
                        agricultor.getHectareasTotales().doubleValue() : null,
                agricultor.getAnosExperiencia(),
                agricultor.getCertificaciones()
        );
    }

    // Sección 4 — Venta de Productos
    @Transactional
    public List<VentaAgricultorDTO> obtenerVentas(String email) {
        Agricultor agricultor = agricultorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RuntimeException("Agricultor no encontrado"));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<VentaAgricultorDTO> ventas = new ArrayList<>();

        for (Pedido pedido : pedidoRepository.findAll()) {
            for (DetallePedido d : pedido.getDetalles()) {
                if (!d.getCultivo().getAgricultor().getId().equals(agricultor.getId())) continue;

                BigDecimal total = d.getCantidadSolicitada().multiply(d.getPrecioPactado());
                String nombreComprador = pedido.getComprador().getUsuario().getNombres()
                        + " " + pedido.getComprador().getUsuario().getApellidoPaterno();
                String empresa = pedido.getComprador().getNombreNegocio() != null
                        ? pedido.getComprador().getNombreNegocio() : "—";

                ventas.add(new VentaAgricultorDTO(
                        pedido.getId(),
                        d.getCultivo().getProductoVariedad().getProducto() != null   // ← nuevo
                                ? d.getCultivo().getProductoVariedad().getProducto().getNombre()
                                : "—",
                        d.getCultivo().getProductoVariedad().getNombreProductosVariedad(),
                        nombreComprador,
                        empresa,
                        d.getCantidadSolicitada(),
                        d.getCultivo().getUnidad(),
                        "S/ " + String.format("%.2f", total),
                        pedido.getFechaCreacion().format(fmt),
                        d.getCultivo().getLote(),
                        pedido.getEstadoPedido().getDescripcionEstadoPedido(),
                        d.getDireccion()
                ));
            }
        }
        return ventas;
    }
}
