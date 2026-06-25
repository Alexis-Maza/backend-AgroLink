package AgroLink.AgroLink.domain.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PerfilAgricolaRequest {
    private String descripcion;
    private String dniRuc;
    private String ubicacion;
    private BigDecimal hectareasTotales;
    private Integer anosExperiencia;
    private String certificaciones;
}