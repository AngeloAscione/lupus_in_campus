package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.PlayerDAO;
import NC12.LupusInCampus.Model.Player;
import NC12.LupusInCampus.Model.Utils.ComunicazioneClientServer.MessageResponse;
import NC12.LupusInCampus.Model.Enums.ErrorMessages;
import NC12.LupusInCampus.Model.Enums.SuccessMessages;
import NC12.LupusInCampus.Model.Utils.Validator;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value="controller/player", produces = "application/json")
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

        if (!errors.isEmpty()) {
            // Sending messages error registration
            MessageResponse response = new MessageResponse(
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
        MessageResponse response = new MessageResponse(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                newPlayer,
                SuccessMessages.REGISTRATION_SUCCESS.getMessage()
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> playerLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {

        List<String> errors = validPlayerLogin(email, password);

        if (!errors.isEmpty()) {
            // Sending messages error registration
            MessageResponse response = new MessageResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    errors
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        //  If there aren't errors
        Player player = playerDAO.findPlayerByEmail(email);
        player.setFriendsList(playerDAO.findFriendsByPlayerId(player.getId()));
        session.setAttribute("player", player);

        // sending data player
        MessageResponse response = new MessageResponse(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                player,
                SuccessMessages.LOGIN_SUCCESS.getMessage()
        );
        return ResponseEntity.ok().body(response);

    }

    @PostMapping("/delete")
    public ResponseEntity<?> deletePlayer(@RequestParam String id) {

        if (id.isEmpty() || id.isBlank()){

            MessageResponse response = new MessageResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    ErrorMessages.EMPTY_ID.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        int id_player = Integer.parseInt(id);
        Player player = playerDAO.findPlayerById(id_player);

        if (player != null) {
            playerDAO.delete(player);

            MessageResponse response = new MessageResponse(
                    HttpStatus.OK.value(),
                    HttpStatus.OK.getReasonPhrase(),
                    SuccessMessages.PLAYER_DELETED.getMessage()
            );
            return ResponseEntity.ok().body(response);

        } else {
            MessageResponse response = new MessageResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    ErrorMessages.PLAYER_NOT_FOUND.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
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
            Player player = playerDAO.findPlayerByEmail(email);
            String passHashedDb = player.getPassword();

            if (!BCrypt.checkpw(password, passHashedDb)){
                errors.add(ErrorMessages.INCORRECT_CREDENTIALS.getMessage());
            }
        }

        return errors;
    }

    public Player createPlayer(String nickname, String email, String password){
        Player player = new Player();
        player.setNickname(nickname);
        player.setEmail(email);

        String hashPass = BCrypt.hashpw(password, BCrypt.gensalt());
        player.setPassword(hashPass);

        return player;
    }
}
