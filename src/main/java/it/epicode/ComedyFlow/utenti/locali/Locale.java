package it.epicode.ComedyFlow.utenti.locali;

import it.epicode.ComedyFlow.indirizzi.Comune;
import it.epicode.ComedyFlow.utenti.Utente;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@DiscriminatorValue("LOCALE")
public class Locale extends Utente {

    private String nomeLocale;
    private String descrizione;
    private String via; // es. "Via Etnea, 23"

    @ManyToOne
    @JoinColumn(name = "comune_id")
    private Comune comune;
}
