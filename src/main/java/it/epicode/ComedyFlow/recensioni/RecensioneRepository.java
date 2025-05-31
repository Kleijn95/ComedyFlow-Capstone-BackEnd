package it.epicode.ComedyFlow.recensioni;


import it.epicode.ComedyFlow.eventi.Evento;
import it.epicode.ComedyFlow.utenti.spettatori.Spettatore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecensioneRepository extends JpaRepository<Recensione, Long> {
    List<Recensione> findByEvento_Comico_IdAndTipo(Long comicoId, TipoRecensione tipo);
    List<Recensione> findByEvento_Locale_IdAndTipo(Long localeId, TipoRecensione tipo);
    boolean existsByEventoAndAutoreAndTipo(Evento evento, Spettatore autore, TipoRecensione tipo);
    List<Recensione> findByAutore(Spettatore autore);

}
