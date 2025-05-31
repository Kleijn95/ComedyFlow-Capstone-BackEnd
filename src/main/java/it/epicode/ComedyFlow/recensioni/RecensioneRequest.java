package it.epicode.ComedyFlow.recensioni;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecensioneRequest {
    @NotNull
    private Long eventoId;

    @NotBlank
    private String contenuto;

    @Min(1)
    @Max(5)
    private int voto;

    @NotNull
    private TipoRecensione tipo;
}