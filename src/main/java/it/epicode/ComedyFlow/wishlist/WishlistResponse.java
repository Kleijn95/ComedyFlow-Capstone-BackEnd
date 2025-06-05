package it.epicode.ComedyFlow.wishlist;

import it.epicode.ComedyFlow.eventi.StatoEvento;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishlistResponse {
    private Long id;

    // Evento salvato
    private Long eventoId;
    private String titoloEvento;
    private String descrizioneEvento;
    private LocalDateTime dataEvento;
    private int postiDisponibili;
    private StatoEvento statoEvento;

    // Comico associato (anche se è un salvataggio evento, puoi far vedere il nome)
    private Long comicoId;
    private String nomeComico;
    private String cognomeComico;
    private String avatarComico;

    // Locale associato
    private String nomeLocale;
    private String indirizzoLocale;

    // Info generiche
    private LocalDateTime dataAggiunta;
}
