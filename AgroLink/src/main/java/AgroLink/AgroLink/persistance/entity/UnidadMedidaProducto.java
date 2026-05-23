package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "unidad_medida_producto")
public class UnidadMedidaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad_medida_producto")
    private Long idUnidadMedidaProducto;

    @Column(name = "nombre_unidad_medida_producto", length = 50)
    private String nombreUnidadMedidaProducto;

    // Constructor vacío
    public UnidadMedidaProducto() {}

    // Getters y Setters
    public Long getIdUnidadMedidaProducto() { return idUnidadMedidaProducto; }
    public void setIdUnidadMedidaProducto(Long idUnidadMedidaProducto) { this.idUnidadMedidaProducto = idUnidadMedidaProducto; }

    public String getNombreUnidadMedidaProducto() { return nombreUnidadMedidaProducto; }
    public void setNombreUnidadMedidaProducto(String nombreUnidadMedidaProducto) { this.nombreUnidadMedidaProducto = nombreUnidadMedidaProducto; }
}