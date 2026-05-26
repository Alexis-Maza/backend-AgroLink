package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * RF-A2-05: Define el tiempo estándar estimado (en días) para
 * cada etapa de una variedad de producto específica.
 * Ejemplo: La variedad "Yungay" (papa) tiene etapa "Germinación" de 15 días.
 */
@Data
@Entity
@Table(name = "etapa_productos_variedad")
public class Etapa_Producto_Variedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de la etapa, ej: "Germinación", "Crecimiento", "Floración", "Cosecha"
    @Column(name = "nombre_etapa_productos_variedad", length = 100, nullable = false)
    private String nombreEtapaProductosVariedad;

    // Número de días estimados para completar esta etapa según la variedad
    @Column(name = "dias_duracion_estimada", nullable = false)
    private Integer diasDuracionEstimada;

    // Variedad de producto a la que pertenece esta definición de etapa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_productos_variedad", nullable = false)
    private Producto_Variedad productoVariedad;
}
