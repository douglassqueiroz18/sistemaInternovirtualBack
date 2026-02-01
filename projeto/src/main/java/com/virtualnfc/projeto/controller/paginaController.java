package com.virtualnfc.projeto.controller;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.virtualnfc.projeto.dto.PaginaDto;
import com.virtualnfc.projeto.model.pagina;
import com.virtualnfc.projeto.repository.paginaRepository;

import jakarta.transaction.Transactional;

import com.virtualnfc.projeto.*;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:4200}")

@RestController
@Slf4j
public class paginaController {

    private final paginaRepository paginaRepository;
    private final fileStorageService fileStorageService;

    // O Spring vai encontrar o arquivo FileStorageService.java e colocar aqui
    // automaticamente
    public paginaController(paginaRepository paginaRepository, fileStorageService fileStorageService) {
        this.paginaRepository = paginaRepository;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/pagina")
    public ResponseEntity<?> createPagina(@RequestBody PaginaDto dto) {
        try {
            pagina novaPagina = new pagina();
            novaPagina.setNomeCartao(dto.getNomeCartao());
            novaPagina.setInstagram(dto.getInstagram());
            novaPagina.setWhatsapp(dto.getWhatsapp());
            novaPagina.setFacebook(dto.getFacebook());
            novaPagina.setLinkedin(dto.getLinkedin());
            novaPagina.setTiktok(dto.getTiktok());
            novaPagina.setYoutube(dto.getYoutube());
            novaPagina.setSite(dto.getSite());
            novaPagina.setBackground(dto.getBackground());
            novaPagina.setTypePage(dto.getTypePage());
            novaPagina.setEmail(dto.getEmail());
            novaPagina.setSpotify(dto.getSpotify());
            novaPagina.setMaps(dto.getMaps());
            novaPagina.setEspecialidade(dto.getEspecialidade());
            novaPagina.setRegistroProfissional(dto.getRegistroProfissional());
            novaPagina.setConvenio(dto.getConvenio());
            pagina savedPagina = paginaRepository.save(novaPagina);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPagina);
        } catch (Exception e) {
            log.error("Error creating pagina: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating pagina");
        }
    }

    @GetMapping("/pagina")
    public ResponseEntity<List<pagina>> getAllPaginas() {
        List<pagina> paginas = paginaRepository.findAll();
        return ResponseEntity.ok(paginas);
    }

    @GetMapping("/pagina/{serialKey}")
    public ResponseEntity<?> getPaginaById(@PathVariable String serialKey) {

        Optional<pagina> optionalPagina = paginaRepository.findBySerialKey(serialKey);

        if (optionalPagina.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Página não encontrada");
        }

        return ResponseEntity.ok(optionalPagina.get());
    }

    @DeleteMapping("/pagina/{id}")
    public ResponseEntity<?> deletePagina(@PathVariable Long id) {

        Optional<pagina> optionalPagina = paginaRepository.findById(id);

        if (optionalPagina.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Página não encontrada");
        }

        paginaRepository.delete(optionalPagina.get());

        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.uploadFile(file);
            return ResponseEntity.ok(Collections.singletonMap("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/access/check/{serialKey}")
    public ResponseEntity<?> accessBySerial(@PathVariable String serialKey) {
        boolean exists = paginaRepository.existsBySerialKey(serialKey);

        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "valid", false));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true));
    }

    // ATUALIZAR por SerialKey
    @PutMapping("/pagina/{serialKey}")
    public ResponseEntity<?> updatePagina(
    @PathVariable String serialKey,
    @RequestBody PaginaDto dto) {

    Optional<pagina> optionalPagina = paginaRepository.findBySerialKey(serialKey);

    if (optionalPagina.isEmpty()) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Página não encontrada");
    }

    pagina paginaExistente = optionalPagina.get();
    
    // LÓGICA DE SUBSTITUIÇÃO DE IMAGEM
    String logoAntiga = paginaExistente.getLogo();
    String logoNova = dto.getLogo();
    String logoBackgroundAntiga = paginaExistente.getLogoBackground();
    String logoBackgroundNova = dto.getLogoBackground();
    // Se a nova logo é um base64 (data:image), faz upload para DigitalOcean
    if (logoNova != null && logoNova.startsWith("data:image")) {
        try {
            // Converte base64 para MultipartFile
            String fileName = "logo_" + serialKey + "_" + System.currentTimeMillis() + ".png";
            MultipartFile file = convertBase64ToMultipartFile(logoNova, fileName);
            
            //Faz o delete da logo antiga antes de subir a nova
            fileStorageService.deleteFile(logoAntiga);
            // Faz upload para DigitalOcean
            String uploadedUrl = fileStorageService.uploadFile(file);
            logoNova = uploadedUrl;
            
            // Atualiza o DTO com a URL
            dto.setLogo(uploadedUrl);
        } catch (Exception e) {
            log.error("Erro ao fazer upload da imagem base64: ", e);
            // Opção: retornar erro ou manter logo antiga
            logoNova = logoAntiga;
        }
    }

    // Se existe uma logo antiga E ela é diferente da nova que está chegando
    if (logoAntiga != null && !logoAntiga.equals(logoNova)) {
        // Se a logo nova for nula ou um link diferente, deletamos a anterior do Space
        fileStorageService.deleteFile(logoAntiga);
    }
    if(logoBackgroundNova != null && logoBackgroundNova.startsWith("data:image")){
        try {
            // Converte base64 para MultipartFile
            String fileName = "logobackground_" + serialKey + "_" + System.currentTimeMillis() + ".png";
            MultipartFile file = convertBase64ToMultipartFile(logoBackgroundNova, fileName);
            

            // Faz upload para DigitalOcean
            String uploadedUrl = fileStorageService.uploadFile(file);
            // Se o upload foi bem sucedido, deleta a ANTIGA
            if (logoBackgroundAntiga != null) {
                fileStorageService.deleteFile(logoBackgroundAntiga);
            }
            // Atualiza a variável para a nova URL
            logoBackgroundNova = uploadedUrl;
            dto.setLogoBackground(uploadedUrl);
        } catch (Exception e) {
            log.error("Erro ao fazer upload da imagem base64: ", e);
            // Opção: retornar erro ou manter logo antiga
            logoBackgroundNova = logoBackgroundAntiga;
            // NÃO deleta a antiga porque o upload da nova falhou
        }
    }else if(logoBackgroundNova == null || logoBackgroundNova.isEmpty()){
        // Se o usuário enviou null ou vazio (escolheu cor ou removeu a imagem)
        if(logoBackgroundAntiga != null){
            fileStorageService.deleteFile(logoBackgroundAntiga);
        }
        // Mantém como null ou string vazia
    }else if(logoBackgroundAntiga != null && !logoBackgroundAntiga.equals(logoBackgroundNova)) {
        // Se a logo nova for nula ou um link diferente, deletamos a anterior do Space
        if (logoBackgroundNova.startsWith("http") && !logoBackgroundNova.startsWith("data:image")) {
        // É uma URL externa, deleta a imagem antiga do nosso storage
        fileStorageService.deleteFile(logoBackgroundAntiga);
        }    
    }
    // ATUALIZA LOGO (agora com URL do DigitalOcean ou mantém a antiga)
    paginaExistente.setLogo(logoNova);
    paginaExistente.setLogoBackground(logoBackgroundNova);
    paginaExistente.setNomeCartao(dto.getNomeCartao());
    paginaExistente.setInstagram(dto.getInstagram());
    paginaExistente.setWhatsapp(dto.getWhatsapp());
    paginaExistente.setFacebook(dto.getFacebook());
    paginaExistente.setLinkedin(dto.getLinkedin());
    paginaExistente.setTiktok(dto.getTiktok());
    paginaExistente.setYoutube(dto.getYoutube());
    paginaExistente.setEmail(dto.getEmail());
    paginaExistente.setSite(dto.getSite());
    paginaExistente.setBackground(dto.getBackground());
    paginaExistente.setTypePage(dto.getTypePage());
    paginaExistente.setSpotify(dto.getSpotify());
    paginaExistente.setMaps(dto.getMaps());
    paginaExistente.setEspecialidade(dto.getEspecialidade());
    paginaExistente.setRegistroProfissional(dto.getRegistroProfissional());
    pagianaExistente.setConvenio(dto.getConvenio());

    // LogoBackground também pode precisar do mesmo tratamento
    String logoBgNova = dto.getLogoBackground();
    if (logoBgNova != null && logoBgNova.startsWith("data:image")) {
        try {
            String fileName = "logobg_" + serialKey + "_" + System.currentTimeMillis() + ".png";
            MultipartFile file = convertBase64ToMultipartFile(logoBgNova, fileName);
            String uploadedUrl = fileStorageService.uploadFile(file);
            paginaExistente.setLogoBackground(uploadedUrl);
        } catch (Exception e) {
            log.error("Erro ao fazer upload do logoBackground: ", e);
        }
    }

    pagina paginaAtualizada = paginaRepository.save(paginaExistente);
    return ResponseEntity.ok(paginaAtualizada);
}

    // Método auxiliar para converter base64 em MultipartFile
    private MultipartFile convertBase64ToMultipartFile(String base64, String fileName) {
        try {
            // Remove o prefixo "data:image/...;base64,"
            String base64Data = base64.split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Detecta o tipo MIME do base64
            String mimeType = base64.split(";")[0].split(":")[1];

            return new MultipartFile() {
                @Override
                public String getName() {
                    return "file";
                }

                @Override
                public String getOriginalFilename() {
                    return fileName;
                }

                @Override
                public String getContentType() {
                    return mimeType;
                }

                @Override
                public boolean isEmpty() {
                    return imageBytes.length == 0;
                }

                @Override
                public long getSize() {
                    return imageBytes.length;
                }

                @Override
                public byte[] getBytes() {
                    return imageBytes;
                }

                @Override
                public java.io.InputStream getInputStream() {
                    return new ByteArrayInputStream(imageBytes);
                }

                @Override
                public void transferTo(java.io.File dest) throws IllegalStateException {
                    try {
                        java.nio.file.Files.write(dest.toPath(), imageBytes);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter base64 para arquivo", e);
        }
    }
    private String gerarSerialKeyUnica() {
        String serialKey;
        do {
            serialKey = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        } while (paginaRepository.existsBySerialKey(serialKey));
        return serialKey;
    }
    @PostMapping("/pagina/batch-simples")
    @Transactional
    public ResponseEntity<?> criarPaginasRapido(@RequestBody Map<String, Integer> request) {
    try {
        Integer quantidade = request.get("quantidade");
        
        if (quantidade == null || quantidade <= 0 || quantidade > 5000) {
            return ResponseEntity.badRequest().body("Quantidade inválida (1-5000)");
        }
        
        log.info("Iniciando criação rápida de {} páginas", quantidade);
        
        List<pagina> batch = new ArrayList<>();
        List<String> serialKeys = new ArrayList<>();
        
        for (int i = 0; i < quantidade; i++) {
            pagina p = new pagina();
            

            
            // Serial key única
            String serialKey = gerarSerialKeyUnica();
            p.setSerialKey(serialKey);
            serialKeys.add(serialKey);
            
            // REMOVA estas linhas se não tiver os campos
            // p.setCreatedAt(LocalDateTime.now());
            // p.setUpdatedAt(LocalDateTime.now());
            
            batch.add(p);
        }
        
        // Salva tudo de uma vez (batch insert)
        paginaRepository.saveAll(batch);
        
        log.info("✅ {} páginas criadas com sucesso", quantidade);
        
        return ResponseEntity.ok(Map.of(
            "sucesso", true,
            "mensagem", quantidade + " páginas criadas",
            "serialKeys", serialKeys
        ));
        
    } catch (Exception e) {
        log.error("Erro: ", e);
        return ResponseEntity.status(500).body("Erro: " + e.getMessage());
    }
}
}
