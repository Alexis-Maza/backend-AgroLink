package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.domain.repository.projection.UsuarioAdminView;
import AgroLink.AgroLink.persistance.entity.Rol;
import AgroLink.AgroLink.persistance.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetToken(String resetToken);

    List<UsuarioAdminView> findByRol(Rol rol);
    long countByRol(Rol rol);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol AND u.fechaRegistro >= :inicio")
    long countByRolAndFechaRegistroAfter(@Param("rol") Rol rol, @Param("inicio") LocalDateTime inicio);

}
