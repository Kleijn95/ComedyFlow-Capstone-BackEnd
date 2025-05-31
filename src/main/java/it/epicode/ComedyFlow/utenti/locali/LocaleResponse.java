package it.epicode.ComedyFlow.utenti.locali;


import it.epicode.ComedyFlow.indirizzi.ComuneResponse;
import lombok.Data;

@Data
public class LocaleResponse {
    private Long id;
    private String nome;
    private String cognome;
    private String email;
    private String avatar;
    private String nomeLocale;
    private String descrizione;
    private String via;
    private ComuneResponse comune;
}
