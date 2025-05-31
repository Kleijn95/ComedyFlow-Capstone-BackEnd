package it.epicode.ComedyFlow.utenti;

import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.auth.AppUserRepository;
import it.epicode.ComedyFlow.auth.AppUserService;
import it.epicode.ComedyFlow.auth.Role;
import it.epicode.ComedyFlow.common.cloudinary.CloudinaryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class UtenteService {

    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔒 Controllo se l'utente autenticato è admin
    private boolean isAdmin(AppUser adminLoggato) {
        return adminLoggato.getRoles().contains(Role.ROLE_ADMIN);
    }

    // 👤 Registrazione nuovo utente (qualsiasi tipo)
    public Utente save(Utente utente) {
        return utenteRepository.save(utente);
    }

    // 🔎 Cerca utente per ID
    public Utente getUtenteById(long id) {
        return utenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + id));
    }

    // 📋 Restituisce tutti gli utenti (solo admin)
    public List<Utente> getAllUtenti(AppUser adminLoggato) {
        if (!isAdmin(adminLoggato)) {
            throw new RuntimeException("Non hai i permessi per visualizzare tutti gli utenti");
        }
        return utenteRepository.findAll();
    }

    // 🔄 Modifica utente (solo admin)
    public UtenteResponse updateUtente(Long id, UtenteRequest request, AppUser adminLoggato) {
        if (!isAdmin(adminLoggato)) {
            throw new RuntimeException("Non hai i permessi per modificare questo utente");
        }

        Utente existing = getUtenteById(id);

        existing.setNome(request.getNome());
        existing.setCognome(request.getCognome());
        existing.setEmail(request.getEmail());
        existing.setAvatar(request.getAvatar());

        return toResponse(utenteRepository.save(existing));
    }


    // ❌ Cancella utente (solo admin)
    public void deleteUtente(Long id, AppUser adminLoggato) {
        if (!isAdmin(adminLoggato)) {
            throw new RuntimeException("Non hai i permessi per eliminare questo utente");
        }
        Utente utente = getUtenteById(id);
        utenteRepository.delete(utente);
    }

    // 📷 Carica avatar per utente autenticato
    public void uploadAvatar(long id, MultipartFile file) {
        Utente utente = getUtenteById(id);
        utente.setAvatar(cloudinaryService.uploadImage(file));
        utenteRepository.save(utente);
    }

    // 🔎 Trova per username (se lo hai come campo)
    public Utente findByUsername(String username) {
        Optional<Utente> utente = utenteRepository.findByAppUserUsername(username);
        if (utente == null) {
            throw new EntityNotFoundException("Utente non trovato con username: " + username);
        }
        return utente.get();

    }

    private UtenteResponse toResponse(Utente u) {
        UtenteResponse r = new UtenteResponse();
        r.setId(u.getId());
        r.setNome(u.getNome());
        r.setCognome(u.getCognome());
        r.setEmail(u.getEmail());
        r.setAvatar(u.getAvatar());
        return r;
    }

}





