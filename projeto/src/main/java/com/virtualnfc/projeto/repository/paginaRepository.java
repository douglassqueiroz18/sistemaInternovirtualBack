package com.virtualnfc.projeto.repository;

import com.virtualnfc.projeto.model.pagina;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface paginaRepository extends JpaRepository<pagina, Long> {


Optional<pagina> findBySerialKey(String serialKey);


boolean existsBySerialKey(String serialKey);
}
