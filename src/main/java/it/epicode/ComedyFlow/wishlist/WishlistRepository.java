package it.epicode.ComedyFlow.wishlist;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUtenteId(Long utenteId);
    boolean existsByUtenteIdAndComicoId(Long utenteId, Long comicoId);
    boolean existsByUtenteIdAndEventoId(Long utenteId, Long eventoId);
    Optional<Wishlist> findByUtenteIdAndComicoId(Long utenteId, Long comicoId);
    Optional<Wishlist> findByUtenteIdAndEventoId(Long utenteId, Long eventoId);
}