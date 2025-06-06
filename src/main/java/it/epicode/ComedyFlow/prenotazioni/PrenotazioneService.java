package it.epicode.ComedyFlow.prenotazioni;

import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.common.CommonResponse;
import it.epicode.ComedyFlow.eventi.Evento;
import it.epicode.ComedyFlow.eventi.EventoRepository;
import it.epicode.ComedyFlow.eventi.StatoEvento;
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
public class PrenotazioneService {

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private SpettatoreRepository spettatoreRepository;

    public List<PrenotazioneResponse> findAll() {
        return prenotazioneRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PrenotazioneResponse findById(Long id) {
        Prenotazione p = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata con ID: " + id));
        return toResponse(p);
    }

    @Transactional
    public CommonResponse create(PrenotazioneRequest request) {
        Evento evento = eventoRepository.findById(request.getEventoId())
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato con ID: " + request.getEventoId()));

        // ⛔ Blocca prenotazioni se l'evento è annullato o terminato
        if (evento.getStato() == StatoEvento.ANNULLATO || evento.getStato() == StatoEvento.TERMINATO
                || evento.getDataOra().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Non puoi prenotare un evento annullato o terminato.");
        }

        if (request.getNumeroPostiPrenotati() > 5) {
            throw new IllegalArgumentException("Non puoi prenotare più di 5 posti.");
        }

        if (evento.getNumeroPostiDisponibili() < request.getNumeroPostiPrenotati()) {
            throw new IllegalArgumentException("Posti insufficienti per questo evento.");
        }

        Spettatore spettatore = spettatoreRepository.findById(request.getSpettatoreId())
                .orElseThrow(() -> new EntityNotFoundException("Spettatore non trovato con ID: " + request.getSpettatoreId()));

        Prenotazione p = new Prenotazione();
        p.setEvento(evento);
        p.setSpettatore(spettatore);
        p.setNumeroPostiPrenotati(request.getNumeroPostiPrenotati());

        evento.setNumeroPostiDisponibili(evento.getNumeroPostiDisponibili() - request.getNumeroPostiPrenotati());

        prenotazioneRepository.save(p);
        eventoRepository.save(evento);

        return new CommonResponse(p.getId());
    }


    @Transactional
    public CommonResponse prenotaEvento(Long eventoId, int numeroPosti, AppUser spettatoreAutenticato) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato con ID: " + eventoId));

        // ⛔ Blocca prenotazioni se l'evento è annullato o terminato
        if (evento.getStato() == StatoEvento.ANNULLATO || evento.getStato() == StatoEvento.TERMINATO
                || evento.getDataOra().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Non puoi prenotare un evento annullato o terminato.");
        }

        if (numeroPosti > 5) {
            throw new IllegalArgumentException("Non puoi prenotare più di 5 posti.");
        }

        if (evento.getNumeroPostiDisponibili() < numeroPosti) {
            throw new IllegalArgumentException("Posti insufficienti per questo evento.");
        }

        Spettatore spettatore = spettatoreRepository.findByAppUserUsername(spettatoreAutenticato.getUsername());
        if (spettatore == null) {
            throw new EntityNotFoundException("Spettatore non trovato per l'utente corrente.");
        }

        Prenotazione p = new Prenotazione();
        p.setEvento(evento);
        p.setSpettatore(spettatore);
        p.setNumeroPostiPrenotati(numeroPosti);

        evento.setNumeroPostiDisponibili(evento.getNumeroPostiDisponibili() - numeroPosti);

        prenotazioneRepository.save(p);
        eventoRepository.save(evento);

        return new CommonResponse(p.getId());
    }



    @Transactional
    public void annullaPrenotazione(Long prenotazioneId, AppUser requester) {
        aggiornaStatiEventi();
        Prenotazione p = prenotazioneRepository.findById(prenotazioneId)
                .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata con ID: " + prenotazioneId));

        Evento evento = p.getEvento();

        if (evento.getStato() == StatoEvento.TERMINATO) {
            throw new IllegalStateException("Non è possibile annullare una prenotazione per un evento terminato.");
        }

        boolean isAdmin = requester.getRoles().contains(it.epicode.ComedyFlow.auth.Role.ROLE_ADMIN);
        boolean isOwner = p.getSpettatore().getAppUser().getId().equals(requester.getId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Non hai i permessi per annullare questa prenotazione.");
        }

        evento.setNumeroPostiDisponibili(evento.getNumeroPostiDisponibili() + p.getNumeroPostiPrenotati());

        prenotazioneRepository.delete(p);
        eventoRepository.save(evento);
    }

    @Transactional
    public CommonResponse modificaPrenotazione(Long prenotazioneId, int nuovoNumeroPosti, AppUser requester) {
        aggiornaStatiEventi();

        Prenotazione prenotazione = prenotazioneRepository.findById(prenotazioneId)
                .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata con ID: " + prenotazioneId));

        Evento evento = prenotazione.getEvento();

        if (evento.getStato() == StatoEvento.TERMINATO) {
            throw new IllegalStateException("Non è possibile modificare una prenotazione per un evento terminato.");
        }

        if (nuovoNumeroPosti > 5 || nuovoNumeroPosti < 1) {
            throw new IllegalArgumentException("Il numero di posti deve essere compreso tra 1 e 5.");
        }

        boolean isAdmin = requester.getRoles().contains(it.epicode.ComedyFlow.auth.Role.ROLE_ADMIN);
        boolean isOwner = prenotazione.getSpettatore().getAppUser().getId().equals(requester.getId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Non hai i permessi per modificare questa prenotazione.");
        }

        int postiAttuali = prenotazione.getNumeroPostiPrenotati();
        int differenza = nuovoNumeroPosti - postiAttuali;

        if (differenza > 0 && evento.getNumeroPostiDisponibili() < differenza) {
            throw new IllegalArgumentException("Non ci sono abbastanza posti disponibili.");
        }

        prenotazione.setNumeroPostiPrenotati(nuovoNumeroPosti);
        evento.setNumeroPostiDisponibili(evento.getNumeroPostiDisponibili() - differenza);

        prenotazioneRepository.save(prenotazione);
        eventoRepository.save(evento);

        return new CommonResponse(prenotazione.getId());
    }


    @Transactional
    public void delete(Long id, AppUser requester) {
        Prenotazione p = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata con ID: " + id));

        boolean isAdmin = requester.getRoles().contains(it.epicode.ComedyFlow.auth.Role.ROLE_ADMIN);
        boolean isOwner = p.getSpettatore().getAppUser().getId().equals(requester.getId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Non hai i permessi per eliminare questa prenotazione.");
        }

        Evento evento = p.getEvento();
        evento.setNumeroPostiDisponibili(evento.getNumeroPostiDisponibili() + p.getNumeroPostiPrenotati());

        prenotazioneRepository.delete(p);
        eventoRepository.save(evento);
    }

    public List<PrenotazioneResponse> findBySpettatore(AppUser spettatore) {
        return prenotazioneRepository.findAll().stream()
                .filter(p -> p.getSpettatore().getAppUser().getId().equals(spettatore.getId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    private void aggiornaStatiEventi() {
        List<Evento> eventi = eventoRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Evento evento : eventi) {
            if (evento.getStato() != StatoEvento.ANNULLATO && evento.getDataOra().plusHours(3).isBefore(now)) {
                evento.setStato(StatoEvento.TERMINATO);
                eventoRepository.save(evento);
            }
        }
    }

    public List<PrenotazioneResponse> getPrenotazioniByEvento(Long eventoId) {
        List<Prenotazione> lista = prenotazioneRepository.findByEvento_Id(eventoId);
        return lista.stream().map(this::toResponse).toList();
    }

    private PrenotazioneResponse toResponse(Prenotazione p) {
        PrenotazioneResponse r = new PrenotazioneResponse();
        r.setId(p.getId());
        r.setNumeroPostiPrenotati(p.getNumeroPostiPrenotati());
        r.setDataOraPrenotazione(p.getDataOraPrenotazione());
        r.setNomeSpettatore(p.getSpettatore().getNome() + " " + p.getSpettatore().getCognome());
        r.setTitoloEvento(p.getEvento().getTitolo());
        r.setNomeLocale(p.getEvento().getLocale().getNomeLocale());
        r.setDataOraEvento(p.getEvento().getDataOra());
        r.setStatoEvento(p.getEvento().getStato() != null ? p.getEvento().getStato() : StatoEvento.IN_PROGRAMMA);
        r.setEventoId(p.getEvento().getId());
r.setAvatar(p.getSpettatore().getAvatar());
        return r;
    }


}
