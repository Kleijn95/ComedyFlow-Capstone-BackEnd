package it.epicode.ComedyFlow.utenti.spettatori;

import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.common.CommonResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/spettatori")
public class SpettatoreController {

    @Autowired
    private SpettatoreService spettatoreService;

    // ✅ Registrazione di uno spettatore (aperta)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse create(@RequestBody @Valid SpettatoreRequest request) throws MessagingException {
        return spettatoreService.create(request);
    }

    // 🔎 Visualizza tutti (solo admin)
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<SpettatoreResponse> findAll(@AuthenticationPrincipal AppUser user) {
        return spettatoreService.findAll(user);
    }

    // 🔎 Per ID (solo admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public SpettatoreResponse findById(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        return spettatoreService.findById(id, user);
    }

    // ✏️ Modifica spettatore (admin o sé stesso)
    @PutMapping("/{id}")
    public SpettatoreResponse update(@PathVariable Long id,
                                     @RequestBody @Valid SpettatoreUpdateRequest request,
                                     @AuthenticationPrincipal AppUser user) {
        return spettatoreService.update(id, request, user);
    }

    @PutMapping("/{id}/avatar")
    public ResponseEntity<Void> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AppUser user) {

        spettatoreService.uploadAvatar(id, file, user);
        return ResponseEntity.ok().build();
    }

    // ❌ Cancella spettatore (admin o sé stesso)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        spettatoreService.delete(id, user);
    }
}
