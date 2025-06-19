package it.epicode.ComedyFlow.eventi;



import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.auth.AppUserRepository;
import it.epicode.ComedyFlow.auth.Role;
import it.epicode.ComedyFlow.common.CommonResponse;

import it.epicode.ComedyFlow.utenti.comici.Comico;
import it.epicode.ComedyFlow.utenti.comici.ComicoRepository;
import it.epicode.ComedyFlow.utenti.locali.Locale;
import it.epicode.ComedyFlow.utenti.locali.LocaleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ComicoRepository comicoRepository;

    @Autowired
    private LocaleRepository localeRepository;

    @Autowired
    private AppUserRepository appUserRepository;



    public Page<EventoResponse> filterEvento(EventoFilterDto filter, Pageable pageable) {
        Specification<Evento> spec = EventoSpecification.filterBy(filter);
        return eventoRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private void aggiornaStatoAutomaticamente(Evento evento) {
        if (evento.getStato() == StatoEvento.IN_PROGRAMMA && evento.getDataOra().isBefore(LocalDateTime.now())) {
            evento.setStato(StatoEvento.TERMINATO);
            eventoRepository.save(evento);
        }
    }
    public void annullaEvento(Long eventoId, Long userId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato con ID: " + eventoId));

        AppUser utente = appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        // Solo il locale che ha organizzato l'evento o un admin può annullarlo
        boolean isAdmin = utente.getRoles().contains(Role.ROLE_ADMIN);


        boolean isOrganizzatore = evento.getLocale().getAppUser().getId().equals(userId);

        if (!isAdmin && !isOrganizzatore) {
            throw new AccessDeniedException("Non hai i permessi per annullare questo evento");
        }

        evento.setStato(StatoEvento.ANNULLATO);
        eventoRepository.save(evento);
    }


    public Page<EventoResponse> findAll(Pageable  pageable) {
        return eventoRepository.findAll(pageable).map(this::toResponse);
    }

    public EventoResponse findById(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato con ID: " + id));
        return toResponse(evento);
    }

    public EventoResponse create(EventoRequest request) {
        Comico comico = comicoRepository.findById(request.getComicoId())
                .orElseThrow(() -> new EntityNotFoundException("Comico non trovato con ID: " + request.getComicoId()));
        Locale locale = localeRepository.findById(request.getLocaleId())
                .orElseThrow(() -> new EntityNotFoundException("Locale non trovato con ID: " + request.getLocaleId()));

        Evento evento = new Evento();
        evento.setTitolo(request.getTitolo());
        evento.setDescrizione(request.getDescrizione());
        evento.setDataOra(request.getDataOra());
        evento.setNumeroPostiTotali(request.getNumeroPostiTotali());
        evento.setNumeroPostiDisponibili(request.getNumeroPostiDisponibili());
        evento.setComico(comico);
        evento.setLocale(locale);
        evento.setLocandina(request.getLocandina());
        evento.setStato(StatoEvento.IN_PROGRAMMA); // ✅ aggiunto

        eventoRepository.save(evento);
        return toResponse(evento);
    }


    public EventoResponse update(Long id, EventoRequest request, Long userId) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato con ID: " + id));

        AppUser utente = appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        boolean isAdmin = utente.getRoles().contains(Role.ROLE_ADMIN);
        boolean isOrganizzatore = evento.getLocale().getAppUser().getId().equals(userId);

        if (!isAdmin && !isOrganizzatore) {
            throw new AccessDeniedException("Non hai i permessi per modificare questo evento");
        }

        Comico comico = comicoRepository.findById(request.getComicoId())
                .orElseThrow(() -> new EntityNotFoundException("Comico non trovato con ID: " + request.getComicoId()));

        // ⚠️ Il locale NON deve essere cambiato se non è un admin
        if (!isAdmin && !evento.getLocale().getId().equals(request.getLocaleId())) {
            throw new AccessDeniedException("Non puoi modificare il locale dell’evento");
        }

        evento.setTitolo(request.getTitolo());
        evento.setDescrizione(request.getDescrizione());
        evento.setDataOra(request.getDataOra());
        evento.setNumeroPostiTotali(request.getNumeroPostiTotali());
        evento.setNumeroPostiDisponibili(request.getNumeroPostiDisponibili());
        evento.setComico(comico);
        evento.setLocandina(request.getLocandina());


        if (isAdmin) {
            if (request.getLocaleId() == null) {
                throw new IllegalArgumentException("LocaleId non può essere null per l'admin");
            }
            Locale locale = localeRepository.findById(request.getLocaleId())
                    .orElseThrow(() -> new EntityNotFoundException("Locale non trovato con ID: " + request.getLocaleId()));
            evento.setLocale(locale);
        }


        return toResponse(eventoRepository.save(evento));
    }


    public void delete(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato con ID: " + id));
        eventoRepository.delete(evento);
    }

    private EventoResponse toResponse(Evento e) {
        EventoResponse r = new EventoResponse();
        r.setId(e.getId());
        r.setTitolo(e.getTitolo());
        r.setDataOra(e.getDataOra());
        r.setDescrizione(e.getDescrizione());
        r.setNumeroPostiTotali(e.getNumeroPostiTotali());
        r.setNumeroPostiDisponibili(e.getNumeroPostiDisponibili());
        r.setNomeLocale(e.getLocale().getNomeLocale());
        r.setNomeComico(e.getComico().getNome() + " " + e.getComico().getCognome());
        r.setComuneNome(e.getLocale().getComune().getNome()); // ✅ aggiunto
        r.setViaLocale(e.getLocale().getVia());
        r.setEmailLocale(e.getLocale().getEmail());
        r.setLocaleId(e.getLocale().getId());
        r.setStato(e.getStato()); // ✅ aggiunto
        r.setComicoId(e.getComico().getId());
        r.setLocandina(e.getLocandina());

        return r;
    }

}

