package it.epicode.ComedyFlow.prenotazioni;

import lombok.Data;

@Data
public class PrenotazioneRequest {
    private Long spettatoreId;
    private Long eventoId;
    private int numeroPostiPrenotati;
}
