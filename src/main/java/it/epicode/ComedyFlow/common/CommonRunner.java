package it.epicode.ComedyFlow.common;

import com.github.javafaker.Faker;
import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.auth.AppUserRepository;
import it.epicode.ComedyFlow.auth.Role;
import it.epicode.ComedyFlow.eventi.Evento;
import it.epicode.ComedyFlow.eventi.EventoRepository;
import it.epicode.ComedyFlow.eventi.StatoEvento;
import it.epicode.ComedyFlow.indirizzi.Comune;
import it.epicode.ComedyFlow.indirizzi.ComuneRepository;
import it.epicode.ComedyFlow.indirizzi.ProvinciaRepository;
import it.epicode.ComedyFlow.prenotazioni.Prenotazione;
import it.epicode.ComedyFlow.prenotazioni.PrenotazioneRepository;
import it.epicode.ComedyFlow.recensioni.Recensione;
import it.epicode.ComedyFlow.recensioni.RecensioneRepository;
import it.epicode.ComedyFlow.recensioni.TipoRecensione;
import it.epicode.ComedyFlow.utenti.locali.Locale;
import it.epicode.ComedyFlow.utenti.comici.Comico;
import it.epicode.ComedyFlow.utenti.comici.ComicoRepository;
import it.epicode.ComedyFlow.utenti.locali.LocaleRepository;
import it.epicode.ComedyFlow.utenti.spettatori.Spettatore;
import it.epicode.ComedyFlow.utenti.spettatori.SpettatoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class CommonRunner implements CommandLineRunner {

    @Autowired private Faker faker;
    @Autowired private ProvinciaRepository provinciaRepo;
    @Autowired private ComuneRepository comuneRepo;
    @Autowired private ComicoRepository comicoRepo;
    @Autowired private LocaleRepository localeRepo;
    @Autowired private SpettatoreRepository spettatoreRepo;
    @Autowired private EventoRepository eventoRepo;
    @Autowired private PrenotazioneRepository prenotazioneRepo;
    @Autowired private AppUserRepository appUserRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RecensioneRepository recensioneRepo;

    private String generateUniqueUsername() {
        String username;
        do {
            username = faker.name().username();
        } while (appUserRepo.existsByUsername(username));
        return username;
    }

    @Override
    public void run(String... args) {
        List<Comune> comuni = comuneRepo.findAll();
        if (comuni.isEmpty()) {
            System.err.println("\u26A0\uFE0F Nessun comune trovato. Assicurati di aver importato i CSV.");
            return;
        }

        Collections.shuffle(comuni);
        List<Comune> comuniScelti = comuni.stream().limit(5).toList();

        List<Locale> locali = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Comune comune = comuniScelti.get(i % comuniScelti.size());
            AppUser user = new AppUser();
            String nome = faker.name().firstName();
            String cognome = faker.name().lastName();
            user.setUsername(generateUniqueUsername());
            user.setPassword(passwordEncoder.encode("password"));
            user.setRoles(Set.of(Role.ROLE_LOCALE));
            user.setEmailVerified(true);
            AppUser savedUser = appUserRepo.save(user);

            Locale l = new Locale();
            l.setNome(nome);
            l.setCognome(cognome);
            l.setEmail(faker.internet().emailAddress());
            l.setAvatar("https://ui-avatars.com/api/?name=" + nome + "+" + cognome);
            l.setNomeLocale(faker.company().name());
            l.setDescrizione(faker.lorem().sentence());
            l.setVia(faker.address().streetAddress());
            l.setComune(comune);
            l.setAppUser(savedUser);
            localeRepo.save(l);
            locali.add(l);
        }

        List<Comico> comici = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            AppUser user = new AppUser();
            String nome = faker.name().firstName();
            String cognome = faker.name().lastName();
            user.setUsername(generateUniqueUsername());
            user.setPassword(passwordEncoder.encode("password"));
            user.setRoles(Set.of(Role.ROLE_COMICO));
            user.setEmailVerified(true);
            AppUser savedUser = appUserRepo.save(user);

            Comico c = new Comico();
            c.setNome(nome);
            c.setCognome(cognome);
            c.setEmail(faker.internet().emailAddress());
            c.setAvatar("https://ui-avatars.com/api/?name=" + nome + "+" + cognome);
            c.setBio(faker.lorem().paragraph());
            c.setAppUser(savedUser);
            comicoRepo.save(c);
            comici.add(c);
        }

        List<Spettatore> spettatori = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            AppUser user = new AppUser();
            String nome = faker.name().firstName();
            String cognome = faker.name().lastName();
            user.setUsername(generateUniqueUsername());
            user.setPassword(passwordEncoder.encode("password"));
            user.setRoles(Set.of(Role.ROLE_SPETTATORE));
            user.setEmailVerified(true);
            AppUser savedUser = appUserRepo.save(user);

            Spettatore s = new Spettatore();
            s.setNome(nome);
            s.setCognome(cognome);
            s.setEmail(faker.internet().emailAddress());
            s.setAvatar("https://ui-avatars.com/api/?name=" + nome + "+" + cognome);
            s.setAppUser(savedUser);
            spettatoreRepo.save(s);
            spettatori.add(s);
        }

        List<Evento> eventi = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Evento e = new Evento();
            e.setTitolo("Show Passato " + (i + 1));
            e.setDescrizione(faker.lorem().sentence());
            e.setDataOra(LocalDateTime.now().minusDays(5 + i));
            e.setNumeroPostiTotali(100);
            e.setNumeroPostiDisponibili(100);
            e.setComico(comici.get(i % comici.size()));
            e.setLocale(locali.get(i % locali.size()));
            e.setStato(StatoEvento.TERMINATO);
            eventoRepo.save(e);
            eventi.add(e);
        }
        for (int i = 0; i < 6; i++) {
            Evento e = new Evento();
            e.setTitolo("Show Futuro " + (i + 1));
            e.setDescrizione(faker.lorem().sentence());
            e.setDataOra(LocalDateTime.now().plusDays(3 + i));
            e.setNumeroPostiTotali(100);
            e.setNumeroPostiDisponibili(100);
            e.setComico(comici.get(i % comici.size()));
            e.setLocale(locali.get(i % locali.size()));
            e.setStato(StatoEvento.IN_PROGRAMMA);
            eventoRepo.save(e);
            eventi.add(e);
        }
        for (int i = 0; i < 3; i++) {
            Evento e = new Evento();
            e.setTitolo("Show Annullato " + (i + 1));
            e.setDescrizione(faker.lorem().sentence());
            e.setDataOra(LocalDateTime.now().plusDays(10 + i));
            e.setNumeroPostiTotali(100);
            e.setNumeroPostiDisponibili(100);
            e.setComico(comici.get(i % comici.size()));
            e.setLocale(locali.get(i % locali.size()));
            e.setStato(StatoEvento.ANNULLATO);
            eventoRepo.save(e);
            eventi.add(e);
        }

        for (int i = 0; i < 15; i++) {
            Prenotazione p = new Prenotazione();
            p.setSpettatore(spettatori.get(i % spettatori.size()));
            p.setEvento(eventi.get(i % eventi.size()));
            p.setNumeroPostiPrenotati(faker.number().numberBetween(1, 5));
            prenotazioneRepo.save(p);
        }

        for (int i = 0; i < 10; i++) {
            Recensione r1 = new Recensione();
            r1.setEvento(eventi.get(i % eventi.size()));
            r1.setAutore(spettatori.get(i % spettatori.size()));
            r1.setVoto(faker.number().numberBetween(1, 5));
            r1.setContenuto(faker.lorem().sentence());
            r1.setTipo(TipoRecensione.COMICO);
            r1.setData(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10)));
            recensioneRepo.save(r1);

            Recensione r2 = new Recensione();
            r2.setEvento(eventi.get(i % eventi.size()));
            r2.setAutore(spettatori.get(i % spettatori.size()));
            r2.setVoto(faker.number().numberBetween(1, 5));
            r2.setContenuto(faker.lorem().sentence());
            r2.setTipo(TipoRecensione.LOCALE);
            r2.setData(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10)));
            recensioneRepo.save(r2);
        }

        System.out.println("\u2705 Dati fittizi COMPLETI creati con successo!");
    }
}