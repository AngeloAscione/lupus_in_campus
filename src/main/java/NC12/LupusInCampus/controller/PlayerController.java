package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dto.player.LoginRequest;
import NC12.LupusInCampus.model.dto.player.RegistrationRequest;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.service.PasswordResetService;
import NC12.LupusInCampus.service.RequestService;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.service.emails.Email;
import NC12.LupusInCampus.utils.Validator;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value="controller/player")
public class PlayerController {

    private final PlayerDAO playerDAO;
    private final PasswordResetService passwordResetService;
    private final MessagesResponse messagesResponse;

    @Autowired
    public PlayerController(PlayerDAO playerDAO, PasswordResetService passwordResetService, MessagesResponse messagesResponse) {
        this.playerDAO = playerDAO;
        this.passwordResetService = passwordResetService;
        this.messagesResponse = messagesResponse;
    }

    @PutMapping("/registration")
    public ResponseEntity<String> playerRegistration(@RequestBody RegistrationRequest registrationRequest, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        String nickname = registrationRequest.getNickname();
        String password = registrationRequest.getPassword();
        String email = registrationRequest.getEmail();

        List<ErrorMessages> errors = validPlayerRegistration(nickname, email, password);

        if (!errors.isEmpty()) {
            return messagesResponse.createResponse(endpoint, errors.getFirst());
        }

        // If there aren't errors
        Player player = createPlayer(nickname, email, password);

        // save
        Player newPlayer = playerDAO.save(player);
        session.setAttribute("player", newPlayer);

        // sending data player
        return messagesResponse.createResponse(endpoint, SuccessMessages.REGISTRATION_SUCCESS, newPlayer);
    }

    @PostMapping("/login")
    public ResponseEntity<String> playerLogin(@RequestBody LoginRequest loginRequest, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);
        LoggerUtil.logInfo("Sessione creata con id: " + session.getId());

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        List<ErrorMessages> errors = validPlayerLogin(email, password);

        if (!errors.isEmpty()) {
            // Sending messages error registration
            return messagesResponse.createResponse(endpoint, errors.getFirst());
        }

        //If there aren't errors
        Player player = playerDAO.findPlayerByEmail(email);
        session.setAttribute("player", player);

        // sending data player
        return messagesResponse.createResponse(endpoint, SuccessMessages.LOGIN_SUCCESS, player);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> playerLogout(HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);
        LoggerUtil.logInfo("SessioneId: " + session.getId());

        Player player = (Player) session.getAttribute("player");

        if (player == null) {// not in session
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);
        }

        //in session
        session.invalidate();
        return messagesResponse.createResponse(endpoint, SuccessMessages.LOGOUT_SUCCESS, new Player());
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deletePlayer(@RequestBody Map<String, Integer> params, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session)){
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);
        }

        Player player = (Player) session.getAttribute("player");
        int id = params.get("id");

        if (id == 0) {
            return messagesResponse.createResponse(endpoint, ErrorMessages.EMPTY_ID);
        }

        if (id != player.getId()) {
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_FOUND);
        }

        // SUCCESS
        playerDAO.delete(player);
        return messagesResponse.createResponse(endpoint, SuccessMessages.PLAYER_DELETED, new Player());
    }


    /**
     * Post request for requesting password change
     * @param params Body parameters with player's email
     * @return Sends and email to the player who has requested password change
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> params, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        String email = params.get("email");
        if (!Validator.emailIsValid(email)) {
            return messagesResponse.createResponse(endpoint, ErrorMessages.EMAIL_FORMAT);
        }

        Player player = playerDAO.findPlayerByEmail(email);
        if (player == null) {
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_FOUND);
        }

        String username = "";
        try {
            username = player.getNickname();
        } catch (NullPointerException e) {
            return messagesResponse.createResponse(endpoint, ErrorMessages.EMAIL_NOT_REGISTERED);
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

        return messagesResponse.createResponse(endpoint, SuccessMessages.EMAIL_SENT, new Player());
    }


    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model model) {
        LoggerUtil.logInfo("-> Ricevuta richiesta resetPassword");
        try{
            Player p = passwordResetService.validateResetToken(token);
            model.addAttribute("username", p.getNickname());
            model.addAttribute("token", token);
            return "reset-password";
        }catch (Exception e){
            e.printStackTrace();
        }
        return "error";
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String endpoint = RequestService.getEndpoint(request);

        try {
            passwordResetService.resetPassword(params.get("token"), params.get("password"));
            return messagesResponse.createResponse(endpoint, SuccessMessages.PASSWORD_RESET);
        } catch (Exception e) {
            LoggerUtil.logError("<- Ricevuta richiesta resetPassword", e);
            return messagesResponse.createResponse(endpoint, ErrorMessages.RESET_PASSWORD_ERROR);
        }
    }

    public List<ErrorMessages> validPlayerRegistration(String nickname, String email, String password){
        List<ErrorMessages> errors = new ArrayList<>();

        if (nickname.isBlank() || nickname.isEmpty())
            errors.add(ErrorMessages.EMPTY_NICKNAME_FIELD);
        else if (playerDAO.findPlayerByNickname(nickname) != null)
            errors.add(ErrorMessages.NICKNAME_ALREADY_USED);

        if (!errors.isEmpty()) return errors;

        if (email.isBlank() || email.isEmpty())
            errors.add(ErrorMessages.EMPTY_EMAIL_FIELD);
        else if (!Validator.emailIsValid(email))
            errors.add(ErrorMessages.EMAIL_FORMAT);
        else if (playerDAO.findPlayerByEmail(email) != null)
            errors.add(ErrorMessages.EMAIL_ALREADY_USED);

        if (!errors.isEmpty()) return errors;

        if (password.isBlank() || password.isEmpty())
            errors.add(ErrorMessages.EMPTY_PASSWORD_FIELD);

        return errors;
    }

    public List<ErrorMessages> validPlayerLogin(String email, String password){
        List<ErrorMessages> errors = new ArrayList<>();

        if (email.isBlank() || email.isEmpty())
            errors.add(ErrorMessages.EMPTY_EMAIL_FIELD);
        else if (!Validator.emailIsValid(email))
            errors.add(ErrorMessages.EMAIL_FORMAT);
        else if (playerDAO.findPlayerByEmail(email) == null)
            errors.add(ErrorMessages.EMAIL_NOT_REGISTERED);

        if (!errors.isEmpty()) return errors;

        if (password.isBlank() || password.isEmpty())
            errors.add(ErrorMessages.EMPTY_PASSWORD_FIELD);

        if (errors.isEmpty()){
            if (playerDAO.findPlayerByEmailAndPassword(email, password) == null){
                errors.add(ErrorMessages.INCORRECT_CREDENTIALS);
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
