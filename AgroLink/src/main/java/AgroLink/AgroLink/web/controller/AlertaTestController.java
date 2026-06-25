package AgroLink.AgroLink.web.controller;


import AgroLink.AgroLink.domain.service.AlertaStockCosechaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agricultor/alertas")
@RequiredArgsConstructor
public class AlertaTestController {

    private final AlertaStockCosechaService alertaService;

    @PostMapping("/ejecutar")
    public ResponseEntity<String> ejecutar() {
        alertaService.verificarAlertas();
        return ResponseEntity.ok("Alertas ejecutadas manualmente");
    }
}
