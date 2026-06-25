package AgroLink.AgroLink.domain.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String email;
    private String password;
    private String rol;
}
