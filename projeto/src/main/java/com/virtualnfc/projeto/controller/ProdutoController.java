package com.virtualnfc.projeto.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.virtualnfc.projeto.entity.Produto;
import com.virtualnfc.projeto.service.ProdutoService;
import com.virtualnfc.projeto.service.StorageService;
import lombok.RequiredArgsConstructor;

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
    
    String urlImagem = storageService.fazerUpload(imagem);
    
    produto.setImagemUrl(urlImagem);
    
    return ResponseEntity.ok(service.salvar(produto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        Produto produto = service.buscarPorId(id);
        
        if (produto.getImagemUrl() != null) {
            storageService.deletarArquivo(produto.getImagemUrl());
        }
        
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produto) {
        return ResponseEntity.ok(service.atualizar(id, produto));
    }
}