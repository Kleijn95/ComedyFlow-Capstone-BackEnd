package it.epicode.ComedyFlow.wishlist;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishlistResponse {
    private Long id;
    private Long eventoId;
    private String titoloEvento;
    private Long comicoId;
    private String nomeComico;
    private LocalDateTime dataAggiunta;
}
