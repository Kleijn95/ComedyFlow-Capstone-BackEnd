package it.epicode.ComedyFlow.recensioni;

import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.common.CommonResponse;
import it.epicode.ComedyFlow.eventi.Evento;
import it.epicode.ComedyFlow.eventi.EventoRepository;
import it.epicode.ComedyFlow.utenti.spettatori.Spettatore;
import it.epicode.ComedyFlow.utenti.spettatori.SpettatoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecensioneService {

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private SpettatoreRepository spettatoreRepository;

    public List<RecensioneResponse> findByComico(Long comicoId) {
        return recensioneRepository.findByEvento_Comico_IdAndTipo(comicoId, TipoRecensione.COMICO)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RecensioneResponse> findByLocale(Long localeId) {
        return recensioneRepository.findByEvento_Locale_IdAndTipo(localeId, TipoRecensione.LOCALE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommonResponse create(RecensioneRequest request, AppUser user) {
        Evento evento = eventoRepository.findById(request.getEventoId())
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato"));

        if (!evento.getStato().name().equals("TERMINATO")) {
            throw new IllegalStateException("Puoi recensire solo eventi con stato TERMINATO.");
        }

        Spettatore spettatore = spettatoreRepository.findByAppUserUsername(user.getUsername());
        if (spettatore == null) {
            throw new EntityNotFoundException("Spettatore non trovato");
        }

        boolean haPrenotato = evento.getPrenotazioni().stream()
                .anyMatch(p -> p.getSpettatore().getId().equals(spettatore.getId()));

        if (!haPrenotato) {
            throw new IllegalStateException("Puoi recensire solo eventi a cui hai partecipato.");
        }

        boolean giaRecensito = recensioneRepository.existsByEventoAndAutoreAndTipo(evento, spettatore, request.getTipo());
        if (giaRecensito) {
            throw new IllegalStateException("Hai già recensito questo evento.");
        }

        Recensione r = new Recensione();
        r.setEvento(evento);
        r.setAutore(spettatore);
        r.setContenuto(request.getContenuto());
        r.setVoto(request.getVoto());
        r.setTipo(request.getTipo());
        r.setData(LocalDateTime.now());

        recensioneRepository.save(r);
        return new CommonResponse(r.getId());
    }

    public List<RecensioneResponse> getByComicoId(Long comicoId, String titoloEvento, Integer votoMinimo) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getEvento().getComico().getId().equals(comicoId))
                .filter(r -> titoloEvento == null || r.getEvento().getTitolo().toLowerCase().contains(titoloEvento.toLowerCase()))
                .filter(r -> votoMinimo == null || r.getVoto() >= votoMinimo)
                .map(this::toResponse)
                .toList();
    }

    public List<RecensioneResponse> findBySpettatore(AppUser user) {
        Spettatore spettatore = spettatoreRepository.findByAppUserUsername(user.getUsername());
        return recensioneRepository.findByAutore(spettatore).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    private RecensioneResponse toResponse(Recensione r) {
        RecensioneResponse res = new RecensioneResponse();
        res.setId(r.getId());
        res.setContenuto(r.getContenuto());
        res.setVoto(r.getVoto());
        res.setAutore(r.getAutore().getNome() + " " + r.getAutore().getCognome());
        res.setEventoId(r.getEvento().getId());
        res.setTitoloEvento(r.getEvento().getTitolo());
        res.setTipo(r.getTipo());
        res.setData(r.getData());
        return res;
    }
}
