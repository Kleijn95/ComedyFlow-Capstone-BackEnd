package it.epicode.ComedyFlow.utenti.comici;

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
@RequestMapping("/comici")
public class ComicoController {

    @Autowired
    private ComicoService comicoService;

    // 🔍 Tutti possono vedere la lista dei comici
    @GetMapping
    public List<ComicoResponse> getAllComici() {
        return comicoService.findAllComici();
    }

    // 🔍 Tutti possono vedere un comico specifico
    @GetMapping("/{id}")
    public ComicoResponse getComicoById(@PathVariable Long id) {
        return comicoService.findById(id);
    }

    // ➕ Solo admin può creare un comico
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ComicoResponse createComico(@RequestBody @Valid ComicoRequest request,
                                       @AuthenticationPrincipal AppUser user) throws MessagingException {
        return comicoService.createComico(request, user);
    }

    @PutMapping ("/{id}/avatar")
    @PreAuthorize("hasAnyRole('ROLE_COMICO', 'ROLE_ADMIN')")
    public ResponseEntity<String> uploadAvatarComico(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AppUser requester) {
        comicoService.uploadAvatar(id, file, requester);
        return ResponseEntity.ok("Avatar aggiornato con successo.");
    }


    // ✏️ Admin o comico stesso può modificare
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COMICO')")
    public ComicoResponse updateComico(@PathVariable Long id,
                                       @RequestBody @Valid ComicoRequest request,
                                       @AuthenticationPrincipal AppUser user) {
        return comicoService.updateComico(id, request, user);
    }

    // ❌ Admin o comico stesso può eliminare
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COMICO')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComico(@PathVariable Long id,
                             @AuthenticationPrincipal AppUser user) {
        comicoService.deleteComico(id, user);
    }
}
