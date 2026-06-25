package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "productos_variedad")
public class Producto_Variedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_productos_variedad", length = 100, nullable = false)
    private String nombreProductosVariedad;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    // Relación con el producto base (ej. "Papa" tiene variedad "Yungay")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto")
    private Producto producto;
}
