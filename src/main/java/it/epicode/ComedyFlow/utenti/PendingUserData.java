package it.epicode.ComedyFlow.utenti;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pending")

public class PendingUserData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // o direttamente relazione OneToOne con AppUser
    private String nome;
    private String cognome;
    private String email;
    private String avatar;

    // Solo se ROLE_LOCALE o ROLE_COMICO
    private String nomeLocale;
    private String descrizione;
    private String via;
    private Long comuneId; // per locali
    private String bio;     // per comici


}