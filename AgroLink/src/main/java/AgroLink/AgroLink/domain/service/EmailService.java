package AgroLink.AgroLink.domain.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Recuperar contraseña - AgroLink 🌱");
        helper.setText("""
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
        """.formatted(token), true);

        mailSender.send(message);
    }

    public void sendVerificationEmail(String toEmail, String codigo) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Verifica tu cuenta - AgroLink 🌱");
        helper.setText("""
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
                <p style="color: #999; font-size: 13px; text-align: center; ">
                    Si no creaste esta cuenta, ignora este mensaje.
                </p>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px; text-align: center;">
                    © 2024 AgroLink — Todos los derechos reservados
                </p>
            </div>
        </body>
        </html>
    """.formatted(codigo), true);

        mailSender.send(message);
    }

    public void sendAlertaStockMinimo(String toEmail, String nombreAgricultor,
                                      String lote, String producto,
                                      String cantidadDisponible, String minimoVenta,
                                      String unidad) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Alerta de Stock Minimo - Lote " + lote + " | AgroLink");
        helper.setText("""
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 520px; margin: auto; background-color: white;
                            border-radius: 10px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">

                    <h2 style="color: #2e7d32;">AgroLink</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>El stock de uno de tus cultivos ha alcanzado el <strong style="color:#e53935;">nivel minimo de venta</strong>.</p>

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
                            <td style="padding:10px; border:1px solid #c8e6c9; font-weight:bold;">Minimo de venta</td>
                            <td style="padding:10px; border:1px solid #c8e6c9;">%s %s</td>
                        </tr>
                    </table>

                    <p style="color:#555;">Te recomendamos revisar este cultivo y actualizar tu inventario.</p>

                    <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                    <p style="color:#999; font-size:12px; text-align:center;">
                        © 2026 AgroLink - Todos los derechos reservados
                    </p>
                </div>
            </body>
            </html>
        """.formatted(nombreAgricultor, lote, producto,
                cantidadDisponible, unidad,
                minimoVenta, unidad), true);

        mailSender.send(message);
    }

    public void sendAlertaCosecha(String toEmail, String nombreAgricultor,
                                  String lote, String producto,
                                  String fechaCosecha, long diasRestantes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String diasTexto = diasRestantes == 0 ? "hoy" : (diasRestantes + " dia(s)");

        helper.setTo(toEmail);
        helper.setSubject("Cosecha proxima - Lote " + lote + " | AgroLink");
        helper.setText("""
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 520px; margin: auto; background-color: white;
                            border-radius: 10px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">

                    <h2 style="color: #2e7d32;">AgroLink</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Tu cultivo esta proximo a su fecha estimada de cosecha.</p>

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
                    <p style="color:#999; font-size:12px; text-align:center;">
                        © 2026 AgroLink - Todos los derechos reservados
                    </p>
                </div>
            </body>
            </html>
        """.formatted(nombreAgricultor, lote, producto, fechaCosecha, diasTexto), true);

        mailSender.send(message);
    }
}