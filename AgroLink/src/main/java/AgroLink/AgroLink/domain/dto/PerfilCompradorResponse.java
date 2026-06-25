package AgroLink.AgroLink.domain.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class PerfilCompradorResponse {
    // Datos personales
    private String nombres;
    private String apellidoPaterno;
    private String email;
    private String fotoPerfil;

    // Perfil comercial
    private String dniRuc;
    private String tipoComprador;
    private String telefono;
    private String ubicacion;
    private String direccionEntrega;
}
