package it.epicode.ComedyFlow.prenotazioni;

import it.epicode.ComedyFlow.eventi.Evento;
import it.epicode.ComedyFlow.utenti.spettatori.Spettatore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "prenotazioni")

public class Prenotazione {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private  Long id;

    @ManyToOne
    private Spettatore spettatore;

    @ManyToOne
    private Evento evento;

    @Column(nullable = false)
    private int numeroPostiPrenotati;
    @CreationTimestamp
    private LocalDateTime dataOraPrenotazione;



}