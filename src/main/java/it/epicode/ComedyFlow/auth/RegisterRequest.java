package it.epicode.ComedyFlow.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String nome;
    private String cognome;
    private String email;
    private Role ruoloRichiesto; // SPETTATORE, COMICO, LOCALE
}
