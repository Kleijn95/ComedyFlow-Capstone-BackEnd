package it.epicode.ComedyFlow.prenotazioni;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrenotazioneRepository extends JpaRepository<Prenotazione, Long> {
    List<Prenotazione> findByEvento_Id(Long eventoId);

}