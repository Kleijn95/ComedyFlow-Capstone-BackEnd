package it.epicode.ComedyFlow.utenti.spettatori;


import it.epicode.ComedyFlow.utenti.PendingUserData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingUserDataRepository extends JpaRepository<PendingUserData, Long> {
    Optional<PendingUserData> findByUsername(String username);
    void deleteByUsername(String username);
}
