package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.PlayerDAO;
import NC12.LupusInCampus.Model.Player;
import NC12.LupusInCampus.Model.Utils.ClientServerComunication.MessageResponse;
import NC12.LupusInCampus.Model.Enums.ErrorMessages;
import NC12.LupusInCampus.Model.Enums.SuccessMessages;
import NC12.LupusInCampus.Model.Utils.Validator;
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

    @Autowired
    public PlayerController(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
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
