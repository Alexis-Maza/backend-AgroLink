package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.repository.AgricultorRepository;
import AgroLink.AgroLink.domain.repository.CompradorRepository;
import AgroLink.AgroLink.domain.repository.CultivoRepository;
import AgroLink.AgroLink.domain.repository.PedidoRepository;
import AgroLink.AgroLink.persistance.entity.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final CultivoRepository cultivoRepository;
    private final PedidoRepository pedidoRepository;
    private final AgricultorRepository agricultorRepository;
    private final CompradorRepository compradorRepository;

    private static final byte[] COLOR_VERDE_OSCURO = {(byte)0x1B, (byte)0x5E, (byte)0x20};
    private static final byte[] COLOR_VERDE_CLARO  = {(byte)0xE8, (byte)0xF5, (byte)0xE9};
    private static final byte[] COLOR_TITULO_BG    = {(byte)0x2E, (byte)0x7D, (byte)0x32};

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static class Estilos {
        CellStyle titulo;
        CellStyle encabezado;
        CellStyle dato;
        CellStyle datoAlt;
        CellStyle numero;
        CellStyle numeroAlt;
        CellStyle fecha;
        CellStyle fechaAlt;
    }

    private Estilos crearEstilos(XSSFWorkbook wb) {
        Estilos e = new Estilos();
        DataFormat df = wb.createDataFormat();

        XSSFFont fuenteHeader = wb.createFont();
        fuenteHeader.setBold(true);
        fuenteHeader.setFontHeightInPoints((short) 11);
        fuenteHeader.setColor(new XSSFColor(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF}, null));

        XSSFFont fuenteTitulo = wb.createFont();
        fuenteTitulo.setBold(true);
        fuenteTitulo.setFontHeightInPoints((short) 14);
        fuenteTitulo.setColor(new XSSFColor(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF}, null));

        XSSFFont fuenteDato = wb.createFont();
        fuenteDato.setFontHeightInPoints((short) 10);

        XSSFCellStyle sTitulo = wb.createCellStyle();
        sTitulo.setFont(fuenteTitulo);
        sTitulo.setFillForegroundColor(new XSSFColor(COLOR_TITULO_BG, null));
        sTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sTitulo.setAlignment(HorizontalAlignment.CENTER);
        sTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
        e.titulo = sTitulo;

        XSSFCellStyle sHeader = wb.createCellStyle();
        sHeader.setFont(fuenteHeader);
        sHeader.setFillForegroundColor(new XSSFColor(COLOR_VERDE_OSCURO, null));
        sHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sHeader.setAlignment(HorizontalAlignment.CENTER);
        sHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        sHeader.setWrapText(true);
        aplicarBordes(sHeader);
        e.encabezado = sHeader;

        XSSFCellStyle sDato = wb.createCellStyle();
        sDato.setFont(fuenteDato);
        sDato.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(sDato);
        e.dato = sDato;

        XSSFCellStyle sDatoAlt = wb.createCellStyle();
        sDatoAlt.setFont(fuenteDato);
        sDatoAlt.setFillForegroundColor(new XSSFColor(COLOR_VERDE_CLARO, null));
        sDatoAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sDatoAlt.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(sDatoAlt);
        e.datoAlt = sDatoAlt;

        XSSFCellStyle sNum = wb.createCellStyle();
        sNum.setFont(fuenteDato);
        sNum.setDataFormat(df.getFormat("#,##0.00"));
        sNum.setAlignment(HorizontalAlignment.RIGHT);
        aplicarBordes(sNum);
        e.numero = sNum;

        XSSFCellStyle sNumAlt = wb.createCellStyle();
        sNumAlt.setFont(fuenteDato);
        sNumAlt.setDataFormat(df.getFormat("#,##0.00"));
        sNumAlt.setAlignment(HorizontalAlignment.RIGHT);
        sNumAlt.setFillForegroundColor(new XSSFColor(COLOR_VERDE_CLARO, null));
        sNumAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        aplicarBordes(sNumAlt);
        e.numeroAlt = sNumAlt;

        XSSFCellStyle sFecha = wb.createCellStyle();
        sFecha.setFont(fuenteDato);
        sFecha.setAlignment(HorizontalAlignment.CENTER);
        aplicarBordes(sFecha);
        e.fecha = sFecha;

        XSSFCellStyle sFechaAlt = wb.createCellStyle();
        sFechaAlt.setFont(fuenteDato);
        sFechaAlt.setAlignment(HorizontalAlignment.CENTER);
        sFechaAlt.setFillForegroundColor(new XSSFColor(COLOR_VERDE_CLARO, null));
        sFechaAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        aplicarBordes(sFechaAlt);
        e.fechaAlt = sFechaAlt;

        return e;
    }

    private void aplicarBordes(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void configurarHoja(XSSFSheet sheet, String titulo, String[] headers,
                                Estilos e, int numCols) {
        Row rowTitulo = sheet.createRow(0);
        rowTitulo.setHeightInPoints(28);
        Cell celdaTitulo = rowTitulo.createCell(0);
        celdaTitulo.setCellValue(titulo + "  —  " + LocalDate.now().format(DATE_FMT));
        celdaTitulo.setCellStyle(e.titulo);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, numCols - 1));

        Row rowHeader = sheet.createRow(1);
        rowHeader.setHeightInPoints(20);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = rowHeader.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(e.encabezado);
        }

        sheet.createFreezePane(0, 2);
        sheet.setAutoFilter(new CellRangeAddress(1, 1, 0, numCols - 1));
    }

    // ── Helper para obtener "Producto - Variedad" de forma segura ────────
    private String getNombreProductoVariedad(Producto_Variedad pv) {
        if (pv == null) return "—";
        String variedad = pv.getNombreProductosVariedad() != null ? pv.getNombreProductosVariedad() : "—";
        String producto = (pv.getProducto() != null && pv.getProducto().getNombre() != null)
                ? pv.getProducto().getNombre() : "—";
        return producto + " - " + variedad;
    }

    // ── Catálogo de Cultivos ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] exportCultivosToExcel(String email) {
        Agricultor agricultor = agricultorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RuntimeException("Agricultor no encontrado"));

        List<Cultivo> cultivos = cultivoRepository.findByAgricultor(agricultor);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Catálogo de Cultivos");
            sheet.setTabColor(new XSSFColor(COLOR_VERDE_OSCURO, null));

            Estilos e = crearEstilos(wb);

            String[] headers = {"ID", "Lote", "Producto / Variedad", "Fecha Siembra",
                    "Área (ha)", "Días Estimados", "Estado", "Precio Unit.",
                    "Mínimo Venta", "Cant. Estimada", "Cant. Disponible", "Unidad", "Disponible"};

            configurarHoja(sheet, "Catálogo de Cultivos", headers, e, headers.length);

            int rowNum = 2;
            for (Cultivo c : cultivos) {
                boolean alt = (rowNum % 2 == 0);
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(16);

                celdaDato  (row, 0, String.valueOf(c.getId()),                      alt ? e.datoAlt : e.dato);
                celdaDato  (row, 1, c.getLote(),                                    alt ? e.datoAlt : e.dato);
                celdaDato  (row, 2, getNombreProductoVariedad(c.getProductoVariedad()), alt ? e.datoAlt : e.dato); // ← cambiado
                celdaDato  (row, 3, c.getFechaSiembra().format(DATE_FMT),           alt ? e.fechaAlt : e.fecha);
                celdaNumero(row, 4, toDouble(c.getAreaSembrada()),                  alt ? e.numeroAlt : e.numero);
                celdaDato  (row, 5, String.valueOf(c.getDiasTotalesEstimados()),    alt ? e.datoAlt : e.dato);
                celdaDato  (row, 6, c.getEstadoCultivo().getDescripcionEstadoCultivo(), alt ? e.datoAlt : e.dato);
                celdaNumero(row, 7, toDouble(c.getPrecio()),                        alt ? e.numeroAlt : e.numero);
                celdaNumero(row, 8, toDouble(c.getMinimoVenta()),                   alt ? e.numeroAlt : e.numero);
                celdaNumero(row, 9, toDouble(c.getCantidadEstimada()),              alt ? e.numeroAlt : e.numero);
                celdaNumero(row, 10, toDouble(c.getCantidadDisponible()),           alt ? e.numeroAlt : e.numero);
                celdaDato  (row, 11, c.getUnidad(),                                 alt ? e.datoAlt : e.dato);
                celdaDato  (row, 12, Boolean.TRUE.equals(c.getDisponible()) ? "✔ Sí" : "✘ No", alt ? e.datoAlt : e.dato);
            }

            autoAjustarColumnas(sheet, headers.length);
            wb.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new RuntimeException("Error al generar catálogo de cultivos en Excel", ex);
        }
    }

    // ── Mis Ventas ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] exportVentasToExcel(String email) {
        Agricultor agricultor = agricultorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RuntimeException("Agricultor no encontrado"));

        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDetalles().stream()
                        .anyMatch(d -> d.getCultivo().getAgricultor().getId().equals(agricultor.getId())))
                .toList();

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Mis Ventas");
            sheet.setTabColor(new XSSFColor(COLOR_VERDE_OSCURO, null));

            Estilos e = crearEstilos(wb);

            String[] headers = {"N° Pedido", "Fecha", "Comprador", "Producto / Variedad",
                    "Lote", "Cantidad", "Precio Pactado", "Total", "Estado"};

            configurarHoja(sheet, "Reporte de Ventas", headers, e, headers.length);

            int rowNum = 2;
            for (Pedido p : pedidos) {
                for (DetallePedido d : p.getDetalles()) {
                    if (!d.getCultivo().getAgricultor().getId().equals(agricultor.getId())) continue;
                    boolean alt = (rowNum % 2 == 0);
                    Row row = sheet.createRow(rowNum++);
                    row.setHeightInPoints(16);

                    double cantidad = toDouble(d.getCantidadSolicitada());
                    double precio   = toDouble(d.getPrecioPactado());
                    double total    = cantidad * precio;

                    celdaDato  (row, 0, String.valueOf(p.getId()),                  alt ? e.datoAlt : e.dato);
                    celdaDato  (row, 1, p.getFechaCreacion().format(DATETIME_FMT),  alt ? e.fechaAlt : e.fecha);
                    celdaDato  (row, 2, p.getComprador().getUsuario().getNombres()
                            + " " + p.getComprador().getUsuario().getApellidoPaterno(), alt ? e.datoAlt : e.dato);
                    celdaDato  (row, 3, getNombreProductoVariedad(d.getCultivo().getProductoVariedad()), alt ? e.datoAlt : e.dato); // ← cambiado
                    celdaDato  (row, 4, d.getCultivo().getLote(),                   alt ? e.datoAlt : e.dato);
                    celdaNumero(row, 5, cantidad,                                   alt ? e.numeroAlt : e.numero);
                    celdaNumero(row, 6, precio,                                     alt ? e.numeroAlt : e.numero);
                    celdaNumero(row, 7, total,                                      alt ? e.numeroAlt : e.numero);
                    celdaDato  (row, 8, p.getEstadoPedido().getDescripcionEstadoPedido(), alt ? e.datoAlt : e.dato);
                }
            }

            autoAjustarColumnas(sheet, headers.length);
            wb.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new RuntimeException("Error al generar reporte de ventas en Excel", ex);
        }
    }

    // ── Mis Compras ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] exportPedidosToExcel(String email) {
        Comprador comprador = compradorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RuntimeException("Comprador no encontrado"));

        List<Pedido> pedidos = pedidoRepository.findByComprador(comprador);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Mis Compras");
            sheet.setTabColor(new XSSFColor(COLOR_VERDE_OSCURO, null));

            Estilos e = crearEstilos(wb);

            String[] headers = {"N° Pedido", "Fecha", "Producto / Variedad", "Lote",
                    "Agricultor", "Cantidad", "Precio Pactado", "Total", "Dirección Entrega", "Estado"};

            configurarHoja(sheet, "Reporte de Compras", headers, e, headers.length);

            int rowNum = 2;
            for (Pedido p : pedidos) {
                for (DetallePedido d : p.getDetalles()) {
                    boolean alt = (rowNum % 2 == 0);
                    Row row = sheet.createRow(rowNum++);
                    row.setHeightInPoints(16);

                    double cantidad = toDouble(d.getCantidadSolicitada());
                    double precio   = toDouble(d.getPrecioPactado());
                    double total    = cantidad * precio;

                    celdaDato  (row, 0, String.valueOf(p.getId()),                  alt ? e.datoAlt : e.dato);
                    celdaDato  (row, 1, p.getFechaCreacion().format(DATETIME_FMT),  alt ? e.fechaAlt : e.fecha);
                    celdaDato  (row, 2, getNombreProductoVariedad(d.getCultivo().getProductoVariedad()), alt ? e.datoAlt : e.dato); // ← cambiado
                    celdaDato  (row, 3, d.getCultivo().getLote(),                   alt ? e.datoAlt : e.dato);
                    celdaDato  (row, 4, d.getCultivo().getAgricultor().getUsuario().getNombres()
                            + " " + d.getCultivo().getAgricultor().getUsuario().getApellidoPaterno(), alt ? e.datoAlt : e.dato);
                    celdaNumero(row, 5, cantidad,                                   alt ? e.numeroAlt : e.numero);
                    celdaNumero(row, 6, precio,                                     alt ? e.numeroAlt : e.numero);
                    celdaNumero(row, 7, total,                                      alt ? e.numeroAlt : e.numero);
                    celdaDato  (row, 8, d.getDireccion(),                           alt ? e.datoAlt : e.dato);
                    celdaDato  (row, 9, p.getEstadoPedido().getDescripcionEstadoPedido(), alt ? e.datoAlt : e.dato);
                }
            }

            autoAjustarColumnas(sheet, headers.length);
            wb.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new RuntimeException("Error al generar reporte de compras en Excel", ex);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void celdaDato(Row row, int col, String valor, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(valor != null ? valor : "");
        cell.setCellStyle(style);
    }

    private void celdaNumero(Row row, int col, double valor, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(valor);
        cell.setCellStyle(style);
    }

    private void autoAjustarColumnas(XSSFSheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
        }
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}