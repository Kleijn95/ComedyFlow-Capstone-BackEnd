package it.epicode.ComedyFlow.recensioni;

import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.auth.AppUserRepository;
import it.epicode.ComedyFlow.common.CommonResponse;
import it.epicode.ComedyFlow.utenti.spettatori.Spettatore;
import it.epicode.ComedyFlow.utenti.spettatori.SpettatoreRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recensioni")
public class RecensioneController {

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private AppUserRepository appUserRepository;

    @PostMapping
//    @PreAuthorize("hasRole('ROLE_SPETTATORE')")
    public CommonResponse creaRecensione(@RequestBody @Valid RecensioneRequest request,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = appUserRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));
        return recensioneService.create(request, user);
    }

    @GetMapping("/mie")
    public List<RecensioneResponse> mie(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = appUserRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));
        return recensioneService.findBySpettatore(user);
    }


    @GetMapping("/locale/{id}")
    public List<RecensioneResponse> recensioniPerLocale(@PathVariable Long id) {
        return recensioneService.findByLocale(id);
    }

    @GetMapping("/comico/{comicoId}")
    public List<RecensioneResponse> getByComicoId(
            @PathVariable Long comicoId,
            @RequestParam(required = false) String titoloEvento,
            @RequestParam(required = false) Integer votoMinimo
    ) {
        return recensioneService.getByComicoId(comicoId, titoloEvento, votoMinimo);
    }
}
