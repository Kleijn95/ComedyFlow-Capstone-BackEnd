package it.epicode.ComedyFlow.wishlist;

import it.epicode.ComedyFlow.eventi.EventoRepository;
import it.epicode.ComedyFlow.utenti.UtenteRepository;
import it.epicode.ComedyFlow.utenti.comici.ComicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired private UtenteRepository utenteRepository;
    @Autowired private EventoRepository eventoRepository;
    @Autowired private ComicoRepository comicoRepository;

    public List<WishlistResponse> getWishlistByUtente(Long utenteId) {
        return wishlistRepository.findByUtenteId(utenteId).stream()
                .map(WishlistMapper::toDto)
                .toList();
    }

    public void addEventoToWishlist(Long utenteId, Long eventoId) {
        if (wishlistRepository.existsByUtenteIdAndEventoId(utenteId, eventoId)) return;
        Wishlist w = new Wishlist();
        w.setUtente(utenteRepository.findById(utenteId).orElseThrow());
        w.setEvento(eventoRepository.findById(eventoId).orElseThrow());
        wishlistRepository.save(w);
    }

    public void addComicoToWishlist(Long utenteId, Long comicoId) {
        if (wishlistRepository.existsByUtenteIdAndComicoId(utenteId, comicoId)) return;
        Wishlist w = new Wishlist();
        w.setUtente(utenteRepository.findById(utenteId).orElseThrow());
        w.setComico(comicoRepository.findById(comicoId).orElseThrow());
        wishlistRepository.save(w);
    }

    public void removeEventoFromWishlist(Long utenteId, Long eventoId) {
        wishlistRepository.findByUtenteIdAndEventoId(utenteId, eventoId)
                .ifPresent(wishlistRepository::delete);
    }

    public void removeComicoFromWishlist(Long utenteId, Long comicoId) {
        wishlistRepository.findByUtenteIdAndComicoId(utenteId, comicoId)
                .ifPresent(wishlistRepository::delete);
    }

    public static class WishlistMapper {
        public static WishlistResponse toDto(Wishlist wishlist) {
            WishlistResponse dto = new WishlistResponse();
            if (wishlist.getEvento() != null) {
                dto.setEventoId(wishlist.getEvento().getId());
                dto.setTitoloEvento(wishlist.getEvento().getTitolo());
            }
            if (wishlist.getComico() != null) {
                dto.setComicoId(wishlist.getComico().getId());
                dto.setNomeComico(wishlist.getComico().getNome() + " " + wishlist.getComico().getCognome());
            }
            dto.setId(wishlist.getId());
            dto.setDataAggiunta(wishlist.getDataAggiunta());
            return dto;
        }
    }

}
