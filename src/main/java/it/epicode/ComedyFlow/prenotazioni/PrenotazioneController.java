package it.epicode.ComedyFlow.prenotazioni;

import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.common.CommonResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prenotazioni")
public class PrenotazioneController {

    @Autowired
    private PrenotazioneService prenotazioneService;

    // 🔎 Admin: tutte le prenotazioni
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<PrenotazioneResponse> getAll() {
        return prenotazioneService.findAll();
    }

    // 🔎 Admin: prenotazione per ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PrenotazioneResponse getById(@PathVariable Long id) {
        return prenotazioneService.findById(id);
    }

    // 🔍 Spettatore autenticato: le sue prenotazioni
    @GetMapping("/mie")
    @PreAuthorize("hasRole('ROLE_SPETTATORE')")
    public List<PrenotazioneResponse> miePrenotazioni(@AuthenticationPrincipal AppUser spettatore) {
        return prenotazioneService.findBySpettatore(spettatore);
    }

    // ✅ Admin: crea nuova prenotazione per spettatore specificato
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse create(@RequestBody @Valid PrenotazioneRequest request) {
        return prenotazioneService.create(request);
    }

    // ✅ Spettatore: prenota evento
    @PostMapping("/evento/{eventoId}")
    @PreAuthorize("hasRole('ROLE_SPETTATORE')")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse prenotaEvento(@PathVariable Long eventoId,
                                        @RequestParam int numeroPosti,
                                        @AuthenticationPrincipal AppUser spettatore) {
        return prenotazioneService.prenotaEvento(eventoId, numeroPosti, spettatore);
    }

    // ❌ Spettatore: annulla la propria prenotazione
    @DeleteMapping("/annulla/{id}")
    @PreAuthorize("hasRole('ROLE_SPETTATORE')")
    public void annulla(@PathVariable Long id, @AuthenticationPrincipal AppUser spettatore) {
        prenotazioneService.annullaPrenotazione(id, spettatore);
    }

    @PutMapping("/modifica/{id}")
    @PreAuthorize("hasRole('ROLE_SPETTATORE')")
    public CommonResponse modificaPrenotazione(
            @PathVariable Long id,
            @RequestParam int nuovoNumeroPosti,
            @AuthenticationPrincipal AppUser user) {
        return prenotazioneService.modificaPrenotazione(id, nuovoNumeroPosti, user);
    }


    // ❌ Admin o owner: cancella prenotazione
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SPETTATORE')")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        prenotazioneService.delete(id, user);
    }
    @GetMapping("/evento/{eventoId}2")
    public ResponseEntity<List<PrenotazioneResponse>> getPrenotazioniByEvento(@PathVariable Long eventoId) {
        List<PrenotazioneResponse> prenotazioni = prenotazioneService.getPrenotazioniByEvento(eventoId);
        return ResponseEntity.ok(prenotazioni);
    }

    @GetMapping("/evento/{eventoId}/partecipanti")
//    @PreAuthorize("hasRole('ROLE_LOCALE')") // opzionale: limita l’accesso
    public ResponseEntity<List<PrenotazioneResponse>> getPartecipantiEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(prenotazioneService.getPrenotazioniByEvento(eventoId));
    }

}
