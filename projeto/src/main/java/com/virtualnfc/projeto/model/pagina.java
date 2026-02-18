package com.virtualnfc.projeto.model;
import java.time.LocalDateTime;
import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "pagina")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class pagina {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.serialKey = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    

    private String nomeCartao;
    private String instagram;
    private String whatsapp;
    private String facebook;
    private String linkedin;
    private String tiktok;
    private String youtube;
    private String site;
    private LocalDateTime createdAt;
    @Column(name = "serial_key", nullable = false, unique = true, length = 12)
    private String serialKey;
    private String logo;
    private String background;
    private String logoBackground;
    private String typePage;
    private String Email;
    private String spotify;
    private String maps;
    private String especialidade;
    private String registroProfissional;
    private String convenio;
    private String chavePix;
}
