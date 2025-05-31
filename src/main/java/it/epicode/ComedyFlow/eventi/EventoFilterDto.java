package it.epicode.ComedyFlow.eventi;


import lombok.Data;

import java.time.LocalDate;

@Data
public class EventoFilterDto {
    private Long idUtente;
    private String comico;
    private String provincia;
    private LocalDate data; // o LocalDateTime se vuoi anche l'ora
    private Long comicoId;
    private Long localeId;

}
