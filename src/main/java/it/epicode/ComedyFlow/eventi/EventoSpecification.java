package it.epicode.ComedyFlow.eventi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class EventoSpecification {

    public static Specification<Evento> filterBy(EventoFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getComico() != null && !filter.getComico().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("comico").get("nome")), "%" + filter.getComico().toLowerCase() + "%"));
            }

            if (filter.getProvincia() != null && !filter.getProvincia().isEmpty()) {
                predicates.add(cb.equal(root.get("locale").get("comune").get("provincia").get("nome"), filter.getProvincia()));
            }

            if (filter.getData() != null) {
                predicates.add(cb.equal(cb.function("DATE", LocalDate.class, root.get("dataOra")), filter.getData()));
            }

            // 👇 Filtro per locale specifico, se presente
            if (filter.getLocaleId() != null) {
                predicates.add(cb.equal(root.get("locale").get("id"), filter.getLocaleId()));
            } else {
                // 👇 Se non è un locale, mostra solo eventi IN_PROGRAMMA
                predicates.add(cb.equal(root.get("stato"), StatoEvento.IN_PROGRAMMA));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}