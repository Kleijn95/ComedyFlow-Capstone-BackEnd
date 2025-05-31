package it.epicode.ComedyFlow.recensioni;

import it.epicode.ComedyFlow.eventi.Evento;
import it.epicode.ComedyFlow.utenti.spettatori.Spettatore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recensioni")

public class Recensione {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private  Long id;
    @Column(nullable = false, length = 1000)
    private String contenuto;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int voto;

    @ManyToOne(optional = false)
    private Spettatore autore;

    @ManyToOne(optional = false)
    private Evento evento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecensione tipo; // COMICO o LOCALE

    private LocalDateTime data;
}