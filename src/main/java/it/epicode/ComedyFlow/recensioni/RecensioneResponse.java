package it.epicode.ComedyFlow.recensioni;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecensioneResponse {
    private Long id;
    private String contenuto;
    private int voto;
    private String autore;
    private Long eventoId;
    private String titoloEvento;
    private TipoRecensione tipo;
    private LocalDateTime data;
}