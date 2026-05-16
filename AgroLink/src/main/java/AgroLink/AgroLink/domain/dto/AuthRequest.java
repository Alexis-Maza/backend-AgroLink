package AgroLink.AgroLink.domain.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String nombre;
    private String email;
    private String password;
    private String rol;
}
