package it.epicode.ComedyFlow.indirizzi;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Provincia")

public class Provincia {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String sigla;   // es. "CT"
    private String nome;    // es. "Catania"

}