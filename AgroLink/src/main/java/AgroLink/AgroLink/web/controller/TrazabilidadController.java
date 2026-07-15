package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.TrazabilidadResponse;
import AgroLink.AgroLink.domain.service.TrazabilidadPdfService;
import AgroLink.AgroLink.domain.service.TrazabilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * RF-TRAZABILIDAD: Controlador REST para el módulo de trazabilidad.
 *
 * Expone dos endpoints bajo la misma ruta base:
 *
 *   GET /api/v1/trazabilidad/{id}
 *       Retorna la cadena de trazabilidad completa en formato JSON.
 *
 *   GET /api/v1/trazabilidad/{id}/export
 *       Genera y descarga el certificado de trazabilidad en formato PDF (binario).
 *       El PDF incluye: datos del agricultor, información del cultivo,
 *       línea de tiempo de etapas y tabla de pedidos vinculados.
 *
 * ── Seguridad (doble capa) ──────────────────────────────────────────────────
 *   @PreAuthorize("hasAnyAuthority('AGRICULTOR','COMPRADOR')")
 *   Bloquea la petición antes de que llegue al servicio si el rol del JWT
 *   no es AGRICULTOR ni COMPRADOR. La validación de propiedad del cultivo
 *   (AGRICULTOR solo ve el suyo) se delega a TrazabilidadService.validarAcceso().
 *
 * ── Por qué devolver byte[] y no StreamingResponseBody ──────────────────────
 *   El PDF se genera en memoria con ByteArrayOutputStream (tamaños típicos
 *   < 500 KB para trazabilidades con decenas de etapas). No es necesario
 *   StreamingResponseBody (streaming progresivo) ya que no hay PDFs de múltiples
 *   megabytes. byte[] es más simple y compatible con cualquier proxy/gateway.
 */
@RestController
@RequestMapping("/api/v1/trazabilidad")
@RequiredArgsConstructor
public class TrazabilidadController {

    private final TrazabilidadService    trazabilidadService;
    private final TrazabilidadPdfService trazabilidadPdfService;

    // =========================================================================
    // ENDPOINT JSON
    // =========================================================================

    /**
     * Recupera la cadena de trazabilidad completa en formato JSON.
     *
     * @param id          ID del cultivo a consultar.
     * @param userDetails Usuario autenticado (JWT ya validado por JwtFilter).
     * @return 200 OK con TrazabilidadResponse, o 403/404 según corresponda.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AGRICULTOR', 'COMPRADOR')")
    public ResponseEntity<TrazabilidadResponse> obtenerTrazabilidad(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();

        TrazabilidadResponse response = trazabilidadService.obtenerTrazabilidad(
                id, userDetails.getUsername(), rol);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // ENDPOINT EXPORT PDF
    // =========================================================================

    /**
     * Genera y descarga el certificado de trazabilidad en PDF.
     *
     * Flujo interno:
     *  1. Llama a TrazabilidadService.obtenerTrazabilidad() — mismas 3 queries
     *     con JOIN FETCH, misma validación de acceso por rol.
     *  2. Pasa el TrazabilidadResponse a TrazabilidadPdfService.generarPdf()
     *     que construye el documento iText7 en un ByteArrayOutputStream.
     *  3. Retorna el byte[] con headers HTTP para descarga automática en el browser.
     *
     * Headers de respuesta:
     *  - Content-Type: application/pdf
     *  - Content-Disposition: attachment; filename="trazabilidad_{id}.pdf"
     *    → El navegador descarga el archivo en lugar de abrirlo en una pestaña.
     *    → Para previsualizarlo en una pestaña, el Frontend debe cambiar a "inline".
     *
     * @param id          ID del cultivo cuya trazabilidad se exporta.
     * @param userDetails Usuario autenticado (JWT ya validado por JwtFilter).
     * @return 200 OK con bytes del PDF, o 403/404 si no existe o sin permiso.
     */
    @GetMapping("/{id}/export")
    @PreAuthorize("hasAnyAuthority('AGRICULTOR', 'COMPRADOR')")
    public ResponseEntity<byte[]> exportarTrazabilidadPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();

        // Paso 1: Reutiliza exactamente la misma lógica que el endpoint JSON
        TrazabilidadResponse data = trazabilidadService.obtenerTrazabilidad(
                id, userDetails.getUsername(), rol);

        // Paso 2: Genera el PDF en memoria
        byte[] pdfBytes = trazabilidadPdfService.generarPdf(data);

        // Paso 3: Construye la respuesta HTTP con headers de descarga
        String nombreArchivo = "trazabilidad_cultivo_" + id + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\"")
                .header(HttpHeaders.CACHE_CONTROL,
                        "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}
