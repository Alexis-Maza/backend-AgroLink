package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.TrazabilidadResponse;
import AgroLink.AgroLink.domain.dto.TrazabilidadResponse.*;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RF-TRAZABILIDAD-PDF: Servicio que convierte un objeto TrazabilidadResponse
 * en un documento PDF binario listo para descargar.
 *
 * ── Librería utilizada: iText 7.2.5 Community (AGPL) ──────────────────────
 *
 * Se eligió iText 7 porque:
 *  1. API fluida de alto nivel (Document API) — permite componer el PDF
 *     con Paragraphs, Tables, Cells sin manipular bytes de PDF directamente.
 *  2. Soporte completo de fuentes embebidas, colores RGB, bordes, padding,
 *     colspan/rowspan y control de saltos de página.
 *  3. Integración trivial en Spring Boot: solo requiere la dependencia Maven,
 *     sin archivos de configuración adicionales.
 *  4. La alternativa Apache PDFBox es más de bajo nivel (no tiene table API),
 *     y Jasper Reports requiere archivos .jrxml + servidor de reportes.
 *
 * ── Estructura del PDF generado ────────────────────────────────────────────
 *
 *  Página 1:
 *   ┌──────────────────────────────────────────────────────┐
 *   │  🌱 AGROLINK — Certificado de Trazabilidad           │  ← PORTADA
 *   │  Fecha de emisión: 12/07/2025                        │
 *   ├──────────────────────────────────────────────────────┤
 *   │  DATOS DEL AGRICULTOR                                │  ← Sección 1
 *   │  Nombre | DNI/RUC | Ubicación | Experiencia ...      │
 *   ├──────────────────────────────────────────────────────┤
 *   │  INFORMACIÓN DEL CULTIVO                             │  ← Sección 2
 *   │  Lote | Producto | Estado | Área | Cantidad ...      │
 *   ├──────────────────────────────────────────────────────┤
 *   │  LÍNEA DE TIEMPO — HISTORIAL DE ETAPAS               │  ← Sección 3
 *   │  ● Etapa 1  [OK] / [⚠ RETRASO 26.7%]               │
 *   │  │                                                   │
 *   │  ● Etapa 2  ACTIVA                                   │
 *   ├──────────────────────────────────────────────────────┤
 *   │  PEDIDOS VINCULADOS                                  │  ← Sección 4
 *   │  Tabla con columnas: Pedido | Comprador | Estado ... │
 *   └──────────────────────────────────────────────────────┘
 *   Pie de página: "Generado por AgroLink" + número de página
 *
 * ── Retorno ────────────────────────────────────────────────────────────────
 *  byte[] → Spring MVC lo serializa directamente en el body de la respuesta.
 *  No se escribe en disco; se usa ByteArrayOutputStream en memoria.
 */
@Slf4j
@Service
public class TrazabilidadPdfService {

    // ── Paleta de colores AgroLink ────────────────────────────────────────
    private static final DeviceRgb COLOR_VERDE_OSCURO  = new DeviceRgb(0x1B, 0x5E, 0x20); // #1B5E20
    private static final DeviceRgb COLOR_VERDE_MEDIO   = new DeviceRgb(0x2E, 0x7D, 0x32); // #2E7D32
    private static final DeviceRgb COLOR_VERDE_CLARO   = new DeviceRgb(0xE8, 0xF5, 0xE9); // #E8F5E9
    private static final DeviceRgb COLOR_VERDE_MENTA   = new DeviceRgb(0xA5, 0xD6, 0xA7); // #A5D6A7
    private static final DeviceRgb COLOR_NARANJA_ALERTA = new DeviceRgb(0xFF, 0x6F, 0x00); // #FF6F00
    private static final DeviceRgb COLOR_ROJO_ALERTA   = new DeviceRgb(0xC6, 0x28, 0x28); // #C62828
    private static final DeviceRgb COLOR_GRIS_TEXTO    = new DeviceRgb(0x42, 0x42, 0x42); // #424242
    private static final DeviceRgb COLOR_GRIS_CLARO    = new DeviceRgb(0xF5, 0xF5, 0xF5); // #F5F5F5
    private static final DeviceRgb COLOR_BLANCO        = new DeviceRgb(0xFF, 0xFF, 0xFF);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Márgenes del documento (puntos tipográficos: 1 pt = 1/72 pulgada) ──
    private static final float MARGEN = 40f;

    /**
     * Genera el PDF de trazabilidad completo en memoria.
     *
     * @param data Objeto TrazabilidadResponse ya construido por TrazabilidadService.
     * @return Array de bytes con el contenido binario del PDF.
     * @throws RuntimeException si iText falla al escribir el documento.
     */
    public byte[] generarPdf(TrazabilidadResponse data) {
        log.info("Generando PDF de trazabilidad para cultivo id={}",
                data.getCultivo().getIdCultivo());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer   = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document    doc    = new Document(pdfDoc, PageSize.A4);

            doc.setMargins(MARGEN, MARGEN, MARGEN + 20f, MARGEN); // bottom extra para pie

            // ── Fuentes estándar embebidas (no requieren archivos externos) ──
            PdfFont fontBold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontOblique = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            // ═══════════════════════════════════════════════════════════════
            // SECCIÓN 0: ENCABEZADO / PORTADA
            // ═══════════════════════════════════════════════════════════════
            agregarEncabezado(doc, fontBold, fontRegular, data.getCultivo());

            // ═══════════════════════════════════════════════════════════════
            // SECCIÓN 1: DATOS DEL AGRICULTOR
            // ═══════════════════════════════════════════════════════════════
            agregarSeccionAgricultor(doc, fontBold, fontRegular, data.getAgricultor());

            // ═══════════════════════════════════════════════════════════════
            // SECCIÓN 2: INFORMACIÓN DEL CULTIVO
            // ═══════════════════════════════════════════════════════════════
            agregarSeccionCultivo(doc, fontBold, fontRegular, data.getCultivo());

            // ═══════════════════════════════════════════════════════════════
            // SECCIÓN 3: LÍNEA DE TIEMPO — HISTORIAL DE ETAPAS
            // ═══════════════════════════════════════════════════════════════
            agregarSeccionTimeline(doc, fontBold, fontRegular, fontOblique,
                    data.getHistorialEtapas());

            // ═══════════════════════════════════════════════════════════════
            // SECCIÓN 4: PEDIDOS VINCULADOS
            // ═══════════════════════════════════════════════════════════════
            agregarSeccionPedidos(doc, fontBold, fontRegular, data.getPedidosVinculados());

            // ── Pie de página manual (número de pág. + marca) ──────────────
            agregarPieDePagina(pdfDoc, doc, fontRegular);

            doc.close();
            log.info("PDF generado exitosamente ({} bytes)", baos.size());
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error al generar PDF de trazabilidad", e);
            throw new RuntimeException("No se pudo generar el PDF de trazabilidad: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // SECCIÓN 0: ENCABEZADO
    // =========================================================================

    private void agregarEncabezado(Document doc, PdfFont bold, PdfFont regular,
                                   CultivoInfoDTO cultivo) throws IOException {
        // Bloque de fondo verde con título
        Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth();

        Cell celdaTitulo = new Cell()
                .setBackgroundColor(COLOR_VERDE_OSCURO)
                .setPadding(18f)
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph("🌱 AGROLINK")
                        .setFont(bold).setFontSize(22f)
                        .setFontColor(COLOR_BLANCO)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(4f))
                .add(new Paragraph("Certificado de Trazabilidad Agrícola")
                        .setFont(regular).setFontSize(13f)
                        .setFontColor(COLOR_VERDE_MENTA)
                        .setTextAlignment(TextAlignment.CENTER));
        header.addCell(celdaTitulo);
        doc.add(header);

        // Línea de metadatos: cultivo + fecha de emisión
        Table meta = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginTop(0f);

        meta.addCell(crearCeldaMeta(
                "Cultivo N.° " + cultivo.getIdCultivo() + " — " + cultivo.getLote(),
                regular, TextAlignment.LEFT));
        meta.addCell(crearCeldaMeta(
                "Emitido: " + LocalDate.now().format(DATE_FMT),
                regular, TextAlignment.RIGHT));
        doc.add(meta);

        doc.add(new Paragraph(" ").setFontSize(4f)); // espacio
    }

    private Cell crearCeldaMeta(String texto, PdfFont font, TextAlignment align) {
        return new Cell()
                .setBackgroundColor(COLOR_VERDE_CLARO)
                .setPaddingLeft(12f).setPaddingRight(12f).setPaddingTop(6f).setPaddingBottom(6f)
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph(texto)
                        .setFont(font).setFontSize(9f)
                        .setFontColor(COLOR_VERDE_OSCURO)
                        .setTextAlignment(align));
    }

    // =========================================================================
    // SECCIÓN 1: DATOS DEL AGRICULTOR
    // =========================================================================

    private void agregarSeccionAgricultor(Document doc, PdfFont bold, PdfFont regular,
                                          AgricultorInfoDTO a) throws IOException {
        doc.add(crearTituloSeccion("1. DATOS DEL AGRICULTOR", bold));

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{3, 3, 3, 3}))
                .useAllAvailableWidth()
                .setMarginBottom(14f);

        // Fila de encabezados
        String[] headers = {"Agricultor", "DNI / RUC", "Email", "Ubicación"};
        for (String h : headers) {
            tabla.addHeaderCell(crearCeldaEncabezado(h, bold));
        }

        // Nombre completo
        tabla.addCell(crearCeldaDato(
                a.getNombres() + " " + a.getApellidoPaterno()
                + (a.getApellidoMaterno() != null ? " " + a.getApellidoMaterno() : ""),
                regular, false));
        tabla.addCell(crearCeldaDato(nvl(a.getDniRuc()), regular, false));
        tabla.addCell(crearCeldaDato(nvl(a.getEmail()), regular, false));
        tabla.addCell(crearCeldaDato(nvl(a.getUbicacion()), regular, false));

        // Segunda fila con campos extras
        String[] headers2 = {"Hectáreas totales", "Años de experiencia", "Certificaciones", ""};
        for (String h : headers2) {
            tabla.addHeaderCell(crearCeldaEncabezado(h, bold));
        }

        tabla.addCell(crearCeldaDato(
                a.getHectareasTotales() != null ? a.getHectareasTotales() + " ha" : "—",
                regular, true));
        tabla.addCell(crearCeldaDato(
                a.getAnosExperiencia() != null ? a.getAnosExperiencia() + " años" : "—",
                regular, true));
        tabla.addCell(crearCeldaDato(nvl(a.getCertificaciones()), regular, true));
        tabla.addCell(crearCeldaDato("", regular, true));

        doc.add(tabla);
    }

    // =========================================================================
    // SECCIÓN 2: INFORMACIÓN DEL CULTIVO
    // =========================================================================

    private void agregarSeccionCultivo(Document doc, PdfFont bold, PdfFont regular,
                                       CultivoInfoDTO c) throws IOException {
        doc.add(crearTituloSeccion("2. INFORMACIÓN DEL CULTIVO", bold));

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2, 2, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(14f);

        // Encabezados fila 1
        for (String h : new String[]{"Producto", "Variedad", "Lote", "Estado actual", "Fecha siembra", "Área (ha)"}) {
            tabla.addHeaderCell(crearCeldaEncabezado(h, bold));
        }
        tabla.addCell(crearCeldaDato(nvl(c.getNombreProducto()),     regular, false));
        tabla.addCell(crearCeldaDato(nvl(c.getNombreVariedad()),     regular, false));
        tabla.addCell(crearCeldaDato(nvl(c.getLote()),               regular, false));

        // Estado con badge de color
        Cell celdaEstado = crearCeldaDato(nvl(c.getEstadoActual()), bold, false);
        celdaEstado.setFontColor(resolverColorEstado(c.getEstadoActual()));
        tabla.addCell(celdaEstado);

        tabla.addCell(crearCeldaDato(
                c.getFechaSiembra() != null ? c.getFechaSiembra().format(DATE_FMT) : "—",
                regular, false));
        tabla.addCell(crearCeldaDato(
                c.getAreaSembrada() != null ? c.getAreaSembrada().toPlainString() : "—",
                regular, false));

        // Encabezados fila 2
        for (String h : new String[]{"Cantidad estimada", "Disponible", "Precio unitario", "Mínimo venta", "Unidad", "Días estimados"}) {
            tabla.addHeaderCell(crearCeldaEncabezado(h, bold));
        }
        tabla.addCell(crearCeldaDato(
                c.getCantidadEstimada() != null ? c.getCantidadEstimada().toPlainString() : "—",
                regular, true));
        tabla.addCell(crearCeldaDato(
                c.getCantidadDisponible() != null ? c.getCantidadDisponible().toPlainString() : "—",
                regular, true));
        tabla.addCell(crearCeldaDato(
                c.getPrecio() != null ? "S/ " + c.getPrecio().toPlainString() : "—",
                regular, true));
        tabla.addCell(crearCeldaDato(
                c.getMinimoVenta() != null ? c.getMinimoVenta().toPlainString() : "—",
                regular, true));
        tabla.addCell(crearCeldaDato(nvl(c.getUnidad()), regular, true));
        tabla.addCell(crearCeldaDato(
                c.getDiasTotalesEstimados() != null ? c.getDiasTotalesEstimados() + " días" : "—",
                regular, true));

        doc.add(tabla);

        // Observaciones generales del cultivo (si existen)
        if (c.getObservaciones() != null && !c.getObservaciones().isBlank()) {
            Div obsBox = new Div()
                    .setBackgroundColor(COLOR_VERDE_CLARO)
                    .setBorderLeft(new SolidBorder(COLOR_VERDE_MEDIO, 3f))
                    .setPaddingLeft(10f).setPaddingRight(10f)
                    .setPaddingTop(6f).setPaddingBottom(6f)
                    .setMarginBottom(14f);
            obsBox.add(new Paragraph("Observaciones:")
                    .setFont(bold).setFontSize(9f)
                    .setFontColor(COLOR_VERDE_OSCURO)
                    .setMarginBottom(2f));
            obsBox.add(new Paragraph(c.getObservaciones())
                    .setFont(regular).setFontSize(9f)
                    .setFontColor(COLOR_GRIS_TEXTO));
            doc.add(obsBox);
        }
    }

    // =========================================================================
    // SECCIÓN 3: LÍNEA DE TIEMPO (TIMELINE)
    // =========================================================================

    private void agregarSeccionTimeline(Document doc, PdfFont bold, PdfFont regular,
                                        PdfFont oblique, List<EtapaDTO> etapas) throws IOException {
        doc.add(crearTituloSeccion("3. LÍNEA DE TIEMPO — HISTORIAL DE ETAPAS", bold));

        if (etapas == null || etapas.isEmpty()) {
            doc.add(new Paragraph("No se registraron etapas para este cultivo.")
                    .setFont(oblique).setFontSize(10f).setFontColor(COLOR_GRIS_TEXTO)
                    .setMarginLeft(16f).setMarginBottom(14f));
            return;
        }

        for (int i = 0; i < etapas.size(); i++) {
            EtapaDTO etapa = etapas.get(i);
            boolean esUltima = (i == etapas.size() - 1);

            // ── Fila de la etapa: conector vertical + tarjeta ─────────────
            Table fila = new Table(UnitValue.createPercentArray(new float[]{0.5f, 9.5f}))
                    .useAllAvailableWidth()
                    .setMarginBottom(0f);

            // Columna izquierda: bola + línea vertical conectora
            Cell colConector = new Cell().setBorder(Border.NO_BORDER).setPadding(0f);
            colConector.add(agregarConectorTimeline(esUltima, etapa, bold));
            fila.addCell(colConector);

            // Columna derecha: tarjeta de la etapa
            Cell colTarjeta = new Cell().setBorder(Border.NO_BORDER).setPaddingLeft(8f)
                    .setPaddingBottom(esUltima ? 0f : 12f);
            colTarjeta.add(crearTarjetaEtapa(etapa, bold, regular, oblique));
            fila.addCell(colTarjeta);

            doc.add(fila);
        }

        doc.add(new Paragraph(" ").setFontSize(6f));
    }

    /**
     * Crea la columna izquierda del timeline: círculo indicador + línea de conexión.
     * La línea se simula con un párrafo de borde izquierdo.
     */
    private Div agregarConectorTimeline(boolean esUltima, EtapaDTO etapa, PdfFont bold) {
        Div contenedor = new Div().setWidth(20f);

        // Círculo: simulado con un párrafo centrado con fondo de color
        DeviceRgb colorBola = etapa.getActiva()
                ? COLOR_VERDE_MEDIO
                : (Boolean.TRUE.equals(etapa.getAlertaRetraso()) ? COLOR_NARANJA_ALERTA : COLOR_VERDE_OSCURO);

        Paragraph bola = new Paragraph(etapa.getActiva() ? "◉" : "●")
                .setFont(bold).setFontSize(14f)
                .setFontColor(colorBola)
                .setTextAlignment(TextAlignment.CENTER)
                .setMargin(0f);
        contenedor.add(bola);

        // Línea vertical (solo si no es el último elemento)
        if (!esUltima) {
            Paragraph linea = new Paragraph("│\n│\n│")
                    .setFont(bold).setFontSize(10f)
                    .setFontColor(COLOR_VERDE_MENTA)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0f).setPadding(0f);
            contenedor.add(linea);
        }
        return contenedor;
    }

    /**
     * Tarjeta visual de una etapa del historial.
     * Usa un Div con borde izquierdo de color para distinguir estado/alerta.
     */
    private Div crearTarjetaEtapa(EtapaDTO etapa, PdfFont bold, PdfFont regular,
                                   PdfFont oblique) {
        DeviceRgb colorBorde = Boolean.TRUE.equals(etapa.getAlertaRetraso())
                ? COLOR_NARANJA_ALERTA
                : (etapa.getActiva() ? COLOR_VERDE_MEDIO : COLOR_VERDE_OSCURO);

        DeviceRgb colorFondo = etapa.getActiva()
                ? COLOR_VERDE_CLARO
                : COLOR_GRIS_CLARO;

        Div tarjeta = new Div()
                .setBackgroundColor(colorFondo)
                .setBorderLeft(new SolidBorder(colorBorde, 3f))
                .setPaddingLeft(10f).setPaddingRight(10f)
                .setPaddingTop(8f).setPaddingBottom(8f)
                .setMarginBottom(4f);

        // Nombre de la etapa + badge de estado
        String nombreEtapa = etapa.getNombreEtapa() != null
                ? etapa.getNombreEtapa()
                : etapa.getEstadoCultivo();

        String badge = etapa.getActiva() ? " [ACTIVA]" : "";
        if (Boolean.TRUE.equals(etapa.getAlertaRetraso())) badge = " [⚠ RETRASO]";

        tarjeta.add(new Paragraph(nombreEtapa + badge)
                .setFont(bold).setFontSize(11f)
                .setFontColor(colorBorde)
                .setMarginBottom(4f));

        // Sub-línea: estado del cultivo
        tarjeta.add(new Paragraph("Estado cultivo: " + nvl(etapa.getEstadoCultivo()))
                .setFont(regular).setFontSize(9f)
                .setFontColor(COLOR_GRIS_TEXTO)
                .setMarginBottom(2f));

        // Fechas
        String inicio = etapa.getFechaInicio() != null
                ? etapa.getFechaInicio().format(DATE_FMT) : "—";
        String fin    = etapa.getFechaFin() != null
                ? etapa.getFechaFin().format(DATE_FMT) : "En curso";
        tarjeta.add(new Paragraph("Período: " + inicio + " → " + fin)
                .setFont(oblique).setFontSize(9f)
                .setFontColor(COLOR_GRIS_TEXTO)
                .setMarginBottom(2f));

        // Observaciones de esta etapa (si existen)
        if (etapa.getObservaciones() != null && !etapa.getObservaciones().isBlank()) {
            tarjeta.add(new Paragraph("📝 " + etapa.getObservaciones())
                    .setFont(oblique).setFontSize(9f)
                    .setFontColor(COLOR_GRIS_TEXTO)
                    .setMarginBottom(2f));
        }

        // Métricas de tiempo
        Table metricas = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth().setMarginTop(4f);

        metricas.addCell(crearCeldaMetrica(
                "Días transcurridos",
                etapa.getDiasTranscurridos() != null ? etapa.getDiasTranscurridos() + " d" : "—",
                bold, regular));
        metricas.addCell(crearCeldaMetrica(
                "Días estimados",
                etapa.getDiasDuracionEstimada() != null ? etapa.getDiasDuracionEstimada() + " d" : "N/A",
                bold, regular));

        // % de retraso con color semáforo
        String pctText = "—";
        DeviceRgb colorPct = COLOR_VERDE_OSCURO;
        if (etapa.getPorcentajeRetraso() != null) {
            double pct = etapa.getPorcentajeRetraso();
            pctText = String.format("%.1f%%", Math.abs(pct)) + (pct >= 0 ? " retraso" : " adelanto");
            colorPct = pct > 20 ? COLOR_ROJO_ALERTA : (pct > 0 ? COLOR_NARANJA_ALERTA : COLOR_VERDE_OSCURO);
        }
        Cell celdaPct = crearCeldaMetrica("Variación", pctText, bold, regular);
        celdaPct.setFontColor(colorPct);
        metricas.addCell(celdaPct);

        tarjeta.add(metricas);
        return tarjeta;
    }

    private Cell crearCeldaMetrica(String label, String valor, PdfFont bold, PdfFont regular) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingRight(8f)
                .add(new Paragraph(label)
                        .setFont(regular).setFontSize(8f)
                        .setFontColor(COLOR_GRIS_TEXTO).setMarginBottom(1f))
                .add(new Paragraph(valor)
                        .setFont(bold).setFontSize(10f)
                        .setFontColor(COLOR_VERDE_OSCURO));
    }

    // =========================================================================
    // SECCIÓN 4: PEDIDOS VINCULADOS
    // =========================================================================

    private void agregarSeccionPedidos(Document doc, PdfFont bold, PdfFont regular,
                                       List<PedidoVinculadoDTO> pedidos) throws IOException {
        doc.add(crearTituloSeccion("4. PEDIDOS VINCULADOS", bold));

        if (pedidos == null || pedidos.isEmpty()) {
            doc.add(new Paragraph("No existen pedidos vinculados a este cultivo.")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE))
                    .setFontSize(10f).setFontColor(COLOR_GRIS_TEXTO)
                    .setMarginLeft(16f).setMarginBottom(14f));
            return;
        }

        // Tabla de pedidos con 7 columnas
        Table tabla = new Table(UnitValue.createPercentArray(
                new float[]{1.2f, 2.5f, 2.5f, 1.5f, 1.5f, 1.5f, 2f}))
                .useAllAvailableWidth()
                .setMarginBottom(14f);

        String[] cols = {"Pedido N.°", "Comprador", "Negocio",
                "Cant. (kg)", "Precio unit.", "Estado", "Fecha"};
        for (String c : cols) tabla.addHeaderCell(crearCeldaEncabezado(c, bold));

        boolean alt = false;
        for (PedidoVinculadoDTO p : pedidos) {
            tabla.addCell(crearCeldaDato("#" + p.getIdPedido(), regular, alt));
            tabla.addCell(crearCeldaDato(nvl(p.getNombreComprador()), regular, alt));
            tabla.addCell(crearCeldaDato(nvl(p.getNombreNegocio()), regular, alt));
            tabla.addCell(crearCeldaDato(
                    p.getCantidadSolicitada() != null ? p.getCantidadSolicitada().toPlainString() : "—",
                    regular, alt));
            tabla.addCell(crearCeldaDato(
                    p.getPrecioPactado() != null ? "S/ " + p.getPrecioPactado().toPlainString() : "—",
                    regular, alt));

            // Badge de estado con color
            DeviceRgb colorEstado = resolverColorEstadoPedido(p.getEstadoPedido());
            Cell cEstado = crearCeldaDato(nvl(p.getEstadoPedido()), bold, alt);
            cEstado.setFontColor(colorEstado);
            tabla.addCell(cEstado);

            tabla.addCell(crearCeldaDato(
                    p.getFechaCreacion() != null
                            ? p.getFechaCreacion().format(java.time.format.DateTimeFormatter
                                .ofPattern("dd/MM/yyyy")) : "—",
                    regular, alt));
            alt = !alt;
        }

        doc.add(tabla);

        // Nota legal al pie de la sección
        doc.add(new Paragraph(
                "Este documento es un certificado informativo generado automáticamente " +
                "por el sistema AgroLink. La información aquí contenida refleja el estado " +
                "del cultivo al momento de la emisión y puede variar.")
                .setFont(regular).setFontSize(8f)
                .setFontColor(COLOR_GRIS_TEXTO)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8f));
    }

    // =========================================================================
    // PIE DE PÁGINA
    // =========================================================================

    private void agregarPieDePagina(PdfDocument pdfDoc, Document doc, PdfFont font)
            throws IOException {
        int totalPaginas = pdfDoc.getNumberOfPages();
        for (int i = 1; i <= totalPaginas; i++) {
            doc.showTextAligned(
                    new Paragraph("AgroLink — Trazabilidad Agrícola  |  Pág. " + i + " / " + totalPaginas)
                            .setFont(font).setFontSize(8f)
                            .setFontColor(COLOR_GRIS_TEXTO),
                    297.5f,   // centro horizontal A4 = 595/2
                    25f,      // margen inferior
                    i,
                    TextAlignment.CENTER,
                    VerticalAlignment.BOTTOM,
                    0f);
        }
    }

    // =========================================================================
    // HELPERS DE ESTILO
    // =========================================================================

    private Paragraph crearTituloSeccion(String texto, PdfFont bold) {
        return new Paragraph(texto)
                .setFont(bold).setFontSize(11f)
                .setFontColor(COLOR_BLANCO)
                .setBackgroundColor(COLOR_VERDE_MEDIO)
                .setPaddingLeft(10f).setPaddingTop(5f).setPaddingBottom(5f)
                .setMarginTop(12f).setMarginBottom(4f);
    }

    private Cell crearCeldaEncabezado(String texto, PdfFont bold) {
        return new Cell()
                .setBackgroundColor(COLOR_VERDE_OSCURO)
                .setFontColor(ColorConstants.WHITE)
                .setFont(bold).setFontSize(9f)
                .setPadding(5f)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(new SolidBorder(COLOR_VERDE_OSCURO, 0.5f))
                .add(new Paragraph(texto));
    }

    private Cell crearCeldaDato(String texto, PdfFont font, boolean alternada) {
        return new Cell()
                .setBackgroundColor(alternada ? COLOR_VERDE_CLARO : COLOR_BLANCO)
                .setFont(font).setFontSize(9f)
                .setFontColor(COLOR_GRIS_TEXTO)
                .setPadding(5f)
                .setBorder(new SolidBorder(COLOR_VERDE_MENTA, 0.3f))
                .add(new Paragraph(nvl(texto)));
    }

    // ── Resolución de colores semáforo ────────────────────────────────────────

    private DeviceRgb resolverColorEstado(String estado) {
        if (estado == null) return COLOR_GRIS_TEXTO;
        return switch (estado.toLowerCase()) {
            case "listo para cosechar", "cosechado" -> COLOR_VERDE_OSCURO;
            case "en crecimiento", "en curso"        -> COLOR_VERDE_MEDIO;
            case "planificado"                       -> new DeviceRgb(0x01, 0x57, 0x9B);
            case "cancelado"                         -> COLOR_ROJO_ALERTA;
            default                                  -> COLOR_GRIS_TEXTO;
        };
    }

    private DeviceRgb resolverColorEstadoPedido(String estado) {
        if (estado == null) return COLOR_GRIS_TEXTO;
        return switch (estado.toLowerCase()) {
            case "confirmado", "entregado"           -> COLOR_VERDE_OSCURO;
            case "pendiente", "en preparación"       -> COLOR_NARANJA_ALERTA;
            case "cancelado"                         -> COLOR_ROJO_ALERTA;
            default                                  -> COLOR_GRIS_TEXTO;
        };
    }

    /** Reemplaza null por guión largo para que no aparezca "null" en el PDF. */
    private String nvl(String valor) {
        return (valor != null && !valor.isBlank()) ? valor : "—";
    }
}
