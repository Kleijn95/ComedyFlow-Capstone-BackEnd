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

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ComicoRepository comicoRepository;

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

    // ✅ Classe statica per la mappatura
    public static class WishlistMapper {
        public static WishlistResponse toDto(Wishlist wishlist) {
            WishlistResponse dto = new WishlistResponse();
            dto.setId(wishlist.getId());
            dto.setDataAggiunta(wishlist.getDataAggiunta());

            // EVENTO
            if (wishlist.getEvento() != null) {
                var evento = wishlist.getEvento();
                dto.setEventoId(evento.getId());
                dto.setTitoloEvento(evento.getTitolo());
                dto.setDescrizioneEvento(evento.getDescrizione());
                dto.setDataEvento(evento.getDataOra());
                dto.setPostiDisponibili(evento.getNumeroPostiDisponibili());
                dto.setStatoEvento(evento.getStato());

                // COMICO dell'evento
                if (evento.getComico() != null) {
                    var comico = evento.getComico();
                    dto.setComicoId(comico.getId());
                    dto.setNomeComico(comico.getNome());
                    dto.setCognomeComico(comico.getCognome());
                    dto.setAvatarComico(comico.getAvatar());
                }

                // LOCALE dell'evento
                if (evento.getLocale() != null) {
                    var locale = evento.getLocale();
                    dto.setNomeLocale(locale.getNome());
                    dto.setIndirizzoLocale(locale.getVia());
                }
            }

            // COMICO SALVATO DIRETTAMENTE
            if (wishlist.getComico() != null) {
                var comico = wishlist.getComico();
                dto.setComicoId(comico.getId());
                dto.setNomeComico(comico.getNome());
                dto.setCognomeComico(comico.getCognome());
                dto.setAvatarComico(comico.getAvatar());
            }

            return dto;
        }
    }
}
