package AgroLink.AgroLink.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name}")
    private String fromName;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private void enviar(String toEmail, String subject, String html) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", fromName, "email", fromEmail),
                    "to", List.of(Map.of("email", toEmail)),
                    "subject", subject,
                    "htmlContent", html
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(BREVO_URL, request, String.class);
            System.out.println("Email enviado correctamente a: " + toEmail);
        } catch (Exception e) {
            System.err.println("Error al enviar email: " + e.getMessage());
        }
    }

    public void sendVerificationEmail(String toEmail, String codigo) {
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 500px; margin: auto; background-color: white;
                            border-radius: 10px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2e7d32;">🌱 AgroLink</h2>
                    <p>¡Bienvenido! Estás a un paso de unirte a AgroLink.</p>
                    <p>Tu código de verificación es:</p>
                    <div style="text-align: center; margin: 40px 0;">
                        <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                                     color: white; background-color: #2e7d32;
                                     padding: 15px 30px; border-radius: 8px;">
                            %s
                        </span>
                    </div>
                    <p style="color: #e53935; text-align: center; font-size: 13px;">⏱ Este código expira en 5 minutos.</p>
                    <p style="color: #999; font-size: 13px; text-align: center;">
                        Si no creaste esta cuenta, ignora este mensaje.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        © 2026 AgroLink — Todos los derechos reservados
                    </p>
                </div>
            </body>
            </html>
        """.formatted(codigo);
        enviar(toEmail, "Verifica tu cuenta - AgroLink 🌱", html);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 500px; margin: auto; background-color: white;
                            border-radius: 10px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2e7d32;">🌱 AgroLink</h2>
                    <p>Hola,</p>
                    <p>Recibimos una solicitud para restablecer tu contraseña.</p>
                    <p>Tu código de recuperación es:</p>
                    <div style="text-align: center; margin: 40px 0;">
                        <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                                     color: white; background-color: #2e7d32;
                                     padding: 15px 30px; border-radius: 8px;">
                            %s
                        </span>
                    </div>
                    <p style="color: #e53935; text-align: center; font-size: 13px;">⏱ Este código expira en 5 minutos.</p>
                    <p style="color: #999; font-size: 13px; text-align: center;">
                        Si no solicitaste esto, ignora este mensaje.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        © 2026 AgroLink — Todos los derechos reservados
                    </p>
                </div>
            </body>
            </html>
        """.formatted(token);
        enviar(toEmail, "Recuperar contraseña - AgroLink 🌱", html);
    }

    public void sendAlertaStockMinimo(String toEmail, String nombreAgricultor,
                                      String lote, String producto,
                                      String cantidadDisponible, String minimoVenta,
                                      String unidad) {
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 520px; margin: auto; background-color: white;
                            border-radius: 10px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2e7d32;">AgroLink</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>El stock de uno de tus cultivos ha alcanzado el <strong style="color:#e53935;">nivel mínimo de venta</strong>.</p>
                    <table style="width:100%%; border-collapse:collapse; margin: 20px 0;">
                        <tr style="background-color:#e8f5e9;">
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Lote</td>
                            <td style="padding:10px; border:1px solid #c8e6c9;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Producto / Variedad</td>
                            <td style="padding:10px; border:1px solid #c8e6c9;">%s</td>
                        </tr>
                        <tr style="background-color:#e8f5e9;">
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Cantidad disponible</td>
                            <td style="padding:10px; border:1px solid #c8e6c9; color:#e53935; font-weight:bold;">%s %s</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Mínimo de venta</td>
                            <td style="padding:10px; border:1px solid #c8e6c9;">%s %s</td>
                        </tr>
                    </table>
                    <p style="color:#555;">Te recomendamos revisar este cultivo y actualizar tu inventario.</p>
                    <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                    <p style="color:#999; font-size:12px; text-align:center;">© 2026 AgroLink - Todos los derechos reservados</p>
                </div>
            </body>
            </html>
        """.formatted(nombreAgricultor, lote, producto, cantidadDisponible, unidad, minimoVenta, unidad);
        enviar(toEmail, "Alerta de Stock Mínimo - Lote " + lote + " | AgroLink", html);
    }

    public void sendAlertaCosecha(String toEmail, String nombreAgricultor,
                                  String lote, String producto,
                                  String fechaCosecha, long diasRestantes) {
        String diasTexto = diasRestantes == 0 ? "hoy" : (diasRestantes + " día(s)");
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 520px; margin: auto; background-color: white;
                            border-radius: 10px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2e7d32;">AgroLink</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Tu cultivo está próximo a su fecha estimada de cosecha.</p>
                    <table style="width:100%%; border-collapse:collapse; margin: 20px 0;">
                        <tr style="background-color:#e8f5e9;">
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Lote</td>
                            <td style="padding:10px; border:1px solid #c8e6c9;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Producto / Variedad</td>
                            <td style="padding:10px; border:1px solid #c8e6c9;">%s</td>
                        </tr>
                        <tr style="background-color:#e8f5e9;">
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Fecha estimada de cosecha</td>
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold; color:#2e7d32;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Tiempo restante</td>
                            <td style="padding:10px; border:1px solid #c8e6c9; color:#f57f17; font-weight:bold;">%s</td>
                        </tr>
                    </table>
                    <p style="color:#555;">Prepara los recursos necesarios para realizar la cosecha a tiempo.</p>
                    <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                    <p style="color:#999; font-size:12px; text-align:center;">© 2026 AgroLink - Todos los derechos reservados</p>
                </div>
            </body>
            </html>
        """.formatted(nombreAgricultor, lote, producto, fechaCosecha, diasTexto);
        enviar(toEmail, "Cosecha próxima - Lote " + lote + " | AgroLink", html);
    }

    public void sendCancelacionPedidoPorStock(String toEmail, String nombreComprador,
                                               Long idPedido, String producto, String motivo) {
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 520px; margin: auto; background-color: white;
                            border-radius: 10px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2e7d32;">AgroLink</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Lamentamos informarte que tu pedido ha sido <strong style="color:#e53935;">cancelado</strong> por falta de disponibilidad.</p>
                    <table style="width:100%%; border-collapse:collapse; margin: 20px 0;">
                        <tr style="background-color:#e8f5e9;">
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Pedido</td>
                            <td style="padding:10px; border:1px solid #c8e6c9;">#%d</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Producto</td>
                            <td style="padding:10px; border:1px solid #c8e6c9;">%s</td>
                        </tr>
                        <tr style="background-color:#e8f5e9;">
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Motivo</td>
                            <td style="padding:10px; border:1px solid #c8e6c9; color:#e53935;">%s</td>
                        </tr>
                    </table>
                    <p style="color:#555;">Puedes revisar el catálogo para encontrar productos disponibles similares.</p>
                    <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                    <p style="color:#999; font-size:12px; text-align:center;">© 2026 AgroLink - Todos los derechos reservados</p>
                </div>
            </body>
            </html>
        """.formatted(nombreComprador, idPedido, producto, motivo);
        enviar(toEmail, "Pedido #" + idPedido + " cancelado - AgroLink", html);
    }

    public void sendConfirmacionPedido(String toEmail, String nombreComprador, Long idPedido,
                                       List<String> productos, BigDecimal total) {
        String filas = productos.stream()
                .map(p -> "<tr><td style=\"padding:8px; border:1px solid #c8e6c9;\">" + p + "</td></tr>")
                .collect(java.util.stream.Collectors.joining());

        String html = """
        <html>
        <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
            <div style="max-width: 520px; margin: auto; background-color: white;
                        border-radius: 10px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                <h2 style="color: #2e7d32;">AgroLink</h2>
                <p>Hola <strong>%s</strong>,</p>
                <p>¡Gracias por tu compra! Confirmamos que hemos recibido tu pedido.</p>
                <table style="width:100%%; border-collapse:collapse; margin: 20px 0;">
                    <tr style="background-color:#e8f5e9;">
                        <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Pedido</td>
                        <td style="padding:10px; border:1px solid #c8e6c9;">#%d</td>
                    </tr>
                    <tr>
                        <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;" colspan="2">Productos</td>
                    </tr>
                    %s
                    <tr style="background-color:#e8f5e9;">
                        <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Total</td>
                        <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold; color:#2e7d32;">S/ %.2f</td>
                    </tr>
                </table>
                <p style="color:#555;">Puedes seguir el estado de tu pedido desde la sección "Mis Compras".</p>
                <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                <p style="color:#999; font-size:12px; text-align:center;">© 2026 AgroLink - Todos los derechos reservados</p>
            </div>
        </body>
        </html>
    """.formatted(nombreComprador, idPedido, filas, total);
        enviar(toEmail, "Confirmación de tu pedido #" + idPedido + " - AgroLink", html);
    }
}