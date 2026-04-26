package com.taller.backend.controller;

import com.taller.backend.entity.Pedido;
import com.taller.backend.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public List<Pedido> getAllPedidos() {
        return pedidoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable Long id) {
        return pedidoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Pedido createPedido(@RequestBody Pedido pedido) {
        return pedidoService.save(pedido);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> updatePedido(@PathVariable Long id, @RequestBody Pedido pedidoDetails) {
        return pedidoService.findById(id)
                .map(pedido -> {
                    pedido.setEstado(pedidoDetails.getEstado());
                    pedido.setTotal(pedidoDetails.getTotal());
                    pedido.setUsuario(pedidoDetails.getUsuario());
                    Pedido updatedPedido = pedidoService.save(pedido);
                    return ResponseEntity.ok(updatedPedido);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable Long id) {
        return pedidoService.findById(id)
                .map(pedido -> {
                    pedidoService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
