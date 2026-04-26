package com.virtualnfc.projeto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.virtualnfc.projeto.dto.PedidoDto;
import com.virtualnfc.projeto.service.PedidoService;

import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<?> criarCheckout(@RequestBody PedidoDto pedidoDto) {
        try {
            String paymentLink = pedidoService.gerarLinkPagamento(pedidoDto);
            
            return ResponseEntity.ok(Map.of("paymentLink", paymentLink));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao processar pedido: " + e.getMessage());
        }
    }
}