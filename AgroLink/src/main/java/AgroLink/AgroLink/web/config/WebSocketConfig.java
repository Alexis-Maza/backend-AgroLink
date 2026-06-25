package AgroLink.AgroLink.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración del broker de mensajes WebSocket (STOMP) para AgroLink.
 *
 * <p>Arquitectura de comunicación:</p>
 * <pre>
 *   Frontend (SockJS)
 *       │  CONNECT  ws://localhost:8080/ws-agrolink
 *       │
 *       ▼
 *   Spring STOMP Broker
 *       │  SUBSCRIBE  /topic/pedido/{idPedido}
 *       │
 *       ▼
 *   PedidoService.actualizarEstadoPedido()
 *       │  SimpMessagingTemplate.convertAndSend("/topic/pedido/{id}", payload)
 *       │
 *       ▼
 *   Todos los clientes suscritos reciben la notificación en tiempo real
 * </pre>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura el broker de mensajes en memoria.
     *
     * <ul>
     *   <li>{@code /topic} → prefijo para canales de broadcast (1 emisor → N suscriptores).
     *       Usado por el Timeline: {@code /topic/pedido/{idPedido}}.</li>
     *   <li>{@code /app}   → prefijo para mensajes que van al servidor (si se necesita
     *       en el futuro recibir mensajes desde el cliente vía @MessageMapping).</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broker en memoria: distribuye mensajes a todos los suscriptores del topic
        registry.enableSimpleBroker("/topic");
        // Prefijo para endpoints @MessageMapping en controllers (extensible a futuro)
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registra el endpoint WebSocket al que el Frontend se conecta.
     *
     * <ul>
     *   <li>Ruta: {@code /ws-agrolink}</li>
     *   <li>SockJS habilitado como fallback para navegadores que no soporten WS nativo.</li>
     *   <li>CORS: permite conexiones desde {@code http://localhost:3000} con credenciales.</li>
     * </ul>
     *
     * <p>El Frontend conecta con:</p>
     * <pre>
     *   const socket = new SockJS('http://localhost:8080/ws-agrolink');
     *   const client = Stomp.over(socket);
     *   client.connect({ Authorization: 'Bearer ...' }, () => {
     *       client.subscribe('/topic/pedido/42', (msg) => { ... });
     *   });
     * </pre>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-agrolink")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }
}
