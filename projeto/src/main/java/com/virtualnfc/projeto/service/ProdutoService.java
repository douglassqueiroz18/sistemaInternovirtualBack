package com.virtualnfc.projeto.service;

import java.util.List;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.virtualnfc.projeto.entity.Produto;
import com.virtualnfc.projeto.repository.ProdutoRepository;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;

    @Value("${HETZNER_HOST}")
    private String host;

    @Value("${HETZNER_USER}")
    private String user;

    @Value("${HETZNER_PASS}")
    private String password;

    @Value("${HETZNER_REMOTE_PATH}")
    private String remotePath;

    @Value("${HETZNER_BASE_URL}")
    private String baseUrl;

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    public Produto buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }
    public Produto getLogoUrl() {
        return new Produto(null, "Logo", null, null, null, baseUrl + "logo.png");
    }
    public Produto atualizar(Long id, Produto produtoAtualizado) {
        return repository.findById(id)
            .map(produto -> {
                produto.setNome(produtoAtualizado.getNome());
                produto.setPreco(produtoAtualizado.getPreco());
                return repository.save(produto);
            })
            .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
    }

    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    @Transactional
    public Produto salvarComImagem(Produto produto, byte[] arquivoBytes, String nomeArquivo) {
        enviarParaHetzner(arquivoBytes, nomeArquivo);
        produto.setImagemUrl(baseUrl + nomeArquivo);
        return repository.save(produto);
    }

    @Transactional
    public void deletar(Long id) {
        Produto produto = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.getImagemUrl() != null) {
            deletarNaHetzner(produto.getImagemUrl());
        }

        repository.delete(produto);
    }

    private void enviarParaHetzner(byte[] bytes, String fileName) {
        Session session = null;
        ChannelSftp channel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            session.setPassword(password);
            
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            
            InputStream is = new ByteArrayInputStream(bytes);
            channel.put(is, remotePath + fileName);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar imagem para Hetzner: " + e.getMessage());
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    private void deletarNaHetzner(String url) {
        Session session = null;
        ChannelSftp channel = null;
        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            session.setPassword(password);
            
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.rm(remotePath + fileName);

        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível deletar arquivo na Hetzner: " + e.getMessage());
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}