package AgroLink.AgroLink.domain.dto;

import lombok.Data;

@Data
public class VerifyEmailRequest {
    private String email;
    private String codigo;
}
