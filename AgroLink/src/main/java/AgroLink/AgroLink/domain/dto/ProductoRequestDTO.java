package AgroLink.AgroLink.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    private String descripcion;
}