package it.epicode.ComedyFlow.utenti.comici;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComicoRepository extends JpaRepository<Comico, Long> {
    Optional<Comico> findByNomeIgnoreCase(String nome);
}
