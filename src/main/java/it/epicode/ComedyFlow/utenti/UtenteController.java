package it.epicode.ComedyFlow.utenti;

import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.common.cloudinary.CloudinaryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/utenti")
public class UtenteController {

    @Autowired
    private UtenteService utenteService;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private UtenteRepository utenteRepository;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public Utente getCurrentUserComplete(@AuthenticationPrincipal AppUser appUser) {
        return utenteService.findByUsername(appUser.getUsername());
    }
    // 🔎 Tutti gli utenti (solo admin)
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<Utente> getAll(@AuthenticationPrincipal AppUser user) {
        return utenteService.getAllUtenti(user);
    }

    // 🔎 Utente per ID (solo admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Utente getById(@PathVariable Long id) {
        return utenteService.getUtenteById(id);
    }

    // ✏️ Modifica utente (solo admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UtenteResponse update(
            @PathVariable Long id,
            @RequestBody @Valid UtenteRequest request,
            @AuthenticationPrincipal AppUser user
    ) {
        return utenteService.updateUtente(id, request, user);
    }


    // ❌ Cancella utente (solo admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        utenteService.deleteUtente(id, user);
    }

    // 📷 Carica avatar per utente autenticato
    public void uploadAvatar(long id, MultipartFile file) {
        Utente utente = utenteService.getUtenteById(id);
        utente.setAvatar(cloudinaryService.uploadImage(file));
        utenteRepository.save(utente);
    }

    // 🔎 Trova utente per username (uso interno o admin)
    @GetMapping("/by-username/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Utente getByUsername(@PathVariable String username) {
        return utenteService.findByUsername(username);
    }
}
