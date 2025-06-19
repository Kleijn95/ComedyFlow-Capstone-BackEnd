package it.epicode.ComedyFlow.eventi;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventoRequest {
    private String titolo;
    private LocalDateTime dataOra;
    private String descrizione;
    private int numeroPostiDisponibili;
    private int numeroPostiTotali;
    private Long localeId;
    private Long comicoId;
    private String locandina;

}
