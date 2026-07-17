package AgroLink.AgroLink.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class WhatsAppService {

    @Value("${TWILIO_ACCOUNT_SID}")
    private String accountSid;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String authToken;

    @Value("${TWILIO_WHATSAPP_FROM}")
    private String whatsappFrom;

    private final RestTemplate restTemplate = new RestTemplate();

    private void enviar(String toPhone, String mensaje) {
        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(accountSid, authToken);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("To", "whatsapp:" + normalizarTelefono(toPhone));
            body.add("From", whatsappFrom);
            body.add("Body", mensaje);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("WhatsApp enviado correctamente a: " + toPhone);
        } catch (Exception e) {
            System.err.println("Error al enviar WhatsApp: " + e.getMessage());
        }
    }

    /**
     * Antepone el código de país de Perú (+51) si el número no viene ya en formato internacional.
     */
    private String normalizarTelefono(String telefono) {
        if (telefono == null) {
            return "";
        }
        String limpio = telefono.replaceAll("[\\s-]", "");
        return limpio.startsWith("+") ? limpio : "+51" + limpio;
    }

    public void sendCancelacionPedidoPorStock(String toPhone, String nombreComprador,
                                              Long idPedido, String producto, String motivo) {
        String mensaje = String.format(
                "Hola %s, tu pedido #%d (%s) en AgroLink fue cancelado por falta de disponibilidad. Motivo: %s",
                nombreComprador, idPedido, producto, motivo);
        enviar(toPhone, mensaje);
    }

    public void sendConfirmacionPedido(String toPhone, String nombreComprador, Long idPedido,
                                       String resumenProductos, BigDecimal total) {
        String mensaje = String.format(
                "Hola %s, confirmamos tu pedido #%d en AgroLink. Productos: %s. Total: S/ %.2f. ¡Gracias por tu compra!",
                nombreComprador, idPedido, resumenProductos, total);
        enviar(toPhone, mensaje);
    }
}
