package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.service.PasswordResetService;
import NC12.LupusInCampus.utils.clientServerComunication.MessageResponse;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.service.emails.Email;
import NC12.LupusInCampus.utils.Validator;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value="controller/player")
public class PlayerController {

    private final PlayerDAO playerDAO;
    private final PasswordResetService passwordResetService;

    @Autowired
    public PlayerController(PlayerDAO playerDAO, PasswordResetService passwordResetService) {
        this.playerDAO = playerDAO;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/registration")
    public ResponseEntity<?> playerRegistration(
            @RequestParam String nickname, @RequestParam String email, @RequestParam String password, HttpSession session) {

        List<String> errors = validPlayerRegistration(nickname, email, password);
        MessageResponse response;

        if (!errors.isEmpty()) {
            // Sending messages error registration
            response = new MessageResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    errors
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // If there aren't errors
        Player player = createPlayer(nickname, email, password);

        // save
        Player newPlayer = playerDAO.save(player);
        session.setAttribute("player", newPlayer);

        // sending data player
        response = new MessageResponse(
                SuccessMessages.REGISTRATION_SUCCESS.getCode(),
                SuccessMessages.REGISTRATION_SUCCESS.getMessage(),
                newPlayer
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> playerLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {

        List<String> errors = validPlayerLogin(email, password);
        MessageResponse response;

        if (!errors.isEmpty()) {
            // Sending messages error registration
            response = new MessageResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    errors
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        //  If there aren't errors
        Player player = playerDAO.findPlayerByEmail(email);
        session.setAttribute("player", player);

        // sending data player
        response = new MessageResponse(
                SuccessMessages.LOGIN_SUCCESS.getCode(),
                SuccessMessages.LOGIN_SUCCESS.getMessage(),
                player
        );
        return ResponseEntity.ok().body(response);

    }

    @GetMapping("/logout")
    public ResponseEntity<?> playerLogout(HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        MessageResponse response;

        if (player != null) {
            session.invalidate();

            response = new MessageResponse(
                    SuccessMessages.LOGOUT_SUCCESS.getCode(),
                    SuccessMessages.LOGOUT_SUCCESS.getMessage()
            );
            return ResponseEntity.ok().body(response);
        }

        response = new MessageResponse(
                ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deletePlayer(@RequestParam String id) {

        MessageResponse response;

        if (id.isEmpty() || id.isBlank()){

            response = new MessageResponse(
                    ErrorMessages.EMPTY_ID.getCode(),
                    ErrorMessages.EMPTY_ID.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        int id_player = Integer.parseInt(id);
        Player player = playerDAO.findPlayerById(id_player);

        if (player != null) {
            playerDAO.delete(player);

            response = new MessageResponse(
                    SuccessMessages.PLAYER_DELETED.getCode(),
                    SuccessMessages.PLAYER_DELETED.getMessage()
            );
            return ResponseEntity.ok().body(response);

        }

        response = new MessageResponse(
                ErrorMessages.PLAYER_NOT_FOUND.getCode(),
                ErrorMessages.PLAYER_NOT_FOUND.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

    }


    /**
     * Post request for requesting password change
     * @param email URL parameter with player's email
     * @return Sends and email to the player who has requested password change
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {

        if (!Validator.emailIsValid(email)) {
            MessageResponse response = new MessageResponse(ErrorMessages.EMAIL_FORMAT.getCode(), ErrorMessages.EMAIL_FORMAT.getMessage());;
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = "";
        try {
            username = playerDAO.findPlayerByEmail(email).getNickname();
        } catch (NullPointerException e) {
            MessageResponse response = new MessageResponse(ErrorMessages.EMAIL_NOT_REGISTERED.getCode(), ErrorMessages.EMAIL_NOT_REGISTERED.getMessage());;
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String link = passwordResetService.initiatePasswordReset(email);

        String body = """
                Ciao %s,
                abbiamo ricevuto una richiesta di reimpostazione della tua password per l'account associato a questo indirizzo email.
                
                Per scegliere una nuova password, fai clic sul link qui sotto:
                
                %s
                
                Se non hai richiesto il cambio password, ignora semplicemente questa mail,
                la tua password attuale rimarrà invariata e nessun'altra azione sarà necessaria.
                
                Grazie,
                il team di Lupus In Campus.
                """.formatted(username, link);

        Email.getInstance().sendEmail(email, "Recupera Password", body);

        return ResponseEntity.ok().body("Email enviata correttamente");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password has been reset successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    public List<String> validPlayerRegistration(String nickname, String email, String password){
        List<String> errors = new ArrayList<>();

        if (email.isBlank() || email.isEmpty())
            errors.add(ErrorMessages.EMPTY_EMAIL_FIELD.getMessage());
        else if (!Validator.emailIsValid(email))
            errors.add(ErrorMessages.EMAIL_FORMAT.getMessage());
        else if (playerDAO.findPlayerByEmail(email) != null)
            errors.add(ErrorMessages.EMAIL_ALREADY_USED.getMessage());


        if (password.isBlank() || password.isEmpty())
            errors.add(ErrorMessages.EMPTY_PASSWORD_FIELD.getMessage());


        if (nickname.isBlank() || nickname.isEmpty())
            errors.add(ErrorMessages.EMPTY_NICKNAME_FIELD.getMessage());
        else if (playerDAO.findPlayerByNickname(nickname) != null)
            errors.add(ErrorMessages.NICKNAME_ALREADY_USED.getMessage());

        return errors;
    }

    public List<String> validPlayerLogin(String email, String password){
        List<String> errors = new ArrayList<>();

        if (email.isBlank() || email.isEmpty())
            errors.add(ErrorMessages.EMPTY_EMAIL_FIELD.getMessage());
        else if (!Validator.emailIsValid(email))
            errors.add(ErrorMessages.EMAIL_FORMAT.getMessage());
        else if (playerDAO.findPlayerByEmail(email) == null)
            errors.add(ErrorMessages.EMAIL_NOT_REGISTERED.getMessage());


        if (password.isBlank() || password.isEmpty())
            errors.add(ErrorMessages.EMPTY_PASSWORD_FIELD.getMessage());


        if (errors.isEmpty()){
            if (playerDAO.findPlayerByEmailAndPassword(email, password) == null){
                errors.add(ErrorMessages.INCORRECT_CREDENTIALS.getMessage());
            }
        }

        return errors;
    }

    public Player createPlayer(String nickname, String email, String password){
        Player player = new Player();
        player.setNickname(nickname);
        player.setEmail(email);
        player.setPassword(password);

        return player;
    }
}
