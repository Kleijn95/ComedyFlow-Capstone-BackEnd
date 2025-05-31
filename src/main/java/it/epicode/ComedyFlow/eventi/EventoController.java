package it.epicode.ComedyFlow.eventi;


import it.epicode.ComedyFlow.auth.AppUserRepository;
import it.epicode.ComedyFlow.common.CommonResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/eventi")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private AppUserRepository appUserRepository;



    @PutMapping("/{id}/annulla")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LOCALE')")
    public ResponseEntity<CommonResponse> annullaEvento(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = appUserRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"))
                .getId();

        eventoService.annullaEvento(id, userId);
        return ResponseEntity.ok(new CommonResponse(id));
    }



    // 🔎 Tutti possono vedere la lista degli eventi
    @GetMapping
    public Page<EventoResponse> getAllEventi(
            @RequestParam(required=false) Long idUtente,
            @RequestParam(required = false) String prov,
            @RequestParam(required = false) String comico,
            @RequestParam(required = false) Long comicoId,
            @RequestParam(required = false) Long localeId,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sortBy) {

        String[] sortParams = sortBy.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        EventoFilterDto filter = new EventoFilterDto();
        filter.setIdUtente(idUtente);
        filter.setProvincia(prov);
        filter.setComico(comico);
        filter.setData(data);
        filter.setComicoId(comicoId);
        filter.setLocaleId(localeId); // 👈 aggiungi questo

        return eventoService.filterEvento(filter, pageable);
    }



    // 🔎 Tutti possono vedere un evento per ID
    @GetMapping("/{id}")
    public EventoResponse getEventoById(@PathVariable Long id) {
        return eventoService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LOCALE')")
    @ResponseStatus(HttpStatus.CREATED)
    public EventoResponse createEvento(@RequestBody @Valid EventoRequest request) {
        return eventoService.create(request);
    }


    // ✏️ Solo admin può aggiornare eventi
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LOCALE')")
    public EventoResponse updateEvento(
            @PathVariable Long id,
            @RequestBody @Valid EventoRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = appUserRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"))
                .getId();

        return eventoService.update(id, request, userId);
    }


    // ❌ Solo admin può eliminare eventi
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvento(@PathVariable Long id) {
        eventoService.delete(id);
    }
}

