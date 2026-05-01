package com.virtualnfc.projeto.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.virtualnfc.projeto.entity.Produto;
import com.virtualnfc.projeto.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;
    private final StorageService storageService;

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    public Produto buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }


    public Produto getLogoUrl() {
        String urlLogo = storageService.getPublicBaseUrl() + "logo.png"; 
        return new Produto(null, "Logo", null, null, null, urlLogo);
    }

    public Produto atualizar(Long id, Produto produtoAtualizado) {
        return repository.findById(id)
            .map(produto -> {
                produto.setNome(produtoAtualizado.getNome());
                produto.setPreco(produtoAtualizado.getPreco());
                if (produtoAtualizado.getImagemUrl() != null) {
                produto.setImagemUrl(produtoAtualizado.getImagemUrl());
            }
                return repository.save(produto);
            })
            .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
    }

    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    @Transactional
    public void deletar(Long id) {
        Produto produto = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.getImagemUrl() != null) {
            storageService.deletarArquivo(produto.getImagemUrl());
        }

        repository.delete(produto);
    }
}