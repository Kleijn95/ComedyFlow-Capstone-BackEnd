package it.epicode.ComedyFlow.eventi;

import it.epicode.ComedyFlow.prenotazioni.Prenotazione;
import it.epicode.ComedyFlow.utenti.comici.Comico;
import it.epicode.ComedyFlow.utenti.locali.Locale;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "eventi")

public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false)
    private String titolo;
    @Column(nullable = false)
    private LocalDateTime dataOra;
    private String descrizione;
    @Column(nullable = false)
    private int numeroPostiDisponibili;
    @Column(nullable = false)
    private int numeroPostiTotali;

    @Enumerated(EnumType.STRING)
    private StatoEvento stato;

    @OneToMany(mappedBy = "evento")
    private List<Prenotazione> prenotazioni;

    @ManyToOne
    private Locale locale;

    @ManyToOne
    private Comico comico;

    private String locandina;


}