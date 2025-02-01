package NC12.LupusInCampus.model.dao;

import NC12.LupusInCampus.model.PasswordResetToken;
import NC12.LupusInCampus.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenDAO extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByPlayer(Player player);
}




