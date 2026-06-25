package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "unidad_medida_producto")
public class UnidadMedidaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad_medida_producto")
    private Long id;

    @Column(name = "nombre_unidad_medida_producto", length = 50)
    private String nombreUnidadMedidaProducto;
}