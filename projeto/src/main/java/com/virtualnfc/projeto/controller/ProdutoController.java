package com.virtualnfc.projeto.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.virtualnfc.projeto.entity.Produto;
import com.virtualnfc.projeto.service.ProdutoService;
import com.virtualnfc.projeto.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;
    private final StorageService storageService;

    @GetMapping
    public List<Produto> listar() {
        return service.listarTodos();
    }
    @GetMapping("/logo")
    public ResponseEntity<Produto> obterLogo() {
        return ResponseEntity.ok(service.getLogoUrl());
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Produto> criar(
            @RequestPart("produto") Produto produto, 
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) throws Exception {
    
        log.info("Recebendo requisição para criar produto: {}", produto.getNome());
        
        try {
        String urlImagem = null;

        // Verifica se a imagem foi enviada antes de tentar usá-la
        if (imagem != null && !imagem.isEmpty()) {
            log.info("Arquivo recebido: {} ({} bytes)", imagem.getOriginalFilename(), imagem.getSize());
            log.info("Iniciando upload para Hetzner via porta 22...");
            urlImagem = storageService.fazerUpload(imagem);
            log.info("Upload concluído com sucesso! URL: {}", urlImagem);
        } else {
            log.warn("Nenhuma imagem foi enviada para o produto: {}", produto.getNome());
        }
        
        produto.setImagemUrl(urlImagem);
        Produto salvo = service.salvar(produto);
        
        log.info("Produto salvo no banco de dados com ID: {}", salvo.getId());
        return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            log.error("ERRO CRÍTICO no processo de criação: {}", e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        log.info("Solicitação para deletar produto ID: {}", id);
        Produto produto = service.buscarPorId(id);
        
        if (produto.getImagemUrl() != null) {
            log.info("Removendo imagem da Hetzner: {}", produto.getImagemUrl());
            storageService.deletarArquivo(produto.getImagemUrl());
        }
        
        service.deletar(id);
        log.info("Produto ID {} removido com sucesso.", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Produto> atualizarComImagem(
            @PathVariable Long id,
            @RequestPart("produto") Produto produto,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) throws Exception {
        
        log.info("Requisição de atualização completa para o ID: {}", id);
        
        // 1. Se veio imagem nova, faz o upload e atualiza a URL no objeto
        if (imagem != null && !imagem.isEmpty()) {
            String novaUrl = storageService.fazerUpload(imagem);
            produto.setImagemUrl(novaUrl);
        }
        
        // 2. Chama o service para salvar as mudanças
        return ResponseEntity.ok(service.atualizar(id, produto));
    }
}