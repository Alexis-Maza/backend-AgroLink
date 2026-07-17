package AgroLink.AgroLink.domain.exception;

import java.math.BigDecimal;

public class StockInsuficienteException extends RuntimeException {
    private final String nombreProducto;
    private final BigDecimal stockDisponible;
    private final String unidad;

    public StockInsuficienteException(String nombreProducto, BigDecimal stockDisponible, String unidad) {
        super("Stock insuficiente para: " + nombreProducto + ". Disponible: " + stockDisponible + " " + unidad);
        this.nombreProducto = nombreProducto;
        this.stockDisponible = stockDisponible;
        this.unidad = unidad;
    }

    public String getNombreProducto() { return nombreProducto; }
    public BigDecimal getStockDisponible() { return stockDisponible; }
    public String getUnidad() { return unidad; }
}