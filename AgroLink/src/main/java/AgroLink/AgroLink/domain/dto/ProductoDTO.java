package AgroLink.AgroLink.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private int cantidadVariantes;
    private boolean activo;
    private List<ProductoVariedadDTO> variedades;
}
