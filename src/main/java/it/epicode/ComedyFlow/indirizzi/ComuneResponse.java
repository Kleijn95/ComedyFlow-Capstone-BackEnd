package it.epicode.ComedyFlow.indirizzi;

import lombok.Data;

@Data
public class ComuneResponse {
    private Long id;
    private String nome;
    private String provinciaNome;
    private String provinciaSigla;
}
