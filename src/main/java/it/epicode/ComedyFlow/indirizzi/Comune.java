package it.epicode.ComedyFlow.indirizzi;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comuni")

public class Comune {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String nome; // es. "Acireale"

    @ManyToOne
    private Provincia provincia;
}