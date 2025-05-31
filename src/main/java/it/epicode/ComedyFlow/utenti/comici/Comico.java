package it.epicode.ComedyFlow.utenti.comici;

import it.epicode.ComedyFlow.utenti.Utente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("COMICO") // per Comico
public class Comico extends Utente {

    private String bio;

}