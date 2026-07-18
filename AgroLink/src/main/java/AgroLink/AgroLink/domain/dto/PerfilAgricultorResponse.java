package AgroLink.AgroLink.domain.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class PerfilAgricultorResponse {

    private Long id;

    // Datos personales
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private Integer edad;
    private String email;
    private String fotoPerfil;

    // Perfil agrícola
    private String descripcion;
    private String dniRuc;
    private String ubicacion;
    private Double hectareasTotales;
    private Integer anosExperiencia;
    private String certificaciones;
}
