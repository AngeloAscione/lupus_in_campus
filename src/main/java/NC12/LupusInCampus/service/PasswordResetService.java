package NC12.LupusInCampus.service;

import NC12.LupusInCampus.model.PasswordResetToken;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.PasswordResetTokenDAO;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.utils.JsonConfigReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PlayerDAO playerDAO;
    private final PasswordResetTokenDAO passwordResetTokenDAO;

    private static final long EXPIRATION_TIME = 15;

    public String initiatePasswordReset(String email) {
        Player player = playerDAO.findPlayerByEmail(email);

        passwordResetTokenDAO.deleteByPlayer(player);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setPlayer(player);
        resetToken.setExpiryDate(Instant.now().plus(EXPIRATION_TIME, ChronoUnit.MINUTES));
        passwordResetTokenDAO.save(resetToken);

        String myip = JsonConfigReader.readFile("app-config.json").get("ip").getAsString();

        return "http://%s:8080/controller/player/reset-password?token=".formatted(myip) + token;

    }

    public Player validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenDAO.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token has expired");
        }

        return resetToken.getPlayer();
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenDAO.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token has expired");
        }

        Player player = resetToken.getPlayer();

        player.setPassword(newPassword);

        playerDAO.save(player);

        passwordResetTokenDAO.delete(resetToken);
    }
}
