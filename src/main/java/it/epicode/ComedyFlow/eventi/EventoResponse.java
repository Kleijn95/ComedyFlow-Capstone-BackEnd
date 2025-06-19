package it.epicode.ComedyFlow.eventi;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventoResponse {
    private Long id;
    private String titolo;
    private LocalDateTime dataOra;
    private String descrizione;
    private int numeroPostiDisponibili;
    private int numeroPostiTotali;
    private String nomeLocale;
    private String nomeComico;
    private String comuneNome;
    private String viaLocale;
    private String emailLocale;
    private StatoEvento stato;
    private Long localeId;
    private Long comicoId;
    private String locandina;

}
