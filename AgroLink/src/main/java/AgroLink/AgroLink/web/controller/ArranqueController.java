package AgroLink.AgroLink.web.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ArranqueController {

    @GetMapping("/home")
    public String mostrarHome() {
        return getMensaje("Bienvenido a AgroLink"); // Enseña el mensaje
    }

    @GetMapping("/cultivos")
    public List<String> mostrarCultivos() {
        return getLista("cultivos"); //Ahora cargan los cultivos
    }

    @GetMapping("/productos")
    public List<String> mostrarProductos() {
        return getLista("productos"); // Muestra los productos
    }

    @GetMapping("/pedidos")
    public List<String> mostrarPedidos() {
        return getLista("pedidos"); // Muestra los pedidos
    }

    private String getMensaje(String mensaje) {
        return mensaje;
    }

    private List<String> getLista(String tipo) {

        if (tipo.equals("cultivos")) {
            return Arrays.asList("Papa", "Maíz");
        }

        if (tipo.equals("productos")) {
            return Arrays.asList("Semillas", "Fertilizantes");
        }

        return Arrays.asList("Pedido 1", "Pedido 2");
    }
}

