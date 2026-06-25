package AgroLink.AgroLink.domain.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DatosPersonalesCompradorRequest {
    private String nombres;
    private String apellidoPaterno;
    private String fechaNacimiento;
    private String fotoPerfil;
}
