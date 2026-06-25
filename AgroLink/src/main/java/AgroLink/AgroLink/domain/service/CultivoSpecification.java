package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.persistance.entity.Cultivo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CultivoSpecification {

    public static Specification<Cultivo> filtrarCatalogo(
            String search, String region, Double precioMax, Long productoId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Solo cultivos disponibles con stock
            predicates.add(cb.isTrue(root.get("disponible")));
            predicates.add(cb.greaterThan(
                root.get("cantidadDisponible"), BigDecimal.ZERO
            ));

            // Filtro por nombre de producto/variedad
            if (search != null && !search.trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("productoVariedad")
                        .get("nombreProductosVariedad")),
                    "%" + search.trim().toLowerCase() + "%"
                ));
            }

            // Filtro por región del agricultor
            if (region != null && !region.trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("agricultor").get("ubicacion")),
                    "%" + region.trim().toLowerCase() + "%"
                ));
            }

            // Filtro por precio máximo
            if (precioMax != null && precioMax > 0) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("precio"),
                    BigDecimal.valueOf(precioMax)
                ));
            }

            // Filtro por tipo de producto
            if (productoId != null) {
                predicates.add(cb.equal(
                    root.get("productoVariedad").get("producto").get("id"),
                    productoId
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}