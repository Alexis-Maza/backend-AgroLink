package AgroLink.AgroLink.domain.dto;

import lombok.Data;

@Data
public class PerfilComercialRequest {
    private String dniRuc;
    private String tipoComprador;
    private String telefono;
    private String ubicacion;
    private String direccionEntrega;
}
