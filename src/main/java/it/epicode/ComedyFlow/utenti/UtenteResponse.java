package it.epicode.ComedyFlow.utenti;

import lombok.Data;

@Data
public class UtenteResponse {
    private Long id;
    private String nome;
    private String cognome;
    private String email;
    private String avatar;
}
