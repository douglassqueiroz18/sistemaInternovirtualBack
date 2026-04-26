package com.virtualnfc.projeto.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoWebhookController {

    private final JavaMailSender emailSender;

    public PagamentoWebhookController(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }
    @PostMapping("/notificacoes")
    public ResponseEntity<Void> receberNotificacao(@RequestBody Map<String, Object> payload) {
        String status = (String) payload.get("status");
        String referenceId = (String) payload.get("reference_id");

        if ("PAID".equals(status)) {
            enviarEmailNotificacao(referenceId);
        }

        return ResponseEntity.ok().build();
    }
    private void enviarEmailNotificacao(String pedidoId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("seu-email@gmail.com");
        message.setSubject("✅ Venda Aprovada - VirtualNFC");
        message.setText("O pedido " + pedidoId + " foi pago com sucesso no PagBank!");
        emailSender.send(message);
    }
}

