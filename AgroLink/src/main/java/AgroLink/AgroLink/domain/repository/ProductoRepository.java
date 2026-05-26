package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}