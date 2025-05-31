package it.epicode.ComedyFlow.wishlist;

import it.epicode.ComedyFlow.eventi.Evento;
import it.epicode.ComedyFlow.utenti.Utente;
import it.epicode.ComedyFlow.utenti.comici.Comico;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "wishlist")

public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Utente utente;

    @ManyToOne
    private Comico comico;

    @ManyToOne
    private Evento evento;

    private LocalDateTime dataAggiunta;

    @PrePersist
    public void onCreate() {
        this.dataAggiunta = LocalDateTime.now();
    }
}