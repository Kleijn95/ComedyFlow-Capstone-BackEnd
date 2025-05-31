// 📦 Servizio per gestione locali aggiornato con Request/Response
package it.epicode.ComedyFlow.utenti.locali;

import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.auth.AppUserRepository;
import it.epicode.ComedyFlow.auth.Role;
import it.epicode.ComedyFlow.common.cloudinary.CloudinaryService;
import it.epicode.ComedyFlow.indirizzi.ComuneRepository;
import it.epicode.ComedyFlow.indirizzi.ComuneResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocaleService {
    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private LocaleRepository localeRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ComuneRepository comuneRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔎 Tutti possono vedere tutti i locali
    public List<LocaleResponse> findAll() {
        return localeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 🔎 Tutti possono cercare un locale per ID
    public LocaleResponse findById(Long id) {
        Locale locale = localeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locale non trovato con ID: " + id));
        return toResponse(locale);
    }

    // 🔄 Solo admin o il proprietario del locale possono modificarlo
    public LocaleResponse updateLocale(Long id, LocaleRequest request, AppUser requester) {
        Locale existing = localeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locale non trovato con ID: " + id));

        if (!isAdmin(requester) && !isOwner(existing, requester)) {
            throw new SecurityException("Non hai i permessi per modificare questo locale.");
        }
        if (request.getPassword() != null && !request.getPassword().equals("hidden-password")) {
            AppUser appUser = existing.getAppUser();
            appUser.setPassword(passwordEncoder.encode(request.getPassword()));
            appUserRepository.save(appUser); // salva modifiche all’utente
        }

        existing.setNome(request.getNome());
        existing.setCognome(request.getCognome());
        existing.setEmail(request.getEmail());
        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            existing.setAvatar(request.getAvatar());
        }
        existing.setNomeLocale(request.getNomeLocale());
        existing.setDescrizione(request.getDescrizione());
        existing.setVia(request.getVia());
        existing.setComune(comuneRepository.findById(request.getComuneId())
                .orElseThrow(() -> new EntityNotFoundException("Comune non trovato con ID: " + request.getComuneId())));

        return toResponse(localeRepository.save(existing));
    }

    // ❌ Solo admin o il proprietario del locale possono eliminarlo
    public void deleteLocale(Long id, AppUser requester) {
        Locale locale = localeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locale non trovato con ID: " + id));

        if (!isAdmin(requester) && !isOwner(locale, requester)) {
            throw new SecurityException("Non hai i permessi per eliminare questo locale.");
        }

        localeRepository.delete(locale);
    }

    private boolean isAdmin(AppUser user) {
        return user.getRoles().contains(Role.ROLE_ADMIN);
    }

    private boolean isOwner(Locale locale, AppUser user) {
        return locale.getAppUser().getId().equals(user.getId());
    }

    public void uploadAvatar(Long id, MultipartFile file, AppUser requester) {
        Locale locale = localeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locale non trovato con ID: " + id));

        if (!isAdmin(requester) && !isOwner(locale, requester)) {
            throw new SecurityException("Non hai i permessi per aggiornare questo avatar.");
        }

        String avatarUrl = cloudinaryService.uploadImage(file);
        locale.setAvatar(avatarUrl);
        localeRepository.save(locale);
    }

    private LocaleResponse toResponse(Locale l) {
        LocaleResponse r = new LocaleResponse();
        r.setId(l.getId());
        r.setNome(l.getNome());
        r.setCognome(l.getCognome());
        r.setEmail(l.getEmail());
        r.setAvatar(l.getAvatar());
        r.setNomeLocale(l.getNomeLocale());
        r.setDescrizione(l.getDescrizione());
        r.setVia(l.getVia());

        ComuneResponse comune = new ComuneResponse();
        comune.setId(l.getComune().getId());
        comune.setNome(l.getComune().getNome());
        comune.setProvinciaNome(l.getComune().getProvincia().getNome());
        comune.setProvinciaSigla(l.getComune().getProvincia().getSigla());
        r.setComune(comune);


        return r;
    }

}
