package it.epicode.ComedyFlow.auth;

import it.epicode.ComedyFlow.common.EmailSenderService;
import it.epicode.ComedyFlow.utenti.PendingUserData;
import it.epicode.ComedyFlow.utenti.spettatori.PendingUserDataRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AppUserService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private PendingUserDataRepository pendingUserDataRepository;

    @Transactional
    public AppUser registerUser(RegisterRequest request) throws MessagingException {
        String username = request.getUsername();

        if (appUserRepository.existsByUsername(username)) {
            throw new EntityExistsException("Username già in uso");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setEmailVerified(false);
        appUser.setEmailVerificationCode(UUID.randomUUID().toString());
        appUser.setRoles(new HashSet<>(Set.of(Role.ROLE_SPETTATORE))); // Default SPETTATORE

        Role ruoloRichiesto = request.getRuoloRichiesto() != null ? request.getRuoloRichiesto() : Role.ROLE_SPETTATORE;

        if (ruoloRichiesto != Role.ROLE_SPETTATORE) {
            appUser.setRuoloRichiesto(ruoloRichiesto);

            // Salva i dati aggiuntivi in PendingUserData
            PendingUserData pending = new PendingUserData();
            pending.setUsername(username);
            pending.setNome(request.getNome());
            pending.setCognome(request.getCognome());
            pending.setEmail(request.getEmail());
            pending.setAvatar("https://ui-avatars.com/api/?name=" + request.getNome() + "+" + request.getCognome());
            pendingUserDataRepository.save(pending);

            // Mail all'admin per approvazione
            String link = "http://localhost:8080/api/auth/approva-ruolo?username=" + username + "&ruolo=" + ruoloRichiesto.name();
            String body = """
        <h3>Richiesta ruolo avanzato</h3>
        <p>L'utente <strong>%s</strong> ha richiesto il ruolo: <strong>%s</strong></p>
        <a href="%s">Approva ruolo</a>
        """.formatted(username, ruoloRichiesto.name(), link);
            emailSenderService.sendEmail("antoniokleijn@gmail.com", "Richiesta ruolo " + ruoloRichiesto.name(), body);
        }

        return appUserRepository.save(appUser);
    }





    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    public String authenticateUser(String username, String password) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));

        if (!appUser.isEmailVerified()) {
            throw new SecurityException("Email non verificata");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            if (!appUser.getRoles().contains(Role.ROLE_COMICO)
                    && !appUser.getRoles().contains(Role.ROLE_LOCALE)
                    && appUser.getRuoloRichiesto() != null) {

                throw new RoleNotApprovedException("Il tuo ruolo avanzato non è stato ancora approvato dall'amministratore.");
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtTokenUtil.generateToken(userDetails);
        } catch (AuthenticationException e) {
            throw new SecurityException("Credenziali non valide", e);
        }
    }



    public AppUser loadUserByUsername(String username)  {
        AppUser appUser = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con username: " + username));


        return appUser;
    }
}
