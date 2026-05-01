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
            @RequestPart("imagem") MultipartFile imagem) throws Exception {
    
        log.info("Recebendo requisição para criar produto: {}", produto.getNome());
        log.info("Arquivo recebido: {} ({} bytes)", imagem.getOriginalFilename(), imagem.getSize());
        
        try {
            log.info("Iniciando upload para Hetzner via porta 22...");
            String urlImagem = storageService.fazerUpload(imagem);
            log.info("Upload concluído com sucesso! URL: {}", urlImagem);
            
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

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produto) {
        log.info("Atualizando dados do produto ID: {}", id);
        return ResponseEntity.ok(service.atualizar(id, produto));
    }
}