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
}