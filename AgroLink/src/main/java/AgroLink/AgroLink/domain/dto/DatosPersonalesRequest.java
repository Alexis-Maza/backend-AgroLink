package AgroLink.AgroLink.domain.dto;

import lombok.Data;

@Data
public class DatosPersonalesRequest {
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private Integer edad;
    private String fotoPerfil;
}