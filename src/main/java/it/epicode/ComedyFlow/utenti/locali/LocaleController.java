package it.epicode.ComedyFlow.utenti.locali;

import it.epicode.ComedyFlow.auth.AppUser;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/locali")
public class LocaleController {

    @Autowired
    private LocaleService localeService;

    // 🔎 Tutti possono vedere tutti i locali
    @GetMapping
    public List<LocaleResponse> getAll() {
        return localeService.findAll();
    }

    // 🔎 Tutti possono vedere un locale per ID
    @GetMapping("/{id}")
    public LocaleResponse getById(@PathVariable Long id) {
        return localeService.findById(id);
    }

    // ✏️ Solo admin o proprietario può modificare
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_LOCALE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<LocaleResponse> update(@PathVariable Long id,
                                                 @RequestBody @Valid LocaleRequest request,
                                                 @AuthenticationPrincipal AppUser requester) {
        return ResponseEntity.ok(localeService.updateLocale(id, request, requester));
    }
    @PutMapping("/{id}/avatar")
    @PreAuthorize("hasRole('ROLE_LOCALE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> uploadAvatar(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal AppUser requester) {
        localeService.uploadAvatar(id, file, requester);
        return ResponseEntity.ok(Map.of("message", "Avatar aggiornato"));
    }
    // ❌ Solo admin o proprietario può eliminare
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_LOCALE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        localeService.deleteLocale(id, user);
    }
}
