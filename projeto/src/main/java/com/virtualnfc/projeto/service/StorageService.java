package com.virtualnfc.projeto.service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@Service
public class StorageService {

    private final String bucketName;
    private final String publicBaseUrl;
    private final S3Client s3Client;

    public StorageService(
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${aws.s3.endpoint}") String endpoint,
            @Value("${aws.access.key}") String accessKey,
            @Value("${aws.secret.key}") String secretKey,
            @Value("${aws.s3.region}") String region) {
        
        this.bucketName = bucketName;
        this.publicBaseUrl = "https://" + bucketName + "." + region + ".digitaloceanspaces.com/";

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
    }

    public String fazerUpload(MultipartFile arquivo) {
        String pasta = "produtos-loja/";
        String nomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(pasta + nomeArquivo)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(arquivo.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(arquivo.getInputStream(), arquivo.getSize()));

            return publicBaseUrl + pasta + nomeArquivo;

        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar arquivo para DigitalOcean: " + e.getMessage());
        }
    }

    public void deletarArquivo(String urlPublica) {
        try {
            String nomeArquivo = urlPublica.substring(urlPublica.lastIndexOf("/") + 1);
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(nomeArquivo)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            System.err.println("Erro ao deletar no Spaces: " + e.getMessage());
        }
    }
    public String getPublicBaseUrl() {
        return this.publicBaseUrl;
    }
}