package AgroLink.AgroLink.persistance.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "producto_variedad") 
public class Producto_Variedad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_variedad") // Tu PK física
    private Long idProductoVariedad;

    @Column(name = "nombre_producto_variedad", length = 100)
    private String nombreProductoVariedad;

    @Column(name = "precio_producto_variedad")
    private Double precioProductoVariedad;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;


    public Producto_Variedad() {}

    // Getters y Setters
    public Long getIdProductoVariedad() { return idProductoVariedad; }
    public void setIdProductoVariedad(Long idProductoVariedad) { this.idProductoVariedad = idProductoVariedad; }

    public String getNombreProductoVariedad() { return nombreProductoVariedad; }
    public void setNombreProductoVariedad(String nombreProductoVariedad) { this.nombreProductoVariedad = nombreProductoVariedad; }

    public Double getPrecioProductoVariedad() { return precioProductoVariedad; }
    public void setPrecioProductoVariedad(Double precioProductoVariedad) { this.precioProductoVariedad = precioProductoVariedad; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
}
