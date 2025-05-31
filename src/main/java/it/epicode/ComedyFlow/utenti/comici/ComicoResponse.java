package it.epicode.ComedyFlow.utenti.comici;


import lombok.Data;

@Data
public class ComicoResponse {
    private Long id;
    private String nome;
    private String cognome;
    private String email;
    private String avatar;
    private String bio;
}
