package it.epicode.ComedyFlow.utenti.spettatori;

import it.epicode.ComedyFlow.utenti.Utente;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity

@DiscriminatorValue("SPETTATORE") // per Spettatore
public class Spettatore extends Utente {



}