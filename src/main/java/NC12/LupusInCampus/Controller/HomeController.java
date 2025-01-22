package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.PlayerDAO;
import NC12.LupusInCampus.Model.Player;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("controller/home")
public class HomeController {

    private final PlayerDAO playerDAO;

    @Autowired
    public HomeController(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
    }

    @GetMapping("/")
    public ResponseEntity<?> home(HttpSession session){

        Player player = (Player) session.getAttribute("giocatore");

        if (player == null)
            return ResponseEntity.badRequest().body("Giocatore nella sessione non trovato");

        Player informazioniPlayer = playerDAO.findPlayerById(player.getId());

        if (informazioniPlayer == null)
            return ResponseEntity.badRequest().body("Giocatore non trovato");


        return ResponseEntity.ok(informazioniPlayer);
    }
}
