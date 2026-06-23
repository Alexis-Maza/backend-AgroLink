package AgroLink.AgroLink.domain.repository.projection;

public interface UsuarioAdminView {
    Long getId();
    String getNombres();
    String getApellidoPaterno();
    String getApellidoMaterno();
    String getEmail();
    Boolean getVerificado();
}