package it.epicode.ComedyFlow.utenti.locali;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocaleRequest {
    @NotBlank
    private String nome;

    @NotBlank
    private String cognome;

    @Email
    private String email;

    private String avatar;

    @NotBlank
    private String nomeLocale;
    private String descrizione;
    private String via;

    private Long comuneId; // ID del comune a cui appartiene

    private String password;
}
