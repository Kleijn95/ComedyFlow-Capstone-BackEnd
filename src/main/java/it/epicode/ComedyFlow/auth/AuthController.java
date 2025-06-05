package it.epicode.ComedyFlow.auth;

import it.epicode.ComedyFlow.common.CommonResponse;
import it.epicode.ComedyFlow.common.EmailSenderService;
import it.epicode.ComedyFlow.indirizzi.ComuneRepository;
import it.epicode.ComedyFlow.utenti.PendingUserData;
import it.epicode.ComedyFlow.utenti.Utente;
import it.epicode.ComedyFlow.utenti.UtenteRepository;
import it.epicode.ComedyFlow.utenti.comici.Comico;
import it.epicode.ComedyFlow.utenti.comici.ComicoRepository;
import it.epicode.ComedyFlow.utenti.locali.Locale;
import it.epicode.ComedyFlow.utenti.locali.LocaleRepository;
import it.epicode.ComedyFlow.utenti.spettatori.*;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;
    @Autowired
    private SpettatoreService spettatoreService;

    @Autowired
    private SpettatoreRepository spettatoreRepository;

    @Autowired
    private ComicoRepository comicoRepository;

    @Autowired
    private LocaleRepository localeRepository;

    @Autowired
    private ComuneRepository comuneRepository; // solo se ti serve per il Locale

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private PendingUserDataRepository pendingUserDataRepository;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current-user")
    public AppUser getCurrentUser(@AuthenticationPrincipal AppUser appUser) {
        return appUser;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse> registerSpettatore(@RequestBody @Valid SpettatoreRequest request) throws MessagingException {
        Role ruoloRichiesto = request.getRuoloRichiesto() != null ? request.getRuoloRichiesto() : Role.ROLE_SPETTATORE;

        AppUser user = appUserService.registerUser(request);


        // Invia email di verifica
        String link = "http://localhost:8080/api/auth/verify?code=" + user.getEmailVerificationCode();
        String html = """
<div style="max-width:600px;margin:0 auto;font-family:Arial,sans-serif;background-color:#ffffff;border-radius:10px;padding:30px;text-align:center;box-shadow:0 0 10px rgba(0,0,0,0.1);">
    <img src="https://res.cloudinary.com/dfqmrqlcj/image/upload/v1749050039/logo_s3ewak.png" alt="ComedyFlow Logo" style="width:120px;margin-bottom:20px;" />
    <h2 style="color:#4b4b4b;">Benvenuto su <span style="color:#4fa1ff;">Comedy</span><span style="color:#a77ff7;">Flow</span>!</h2>
    <p style="color:#555;font-size:16px;margin:20px 0;">
        Siamo felici di averti con noi!<br />
        Per completare la tua registrazione, clicca sul pulsante qui sotto per verificare la tua email.
    </p>
    <a href="%s" style="display:inline-block;padding:14px 28px;background-color:#28a745;color:#ffffff;font-weight:bold;border-radius:5px;text-decoration:none;margin:20px 0;">Verifica Email</a>
    <p style="color:#888;font-size:14px;margin-top:30px;">
        Se il bottone non funziona, copia e incolla questo link nel tuo browser:<br />
        <a href="%s" style="color:#4fa1ff;">%s</a>
    </p>
    <p style="color:#ccc;font-size:12px;margin-top:40px;">© 2025 ComedyFlow - Tutti i diritti riservati</p>
</div>
""".formatted(link, link, link);


        emailSenderService.sendEmail(request.getEmail(), "Verifica la tua email", html);

        // Crea l'entità solo se è uno spettatore
        if (ruoloRichiesto == Role.ROLE_SPETTATORE) {
            Spettatore spettatore = new Spettatore();
            spettatore.setNome(request.getNome());
            spettatore.setCognome(request.getCognome());
            spettatore.setEmail(request.getEmail());
            spettatore.setAvatar("https://ui-avatars.com/api/?name=" + request.getNome() + "+" + request.getCognome());
            spettatore.setAppUser(user);

            spettatoreRepository.save(spettatore);
            return ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponse(spettatore.getId()));
        }

        // Altrimenti ritorna solo l'id dell'appUser (verrà gestito all'approvazione)
        return ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponse(user.getId()));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = appUserService.authenticateUser(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String code) {
        Optional<AppUser> userOptional = appUserRepository.findByEmailVerificationCode(code);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Codice non valido");
        }

        AppUser user = userOptional.get();
        user.setEmailVerified(true);
        user.setEmailVerificationCode(null);
        appUserRepository.save(user);

        return ResponseEntity.ok("Email verificata con successo!");
    }


    @Transactional
    @GetMapping("/approva-ruolo")
    public ResponseEntity<String> approvaRuolo(@RequestParam String username, @RequestParam Role ruolo) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));

        user.setRoles(new HashSet<>(Set.of(ruolo)));
        user.setRuoloRichiesto(null);
        appUserRepository.save(user);

        Optional<Utente> optionalUtente = utenteRepository.findByAppUserUsername(username);
        PendingUserData pending = pendingUserDataRepository.findByUsername(username).orElse(null);

        if (optionalUtente.isPresent()) {
            Utente utente = optionalUtente.get();

            if (ruolo == Role.ROLE_COMICO && utente instanceof Comico comico) {
                comico.setBio(getOrDefault(pending != null ? pending.getBio() : null, "Biografia da completare"));
                utenteRepository.save(comico);

            } else if (ruolo == Role.ROLE_LOCALE && utente instanceof Locale locale) {
                locale.setNomeLocale(getOrDefault(pending != null ? pending.getNomeLocale() : null, "Nome locale da definire"));
                locale.setDescrizione(getOrDefault(pending != null ? pending.getDescrizione() : null, "Descrizione da completare"));
                locale.setVia(getOrDefault(pending != null ? pending.getVia() : null, "Via da completare"));
                if (pending != null && pending.getComuneId() != null) {
                    locale.setComune(comuneRepository.findById(pending.getComuneId()).orElse(null));
                }
                utenteRepository.save(locale);
            }

        } else {
            if (ruolo == Role.ROLE_COMICO) {
                Comico comico = new Comico();
                comico.setAppUser(user);
                comico.setNome(getOrDefault(pending != null ? pending.getNome() : null, "Nome da definire"));
                comico.setCognome(getOrDefault(pending != null ? pending.getCognome() : null, "Cognome da definire"));
                comico.setEmail(getOrDefault(pending != null ? pending.getEmail() : null, user.getUsername()));
                comico.setAvatar(getOrDefault(pending != null ? pending.getAvatar() : null, "https://ui-avatars.com/api/?name=Comico"));
                comico.setBio(getOrDefault(pending != null ? pending.getBio() : null, "Biografia da completare"));
                utenteRepository.save(comico);

            } else if (ruolo == Role.ROLE_LOCALE) {
                Locale locale = new Locale();
                locale.setAppUser(user);
                locale.setNome(getOrDefault(pending != null ? pending.getNome() : null, "Titolare da definire"));
                locale.setCognome(getOrDefault(pending != null ? pending.getCognome() : null, "Cognome da definire"));
                locale.setEmail(getOrDefault(pending != null ? pending.getEmail() : null, user.getUsername()));
                locale.setAvatar(getOrDefault(pending != null ? pending.getAvatar() : null, "https://ui-avatars.com/api/?name=Locale"));
                locale.setNomeLocale(getOrDefault(pending != null ? pending.getNomeLocale() : null, "Nome locale da definire"));
                locale.setDescrizione(getOrDefault(pending != null ? pending.getDescrizione() : null, "Descrizione da completare"));
                locale.setVia(getOrDefault(pending != null ? pending.getVia() : null, "Via da completare"));
                if (pending != null && pending.getComuneId() != null) {
                    locale.setComune(comuneRepository.findById(pending.getComuneId()).orElse(null));
                }
                utenteRepository.save(locale);
            }
        }

        if (pending != null) {
            pendingUserDataRepository.deleteByUsername(username);
            System.out.println("✅ PendingUserData rimosso per " + username);
        }

        return ResponseEntity.ok("Ruolo approvato correttamente.");
    }

    // Metodo di utilità per evitare ripetizioni
    private String getOrDefault(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }



    @PostMapping("/sollecito-approvazione")
    public ResponseEntity<String> sollecito(@RequestParam String username) throws MessagingException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));

        if (user.getRuoloRichiesto() == null) {
            return ResponseEntity.badRequest().body("Nessun ruolo richiesto da approvare.");
        }

        if (user.getUltimoSollecito() != null &&
                user.getUltimoSollecito().isAfter(LocalDateTime.now().minusHours(12))) {
            throw new RuntimeException("Hai già inviato un sollecito nelle ultime 12 ore.");
        }

        user.setUltimoSollecito(LocalDateTime.now());
        appUserRepository.save(user);

        String body = """
        <h3>Sollecito approvazione ruolo</h3>
        <p>L'utente %s ha sollecitato l'approvazione del ruolo: <strong>%s</strong></p>
        <a href="http://localhost:8080/api/auth/approva-ruolo?username=%s&ruolo=%s">Approva ora</a>
        """.formatted(username, user.getRuoloRichiesto(), username, user.getRuoloRichiesto());

        emailSenderService.sendEmail("antoniokleijn@gmail.com", "Sollecito approvazione ruolo", body);

        return ResponseEntity.ok("Sollecito inviato all'amministratore.");
    }




}
