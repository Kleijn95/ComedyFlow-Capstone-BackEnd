package it.epicode.ComedyFlow.utenti.spettatori;

import it.epicode.ComedyFlow.auth.RegisterRequest;
import it.epicode.ComedyFlow.auth.Role;
import lombok.Data;

@Data
public class SpettatoreRequest extends RegisterRequest {
    // Non serve ridefinire i campi, li eredita tutti
    private String avatar; // <--- AGGIUNGI QUESTO CAMPO

}
