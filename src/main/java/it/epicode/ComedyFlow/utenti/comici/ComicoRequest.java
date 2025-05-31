package it.epicode.ComedyFlow.utenti.comici;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComicoRequest {
    @NotBlank
    private String nome;

    @NotBlank
    private String cognome;

    @Email
    private String email;

    private String avatar;

    private String bio;

    private String password;

}

