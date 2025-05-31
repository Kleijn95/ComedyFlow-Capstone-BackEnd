package it.epicode.ComedyFlow.common;

import it.epicode.ComedyFlow.auth.AppUser;

import it.epicode.ComedyFlow.auth.AppUserRepository;
import it.epicode.ComedyFlow.utenti.spettatori.Spettatore;
import it.epicode.ComedyFlow.utenti.spettatori.SpettatoreRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private SpettatoreRepository spettatoreRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @PostMapping("/contatta-locale")
    @PreAuthorize("hasAnyRole('ROLE_SPETTATORE', 'ROLE_COMICO', 'ROLE_LOCALE')")
    public ResponseEntity<String> contattaLocale(@RequestBody ContattaLocaleRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Utente non autenticato");
        }

        AppUser user = appUserRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));

        try {
            String mittente = user.getUsername();

            Spettatore spettatore = spettatoreRepository.findByAppUserUsername(user.getUsername());
            if (spettatore != null) {
                mittente = spettatore.getNome() + " " + spettatore.getCognome() + " (" + user.getUsername() + ")";
            }

            String subject = "Richiesta da un utente di ComedyFlow";
            String body = String.format("""
                Hai ricevuto una nuova richiesta da %s:
                
                %s
                """, mittente, request.getMessaggio());

            emailSenderService.sendEmail(request.getEmailLocale(), subject, body);
            return ResponseEntity.ok("Email inviata con successo");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Errore durante l'invio dell'email: " + e.getMessage());
        }
    }
    @PostMapping("/contatta-admin")
    @PreAuthorize("hasAnyRole('ROLE_LOCALE', 'ROLE_COMICO', 'ROLE_SPETTATORE')")
    public ResponseEntity<String> contattaAdmin(@RequestBody ContattaAdminRequest request,
                                                @AuthenticationPrincipal AppUser user) {
        if (user == null) {
            return ResponseEntity.status(401).body("Utente non autenticato");
        }

        String mittente = user.getUsername();

        String subject = "Richiesta da un utente di ComedyFlow";
        String body = String.format("""
            Hai ricevuto una nuova richiesta da %s:

            %s
            """, mittente, request.getMessaggio());

        try {
            emailSenderService.sendEmail("antoniokleijn@gmail.com", subject, body);
            return ResponseEntity.ok("Email inviata all'admin");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Errore invio email: " + e.getMessage());
        }
    }

}