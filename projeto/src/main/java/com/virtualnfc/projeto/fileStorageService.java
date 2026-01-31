package com.virtualnfc.projeto;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.UUID;

@Service
public class fileStorageService {

    public String uploadFile(MultipartFile file) {
        try {
            String spaceName = "virtualnfcbucket";
            String regionStr = "sfo3";
            String endpoint = "https://" + regionStr + ".digitaloceanspaces.com";
            String fileName = "uploads/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_WEST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("DO00MVPPJNJ8MEQH4CN9", "k/YrrPeiTtIlsVNX7JslzM51j1oRD3JHAnDTJuLWIMc")))
                .build();

            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(spaceName)
                    .key(fileName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(file.getContentType())
                    .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
                    
            return "https://" + spaceName + "." + regionStr + ".digitaloceanspaces.com/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao subir arquivo para DigitalOcean", e);
        }
    }
    public void deleteFile(String fileUrl) {
    try {
        String spaceName = "virtualnfcbucket";
        // Extrai a key da URL (ex: uploads/nome-do-arquivo.jpg)
        // A URL é https://bucket.regiao.digitaloceanspaces.com/uploads/arquivo.jpg
        String key = fileUrl.substring(fileUrl.indexOf("uploads/"));

        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create("https://sfo3.digitaloceanspaces.com"))
                .region(Region.US_WEST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("DO00MVPPJNJ8MEQH4CN9", "k/YrrPeiTtIlsVNX7JslzM51j1oRD3JHAnDTJuLWIMc")))
                .build();

        s3Client.deleteObject(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                .bucket(spaceName)
                .key(key)
                .build());
    } catch (Exception e) {
        // Logamos o erro, mas não travamos o processo se a deleção falhar
        System.err.println("Erro ao deletar arquivo antigo: " + e.getMessage());
    }
}
}