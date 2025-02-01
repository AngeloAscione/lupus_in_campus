package NC12.LupusInCampus.model.dao;

import NC12.LupusInCampus.model.PasswordResetToken;
import NC12.LupusInCampus.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenDAO extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    @Transactional
    void deleteByPlayer(Player player);
}




