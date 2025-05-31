package it.epicode.ComedyFlow.utenti.spettatori;


import org.springframework.data.jpa.repository.JpaRepository;

public interface SpettatoreRepository extends JpaRepository<Spettatore, Long> {
    Spettatore findByAppUserUsername(String username);

}