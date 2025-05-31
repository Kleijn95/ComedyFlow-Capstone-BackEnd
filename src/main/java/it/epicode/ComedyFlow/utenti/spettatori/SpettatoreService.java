package it.epicode.ComedyFlow.utenti.spettatori;

import it.epicode.ComedyFlow.auth.*;
import it.epicode.ComedyFlow.common.CommonResponse;
import it.epicode.ComedyFlow.common.EmailSenderService;
import it.epicode.ComedyFlow.common.cloudinary.CloudinaryService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpettatoreService {

    @Autowired
    private SpettatoreRepository spettatoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AppUserService appUserService;

    @Autowired

    private AppUserRepository appUserRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EmailSenderService  emailSenderService;

    public List<SpettatoreResponse> findAll(AppUser requester) {
        if (!isAdmin(requester)) {
            throw new SecurityException("Accesso negato: solo l'amministratore può visualizzare tutti gli spettatori.");
        }
        return spettatoreRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SpettatoreResponse findById(Long id, AppUser requester) {
        if (!isAdmin(requester)) {
            throw new SecurityException("Accesso negato: solo l'amministratore può cercare uno spettatore per ID.");
        }
        Spettatore spettatore = spettatoreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Spettatore non trovato con ID: " + id));
        return toResponse(spettatore);
    }

    public SpettatoreResponse update(Long id, SpettatoreUpdateRequest request, AppUser requester) {
        Spettatore existing = spettatoreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Spettatore non trovato con ID: " + id));

        if (!isAdmin(requester) && !isOwner(existing, requester)) {
            throw new SecurityException("Non hai i permessi per modificare questo spettatore.");
        }

        existing.setNome(request.getNome());
        existing.setCognome(request.getCognome());
        existing.setEmail(request.getEmail());

        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            existing.setAvatar(request.getAvatar());
        }

        AppUser appUser = existing.getAppUser();
        if (request.getPassword() != null && !request.getPassword().equals("hidden-password")) {
            appUser.setPassword(passwordEncoder.encode(request.getPassword()));
            appUserRepository.save(appUser);
        }

        return toResponse(spettatoreRepository.save(existing));
    }



    public void uploadAvatar(Long id, MultipartFile file, AppUser requester) {
        Spettatore spettatore = spettatoreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Spettatore non trovato con ID: " + id));

        if (!isAdmin(requester) && !isOwner(spettatore, requester)) {
            throw new SecurityException("Non hai i permessi per aggiornare questo avatar.");
        }

        String avatarUrl = cloudinaryService.uploadImage(file);
        spettatore.setAvatar(avatarUrl);
        spettatoreRepository.save(spettatore);
    }


    public void delete(Long id, AppUser requester) {
        Spettatore spettatore = spettatoreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Spettatore non trovato con ID: " + id));

        if (!isAdmin(requester) && !isOwner(spettatore, requester)) {
            throw new SecurityException("Non hai i permessi per eliminare questo spettatore.");
        }

        spettatoreRepository.delete(spettatore);
    }

    public CommonResponse create(SpettatoreRequest request) throws MessagingException {
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setUsername(request.getUsername());
        regRequest.setPassword(request.getPassword());
        regRequest.setNome(request.getNome());
        regRequest.setCognome(request.getCognome());
        regRequest.setEmail(request.getEmail());
        regRequest.setRuoloRichiesto(
                request.getRuoloRichiesto() != null ? request.getRuoloRichiesto() : Role.ROLE_SPETTATORE
        );

        AppUser user = appUserService.registerUser(regRequest);


        // Invia email di verifica
        String link = "http://localhost:8080/api/auth/verify?code=" + user.getEmailVerificationCode();
        String html = """
        <h3>Benvenuto su ComedyFlow!</h3>
        <p>Per completare la registrazione, verifica la tua email cliccando sul link qui sotto:</p>
        <a href="%s" style="display:inline-block;padding:10px 20px;background-color:#28a745;color:white;border-radius:5px;text-decoration:none;">Verifica email</a>
        <p>Oppure copia questo link nel browser:<br>%s</p>
    """.formatted(link, link);

        emailSenderService.sendEmail(request.getEmail(), "Verifica la tua email", html);

        // ✅ SOLO SE è SPETTATORE creo l'entità
        if (user.getRoles().contains(Role.ROLE_SPETTATORE)) {
            Spettatore spettatore = new Spettatore();
            spettatore.setNome(request.getNome());
            spettatore.setCognome(request.getCognome());
            spettatore.setEmail(request.getEmail());
            spettatore.setAvatar("https://ui-avatars.com/api/?name=" + request.getNome() + "+" + request.getCognome());
            spettatore.setAppUser(user);
            spettatoreRepository.save(spettatore);
            return new CommonResponse(spettatore.getId());
        }

        // 🎯 Altrimenti aspetta approvazione, ritorna solo ID dell'AppUser
        return new CommonResponse(user.getId());
    }



    private boolean isAdmin(AppUser user) {
        return user.getRoles().contains(Role.ROLE_ADMIN);
    }

    private boolean isOwner(Spettatore spettatore, AppUser user) {
        return spettatore.getAppUser().getId().equals(user.getId());
    }

    private SpettatoreResponse toResponse(Spettatore s) {
        SpettatoreResponse r = new SpettatoreResponse();
        r.setId(s.getId());
        r.setNome(s.getNome());
        r.setCognome(s.getCognome());
        r.setEmail(s.getEmail());
        r.setAvatar(s.getAvatar());
        return r;
    }
}
