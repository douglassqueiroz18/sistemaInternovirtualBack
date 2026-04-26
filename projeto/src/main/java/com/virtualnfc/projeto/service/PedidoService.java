package com.virtualnfc.projeto.service;

import com.virtualnfc.projeto.dto.ItemPedidoDto;
import com.virtualnfc.projeto.dto.PedidoDto;
import com.virtualnfc.projeto.entity.Produto;
import com.virtualnfc.projeto.repository.ProdutoRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PedidoService {

    private final String PAGBANK_URL = "https://sandbox.api.pagseguro.com/checkouts";
    private final String TOKEN = "060909b0-be80-492a-9ac4-059a44184d86eb0ad1d04fa5952c81cb3827712bd6d390a6-43fd-4d0b-a40e-de3494b12267";

    private final ProdutoRepository produtoRepository;

    public PedidoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }
    public String gerarLinkPagamento(PedidoDto pedidoDto) {
        List<Map<String, Object>> itensValidados = new ArrayList<>();

        for (ItemPedidoDto itemEnviado : pedidoDto.itens()) {
            Long produtoId = Long.parseLong(itemEnviado.id());
            Produto produtoNoBanco = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));
            BigDecimal precoSeguro = produtoNoBanco.getPreco();
            if (itemEnviado.quantidade() == null || itemEnviado.quantidade() <= 0) {    
                throw new RuntimeException("Quantidade inválida para o produto: " + produtoNoBanco.getNome());
            }
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", produtoNoBanco.getNome());            
            itemMap.put("quantity", itemEnviado.quantidade());
            int valorEmCentavos = precoSeguro.multiply(new BigDecimal("100")).intValue();
            itemMap.put("unit_amount", valorEmCentavos);            
            itensValidados.add(itemMap);
        }

        return chamarApiPagBank(itensValidados);
    }

    private String chamarApiPagBank(List<Map<String, Object>> itens) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);

        Map<String, Object> body = new HashMap<>();
        body.put("reference_id", "PEDIDO-" + System.currentTimeMillis());
        body.put("items", itens);
        body.put("redirect_url", "https://virtualnfc.com");
        body.put("notification_urls", List.of("https://seu-dominio.com/api/pagamentos/notificacoes"));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(PAGBANK_URL, request, Map.class);
            List<Map<String, String>> links = (List<Map<String, String>>) response.getBody().get("links");
            
            return links.stream()
                    .filter(l -> "PAY".equals(l.get("rel")))
                    .findFirst()
                    .map(l -> l.get("href"))
                    .orElseThrow(() -> new RuntimeException("Link não gerado"));
        } catch (Exception e) {
            throw new RuntimeException("Erro na API PagBank: " + e.getMessage());
        }
    }
}