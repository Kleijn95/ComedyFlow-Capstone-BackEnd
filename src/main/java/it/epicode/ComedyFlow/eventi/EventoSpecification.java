package it.epicode.ComedyFlow.eventi;

import java.time.LocalDate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class EventoSpecification {

    public static Specification<Evento> filterBy(EventoFilterDto filter) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (filter.getProvincia() != null && !filter.getProvincia().isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(
                                root.get("locale").get("comune").get("provincia").get("nome"),
                                filter.getProvincia()
                        ));
            }

            if (filter.getComico() != null && !filter.getComico().isBlank()) {
                String likePattern = "%" + filter.getComico().toLowerCase() + "%";
                Predicate nomeMatch = cb.like(cb.lower(root.get("comico").get("nome")), likePattern);
                Predicate cognomeMatch = cb.like(cb.lower(root.get("comico").get("cognome")), likePattern);
                predicate = cb.and(predicate, cb.or(nomeMatch, cognomeMatch));
            }

            if (filter.getData() != null) {
                predicate = cb.and(predicate,
                        cb.equal(cb.function("DATE", LocalDate.class, root.get("dataOra")), filter.getData()));
            }

            if (filter.getComicoId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("comico").get("id"), filter.getComicoId()));
            }

            if (filter.getLocaleId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("locale").get("id"), filter.getLocaleId()));
            }

            return predicate;
        };
    }
}