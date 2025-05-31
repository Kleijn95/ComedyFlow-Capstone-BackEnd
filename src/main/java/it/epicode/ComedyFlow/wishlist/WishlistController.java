package it.epicode.ComedyFlow.wishlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@PreAuthorize("hasRole('SPETTATORE')") // o chi vuoi
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping
    public List<WishlistResponse> getWishlist(@RequestParam Long utenteId) {
        return wishlistService.getWishlistByUtente(utenteId);
    }

    @PostMapping("/evento/{eventoId}")
    public void addEvento(@RequestParam Long utenteId, @PathVariable Long eventoId) {
        wishlistService.addEventoToWishlist(utenteId, eventoId);
    }

    @PostMapping("/comico/{comicoId}")
    public void addComico(@RequestParam Long utenteId, @PathVariable Long comicoId) {
        wishlistService.addComicoToWishlist(utenteId, comicoId);
    }

    @DeleteMapping("/evento/{eventoId}")
    public void removeEvento(@RequestParam Long utenteId, @PathVariable Long eventoId) {
        wishlistService.removeEventoFromWishlist(utenteId, eventoId);
    }

    @DeleteMapping("/comico/{comicoId}")
    public void removeComico(@RequestParam Long utenteId, @PathVariable Long comicoId) {
        wishlistService.removeComicoFromWishlist(utenteId, comicoId);
    }
}
