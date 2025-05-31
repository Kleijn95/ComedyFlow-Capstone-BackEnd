package it.epicode.ComedyFlow.utenti;

import it.epicode.ComedyFlow.auth.AppUser;
import it.epicode.ComedyFlow.indirizzi.Comune;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "utenti")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_utente", discriminatorType = DiscriminatorType.STRING)
public abstract class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cognome;
    private String email;
    private String avatar;


    @OneToOne
    private AppUser appUser;
}
