package com.virtualnfc.projeto.controller;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.virtualnfc.projeto.dto.PaginaDto;
import com.virtualnfc.projeto.model.pagina;
import com.virtualnfc.projeto.repository.paginaRepository;

import jakarta.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "${FRONTEND_URL:http://89.167.42.44:4200}")

@RestController
@Slf4j
public class paginaController {

    private final paginaRepository paginaRepository;

    public paginaController(paginaRepository paginaRepository) {
        this.paginaRepository = paginaRepository;
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
            novaPagina.setChavePix(dto.getChavePix());
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
    
    // Mantém os valores de logo e logoBackground exatamente como recebidos no DTO.
    String logoNova = dto.getLogo();
    String logoBackgroundNova = dto.getLogoBackground();

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
    paginaExistente.setConvenio(dto.getConvenio());
    paginaExistente.setChavePix(dto.getChavePix());
    pagina paginaAtualizada = paginaRepository.save(paginaExistente);
    return ResponseEntity.ok(paginaAtualizada);
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
