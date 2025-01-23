package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.Player;
import NC12.LupusInCampus.Model.Utils.ComunicazioneClientServer.MessageResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("controller/home")
public class PlayerAreaController {

    public PlayerAreaController() {}

    @GetMapping("")
    public ResponseEntity<?> home(HttpSession session){

        Player player = (Player) session.getAttribute("player");

        if (player == null) {
            MessageResponse response = new MessageResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Giocatore nella sessione non trovato"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        List<Player> friends = player.getFriendsList();
        return ResponseEntity.ok().body(friends);
    }

}
