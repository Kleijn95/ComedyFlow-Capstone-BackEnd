package it.epicode.ComedyFlow.utenti.spettatori;


import lombok.Data;

@Data
public class SpettatoreResponse {
    private Long id;
    private String nome;
    private String cognome;
    private String email;
    private String avatar;
}
