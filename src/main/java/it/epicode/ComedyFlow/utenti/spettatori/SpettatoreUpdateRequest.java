package it.epicode.ComedyFlow.utenti.spettatori;

import lombok.Data;

@Data
public class SpettatoreUpdateRequest {
    private String nome;
    private String cognome;
    private String email;
    private String avatar;
    private String password;
}
