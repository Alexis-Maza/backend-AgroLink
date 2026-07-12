package AgroLink.AgroLink.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsAppService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
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
}
