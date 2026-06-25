package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.*;
import AgroLink.AgroLink.domain.repository.PedidoRepository;
import AgroLink.AgroLink.persistance.entity.Comprador;
import AgroLink.AgroLink.persistance.entity.Usuario;
import AgroLink.AgroLink.domain.repository.CompradorRepository;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import AgroLink.AgroLink.domain.dto.PedidoResponseDTO;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CompradorService {

    private final UsuarioRepository usuarioRepository;
    private final CompradorRepository compradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final PedidoRepository pedidoRepository;

    // Sección 1 — Datos Personales
    public void actualizarDatosPersonales(String email, DatosPersonalesCompradorRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombres(request.getNombres());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setFotoPerfil(request.getFotoPerfil());

        usuarioRepository.save(usuario);
    }

    // Sección 2 — Cambiar Password
    public void cambiarPassword(String email, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }

    // Sección 3 — Perfil Comercial
    public void actualizarPerfilComercial(String email, PerfilComercialRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Comprador comprador = compradorRepository.findByUsuario(usuario)
                .orElse(new Comprador());

        comprador.setDniRuc(request.getDniRuc());
        comprador.setNombreNegocio(request.getTipoComprador());
        comprador.setTelefono(request.getTelefono());
        comprador.setUbicacion(request.getUbicacion());
        comprador.setDireccion(request.getDireccionEntrega());
        comprador.setUsuario(usuario);

        compradorRepository.save(comprador);
    }

    public PerfilCompradorResponse obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Comprador comprador = compradorRepository.findByUsuario(usuario)
                .orElse(new Comprador());

        return new PerfilCompradorResponse(
                usuario.getNombres(),
                usuario.getApellidoPaterno(),
                usuario.getEmail(),
                usuario.getFotoPerfil(),
                comprador.getDniRuc(),
                comprador.getNombreNegocio(),
                comprador.getTelefono(),
                comprador.getUbicacion(),
                comprador.getDireccion()
        );
    }

    public List<PedidoResponseDTO> obtenerPedidosPorComprador(String email) {
        Comprador comprador = compradorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RuntimeException("Comprador no encontrado: " + email));

        return pedidoRepository.findByComprador(comprador).stream()
                .map(pedido -> new PedidoResponseDTO(
                        pedido.getId(),
                        pedido.getFechaCreacion(),
                        pedido.getEstadoPedido().getDescripcionEstadoPedido(),
                        pedido.getDetalles().stream()
                                .map(d -> new PedidoResponseDTO.DetalleResponseDTO(
                                        d.getCultivo().getId(),
                                        d.getCultivo().getProductoVariedad().getNombreProductosVariedad(),
                                        d.getCantidadSolicitada(),
                                        d.getPrecioPactado(),
                                        d.getDireccion()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}