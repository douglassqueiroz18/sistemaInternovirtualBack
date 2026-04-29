package com.virtualnfc.projeto.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

@Service
public class StorageService {

    private final String host;
    private final String user;
    private final String password;
    private final String remotePath;
    private final String publicBaseUrl;

    public StorageService(
            @Value("${HETZNER_HOST}") String host,
            @Value("${HETZNER_USER}") String user,
            @Value("${HETZNER_PASS}") String password,
            @Value("${HETZNER_REMOTE_PATH}") String remotePath,
            @Value("${HETZNER_BASE_URL}") String publicBaseUrl) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.remotePath = remotePath;
        this.publicBaseUrl = publicBaseUrl;
    }

    public String fazerUpload(MultipartFile arquivo) {
        // Gera um nome único para evitar sobrescrita
        String nomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
        
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 23);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Envia o arquivo para a pasta na Hetzner
            try (InputStream inputStream = arquivo.getInputStream()) {
                channelSftp.put(inputStream, remotePath + nomeArquivo);
            }

            // Retorna a URL pública para salvar no banco
            return publicBaseUrl + nomeArquivo;

        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar arquivo para Hetzner: " + e.getMessage());
        } finally {
            if (channelSftp != null) channelSftp.disconnect();
            if (session != null) session.disconnect();
        }
    }

    public void deletarArquivo(String urlPublica) {
        String nomeArquivo = urlPublica.substring(urlPublica.lastIndexOf("/") + 1);
        
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 23);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.rm(remotePath + nomeArquivo);

        } catch (Exception e) {
            System.err.println("Erro ao deletar arquivo na Hetzner: " + e.getMessage());
        } finally {
            if (channelSftp != null) channelSftp.disconnect();
            if (session != null) session.disconnect();
        }
    }
}