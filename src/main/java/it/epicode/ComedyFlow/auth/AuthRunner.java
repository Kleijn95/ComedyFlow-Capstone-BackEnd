package it.epicode.ComedyFlow.auth;

import it.epicode.ComedyFlow.utenti.spettatori.Spettatore;
import it.epicode.ComedyFlow.utenti.spettatori.SpettatoreRepository;
import it.epicode.ComedyFlow.utenti.spettatori.SpettatoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class AuthRunner implements ApplicationRunner {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SpettatoreService spettatoreService;

    @Autowired
    private SpettatoreRepository spettatoreRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Optional<AppUser> admin = appUserService.findByUsername("admin");
        if (admin.isEmpty()) {
            AppUser appUser = new AppUser();
            appUser.setUsername("admin");
            appUser.setPassword(passwordEncoder.encode("adminpwd"));
            appUser.setRoles(new HashSet<>(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))); // ✅ fix
            appUserRepository.save(appUser);

            Spettatore spettatore = new Spettatore();
            spettatore.setAppUser(appUser);
            spettatore.setAvatar("https://ui-avatars.com/api/?name=Admin");
            spettatore.setNome("Mauro");
            spettatore.setCognome("Larese");
            spettatore.setEmail("maurettothebest@gmail.com");

            spettatoreRepository.save(spettatore);
        }

        if (appUserService.findByUsername("user").isEmpty()) {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("user");
            req.setPassword("userpwd");
            req.setRuoloRichiesto(Role.ROLE_USER);
            appUserService.registerUser(req);
        }

        if (appUserService.findByUsername("comico").isEmpty()) {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("comico");
            req.setPassword("comicopwd");
            req.setRuoloRichiesto(Role.ROLE_COMICO);
            appUserService.registerUser(req);
        }

        if (appUserService.findByUsername("locale").isEmpty()) {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("locale");
            req.setPassword("localepwd");
            req.setRuoloRichiesto(Role.ROLE_LOCALE);
            appUserService.registerUser(req);
        }

        if (appUserService.findByUsername("spettatore").isEmpty()) {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("spettatore");
            req.setPassword("spettatorepwd");
            req.setRuoloRichiesto(Role.ROLE_SPETTATORE);
            appUserService.registerUser(req);
        }
    }}