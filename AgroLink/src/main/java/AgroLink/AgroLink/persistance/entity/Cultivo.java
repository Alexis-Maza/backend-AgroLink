package AgroLink.AgroLink.persistance.entity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cultivos")
public class Cultivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cultive") 
    private Long idCultivo;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "area_sembrada")
    private Double areaSembrada;

    // Relación con el Agricultor (Muchos cultivos pertenecen a un Agricultor)
    @ManyToOne
    @JoinColumn(name = "id_agricultor", nullable = false)
    private Agricultor agricultor;

    // Relación con el Catálogo de Variedades (Muchos cultivos corresponden a una variedad)
    @ManyToOne
    @JoinColumn(name = "id_producto_variedad", nullable = false)
    private Producto_Variedad productoVariedad;

    // Relación con el Estado del Cultivo 
    @ManyToOne
    @JoinColumn(name = "id_estado_cultivo", nullable = false)
    private Estado_Cultivo estadoCultivo;

    
    public Cultivo() {}

    // Getters y Setters
    public Long getIdCultivo() { return idCultivo; }
    public void setIdCultivo(Long idCultivo) { this.idCultivo = idCultivo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public Double getAreaSembrada() { return areaSembrada; }
    public void setAreaSembrada(Double areaSembrada) { this.areaSembrada = areaSembrada; }

    public Agricultor getAgricultor() { return agricultor; }
    public void setAgricultor(Agricultor agricultor) { this.agricultor = agricultor; }

    public Producto_Variedad getProductoVariedad() { return productoVariedad; }
    public void setProductoVariedad(Producto_Variedad productoVariedad) { this.productoVariedad = productoVariedad; }

    public Estado_Cultivo getEstadoCultivo() { return estadoCultivo; }
    public void setEstadoCultivo(Estado_Cultivo estadoCultivo) { this.estadoCultivo = estadoCultivo; }
}
