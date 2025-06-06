package it.epicode.ComedyFlow.prenotazioni;

import it.epicode.ComedyFlow.eventi.StatoEvento;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrenotazioneResponse {
    private Long id;
    private String nomeSpettatore;
    private String titoloEvento;
    private int numeroPostiPrenotati;
    private LocalDateTime dataOraPrenotazione;
    private LocalDateTime dataOraEvento;
    private String nomeLocale;
    private StatoEvento statoEvento; // aggiungi questo campo
    private Long eventoId;
    private String avatar; // URL dell'immagine

}
