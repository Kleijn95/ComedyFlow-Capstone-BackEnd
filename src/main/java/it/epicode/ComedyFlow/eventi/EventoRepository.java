package it.epicode.ComedyFlow.eventi;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    Page<Evento> findAll(Specification<Evento> spec, Pageable pageable);




}