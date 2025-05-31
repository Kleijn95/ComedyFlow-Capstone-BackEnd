package it.epicode.ComedyFlow.utenti;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByAppUserUsername(String username);

}