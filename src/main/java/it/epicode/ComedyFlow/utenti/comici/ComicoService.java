// 📦 Servizio per gestione comici aggiornato con Request/Response
package it.epicode.ComedyFlow.utenti.comici;

import it.epicode.ComedyFlow.auth.*;
import it.epicode.ComedyFlow.common.CommonResponse;
import it.epicode.ComedyFlow.common.cloudinary.CloudinaryService;
import it.epicode.ComedyFlow.utenti.spettatori.Spettatore;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComicoService {

    @Autowired
    private ComicoRepository comicoRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private CloudinaryService cloudinaryService;

    // 🔎 Tutti possono vedere i comici
    public List<ComicoResponse> findAllComici() {
        return comicoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ComicoResponse findById(Long id) {
        Comico comico = comicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comico non trovato con ID: " + id));
        return toResponse(comico);
    }

    // 🔄 Solo admin o il comico stesso possono modificarlo
    public ComicoResponse updateComico(Long id, ComicoRequest request, AppUser requester) {
        Comico existing = comicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comico non trovato con ID: " + id));

        if (!isAdmin(requester) && !isOwner(existing, requester)) {
            throw new SecurityException("Non hai i permessi per modificare questo comico.");
        }

        // 🔁 Aggiorna solo se presenti nel request
        existing.setNome(request.getNome());
        existing.setCognome(request.getCognome());
        existing.setEmail(request.getEmail());
        existing.setBio(request.getBio());

        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            existing.setAvatar(request.getAvatar());
        }

        AppUser appUser = existing.getAppUser();
        if (request.getPassword() != null && !request.getPassword().equals("hidden-password")) {
            appUser.setPassword(passwordEncoder.encode(request.getPassword()));
            appUserRepository.save(appUser); // <-- questa riga mancava
        }


        return toResponse(comicoRepository.save(existing));
    }


    // ❌ Solo admin o il comico stesso possono eliminarlo
    public void deleteComico(Long id, AppUser requester) {
        Comico comico = comicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comico non trovato con ID: " + id));

        if (!isAdmin(requester) && !isOwner(comico, requester)) {
            throw new SecurityException("Non hai i permessi per eliminare questo comico.");
        }

        comicoRepository.delete(comico);
    }

    public void uploadAvatar(Long id, MultipartFile file, AppUser requester) {
        Comico comico = comicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comico non trovato con ID: " + id));

        if (!isAdmin(requester) && !isOwner(comico, requester)) {
            throw new SecurityException("Non hai i permessi per aggiornare questo avatar.");
        }

        String avatarUrl = cloudinaryService.uploadImage(file);
        comico.setAvatar(avatarUrl);
        comicoRepository.save(comico);
    }




    public ComicoResponse createComico(ComicoRequest request, AppUser requester) throws MessagingException {
        if (!isAdmin(requester)) {
            throw new SecurityException("Solo l'amministratore può creare un nuovo comico.");
        }

        String rawPassword = request.getPassword(); // oppure genera una se null
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = "comicoPwd"; // Genera password random
        }

        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setUsername(request.getEmail()); // se usi email come username
        regRequest.setPassword(rawPassword);
        regRequest.setNome(request.getNome());
        regRequest.setCognome(request.getCognome());
        regRequest.setEmail(request.getEmail());
        regRequest.setRuoloRichiesto(Role.ROLE_COMICO);

        AppUser user = appUserService.registerUser(regRequest);




        Comico comico = new Comico();
        comico.setNome(request.getNome());
        comico.setCognome(request.getCognome());
        comico.setEmail(request.getEmail());
        comico.setAvatar(request.getAvatar());
        comico.setBio(request.getBio());
        comico.setAppUser(user);

        comicoRepository.save(comico);
        System.out.println("📨 Comico creato. Password: " + rawPassword); // Log temporaneo

        return toResponse(comico);
    }


    private boolean isAdmin(AppUser user) {
        return user.getRoles().contains(Role.ROLE_ADMIN);
    }

    private boolean isOwner(Comico comico, AppUser user) {
        return comico.getAppUser().getId().equals(user.getId());
    }

    private ComicoResponse toResponse(Comico c) {
        ComicoResponse r = new ComicoResponse();
        r.setId(c.getId());
        r.setNome(c.getNome());
        r.setCognome(c.getCognome());
        r.setEmail(c.getEmail());
        r.setAvatar(c.getAvatar());
        r.setBio(c.getBio());
        return r;
    }
}
