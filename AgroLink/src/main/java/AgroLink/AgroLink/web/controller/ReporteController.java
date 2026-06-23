package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // Agricultor descarga su catálogo histórico de cultivos
    @GetMapping("/mis-cultivos/excel")
    public ResponseEntity<byte[]> exportMisCultivosExcel(
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] content = reporteService.exportCultivosToExcel(userDetails.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=catalogo_cultivos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    // Agricultor descarga su reporte de ventas (pedidos sobre sus cultivos)
    @GetMapping("/mis-ventas/excel")
    public ResponseEntity<byte[]> exportMisVentasExcel(
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] content = reporteService.exportVentasToExcel(userDetails.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mis_ventas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    // Comprador descarga su historial de compras
    @GetMapping("/mis-compras/excel")
    public ResponseEntity<byte[]> exportMisComprasExcel(
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] content = reporteService.exportPedidosToExcel(userDetails.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mis_compras.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
}
