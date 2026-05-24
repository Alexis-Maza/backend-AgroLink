package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;

@Entity
@Table(name = "estado_cultivo")

public class Estado_Cultivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_cultivo") // Tu PK física
    private Long idEstadoCultivo;

    @Column(name = "descripcion_estado_cultivo", length = 50)
    private String descripcionEstadoCultivo;
    public Estado_Cultivo() {
    }

    public Long getIdEstadoCultivo() {
        return idEstadoCultivo;
    }

    public void setIdEstadoCultivo(Long idEstadoCultivo) {
        this.idEstadoCultivo = idEstadoCultivo;
    }

    public String getDescripcionEstadoCultivo() {
        return descripcionEstadoCultivo;
    }

    public void setDescripcionEstadoCultivo(String descripcionEstadoCultivo) {
        this.descripcionEstadoCultivo = descripcionEstadoCultivo;
    }
}
