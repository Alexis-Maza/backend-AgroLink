package AgroLink.AgroLink.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductoVariedadRequestDTO {

    @NotBlank(message = "El nombre de la variante es obligatorio")
    private String nombreProductosVariedad;

    @NotNull(message = "Debe asociar un producto")
    private Long idProducto;
}