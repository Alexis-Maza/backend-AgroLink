package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.repository.EstadoCultivoRepository;
import AgroLink.AgroLink.persistance.entity.Estado_Cultivo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final EstadoCultivoRepository estadoCultivoRepository;

    @Override
    public void run(String... args) throws Exception {
        List<String> estadosRequeridos = List.of(
                "Recién cultivado",
                "En crecimiento",
                "Listo para cosechar"
        );

        for (String desc : estadosRequeridos) {
            if (estadoCultivoRepository.findByDescripcionEstadoCultivo(desc).isEmpty()) {
                Estado_Cultivo nuevoEstado = new Estado_Cultivo();
                nuevoEstado.setDescripcionEstadoCultivo(desc);
                estadoCultivoRepository.save(nuevoEstado);
                System.out.println("[DatabaseSeeder] Estado de cultivo '" + desc + "' creado.");
            }
        }
    }
}
